package com.jcloisterzone.ai;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.ai.copy.CopyGamePhase;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.Phase;

public abstract class RankingAiPlayer extends AiPlayer {

    class PositionLocation {
        Position position;
        Location location;
    }

    private Game original;
    private SavePointManager spm;

    private Map<Feature, AiScoreContext> scoreCache = Maps.newHashMap();
    private PositionRanking bestSoFar;
    //private List<PositionLocation> hopefulGatePlacements = new ArrayList<PositionLocation>();

    public Map<Feature, AiScoreContext> getScoreCache() {
        return scoreCache;
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);
        //spm = new SavePointManager(game);
    }

    /* TEMPORARY COPIED FROM CLIENT STUB */
    @Deprecated
    private void phaseLoop() {
        Phase phase = getGame().getPhase();
        while(! phase.isEntered()) {
            //logger.info("  * not entered {} -> {}", phase, phase.getDefaultNext());
            phase.setEntered(true);
            phase.enter();
            //phase = getGame().getPhase();
            break; //now only transition Tile -> Action phase
        }
    }

    //TEMPORARY method
    //TODO fast game copying without snapshot ? or without re creating board and tile instances
    //TODO do not recreate SavePointManager
    private void backupGame() {
        assert original == null;
        original = getGame();

        Snapshot snapshot = new Snapshot(getGame(), 0);
        Game gameCopy = snapshot.asGame();
        gameCopy.setConfig(getGame().getConfig());
        gameCopy.addGameListener(new GameEventAdapter());
        gameCopy.addUserInterface(this);
        Phase phase = new CopyGamePhase(gameCopy, snapshot, getGame().getTilePack());
        gameCopy.getPhases().put(phase.getClass(), phase);
        gameCopy.setPhase(phase);
        phase.startGame();
        setGame(gameCopy);

        spm = new SavePointManager(getGame());
        bestSoFar = new PositionRanking(Double.NEGATIVE_INFINITY);
        spm.startRecording();
    }

    private void restoreGame() {
        assert original != null;
        spm.stopRecording();
        spm = null;
        setGame(original);
        original = null;
    }

    private boolean isRankingInProcess() {
        return original != null;
    }


    protected void selectAbbeyPlacement(AbbeyPlacementAction action) {
        Map<Position, Set<Rotation>> placements = Maps.newHashMap();
        for(Position pos : action.getSites()) {
            placements.put(pos, Collections.singleton(Rotation.R0));
        }
        rankTilePlacement(placements);
        if (bestSoFar.getRank() > 2.0) {
            getServer().placeTile(bestSoFar.getRotation(), bestSoFar.getPosition());
        } else {
            getServer().pass();
        }
    }

    protected void selectTilePlacement(TilePlacementAction action) {
        Map<Position, Set<Rotation>> placements = action.getAvailablePlacements();
        rankTilePlacement(placements);
        getServer().placeTile(bestSoFar.getRotation(), bestSoFar.getPosition());
    }

    protected void rankTilePlacement(Map<Position, Set<Rotation>> placements) {
        //logger.info("---------- Ranking start ---------------");
        //logger.info("Positions: {} ", placements.keySet());

        backupGame();
        SavePoint sp = spm.save();
        for(Entry<Position, Set<Rotation>> entry : placements.entrySet()) {
            Position pos = entry.getKey();
            for(Rotation rot : entry.getValue()) {
                //logger.info("  * phase {} -> {}", getGame().getPhase(), getGame().getPhase().getDefaultNext());
                //logger.info("  * placing {} {}", pos, rot);
                getGame().getPhase().placeTile(rot, pos);
                //logger.info("  * phase {} -> {}", getGame().getPhase(), getGame().getPhase().getDefaultNext());
                phaseLoop();
                double currRank = rank();
                if (currRank > bestSoFar.getRank()) {
                    bestSoFar = new PositionRanking(currRank, pos, rot);
                }
                spm.restore(sp);
                //TODO fix hopefulGatePlacement
                //now rank meeple placements - must restore because rank change game
                //getGame().getPhase().placeTile(rot, pos);
                //hopefulGatePlacements.clear();
                //spm.restore(sp);
                //TODO add best placements for MAGIC GATE
                //getGame().getPhase().enter();
            }
        }
        restoreGame();
        logger.info("Selected move is: {}", bestSoFar);
    }

    public void rankAction(List<PlayerAction> actions) {
        Tile currTile = getGame().getTilePack().getCurrentTile();
        Position pos = currTile.getPosition();
        for(PlayerAction action : actions) {
            if (action instanceof MeepleAction) {
                MeepleAction ma = (MeepleAction) action;
                rankMeeplePlacement(currTile, ma, ma.getMeepleType(), pos, ma.getSites().get(pos));
//				for(PositionLocation posloc : hopefulGatePlacements) {
//					rankMeepleAction(currTile, ma, posloc.position, Collections.singleton(posloc.location));
//				}
            }
            if (action instanceof BarnAction) {
                BarnAction ba = (BarnAction) action;
                rankMeeplePlacement(currTile, ba, Barn.class, pos, ba.get(pos));
            }
            if (action instanceof FairyAction) {
                rankFairyPlacement(currTile, (FairyAction) action);
            }
        }
    }

    protected void rankFairyPlacement(Tile currTile, FairyAction action) {
        SavePoint sp = spm.save();
        for(Position pos: action.getSites()) {
            getGame().getPhase().moveFairy(pos);
            double currRank = rank();
            if (currRank > bestSoFar.getRank()) {
                bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
                bestSoFar.setAction(action);
                bestSoFar.setActionPosition(pos);
            }
            spm.restore(sp);
        }
    }

    protected void rankMeeplePlacement(Tile currTile, PlayerAction action, Class<? extends Meeple> meepleType, Position pos, Set<Location> locations) {
        if (locations == null) {
            return;
        }
        SavePoint sp = spm.save();
        for(Location loc : locations) {
            //logger.info("    . deploying {}", meepleType);
            getGame().getPhase().deployMeeple(pos, loc, meepleType);
            double currRank = rank();
            if (currRank > bestSoFar.getRank()) {
                bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
                bestSoFar.setAction(action);
                bestSoFar.setActionPosition(pos);
                bestSoFar.setActionLocation(loc);
            }
            spm.restore(sp);
        }
    }

    private void rankPass() {
        SavePoint sp = spm.save();
        getGame().getPhase().pass();
        double currRank = rank();
        if (currRank > bestSoFar.getRank()) {
            bestSoFar = new PositionRanking(currRank);
        }
        spm.restore(sp);
    }

    private void cleanRanking() {
        bestSoFar = null;
        scoreCache.clear();
    }



    @Override
    public void selectAction(List<PlayerAction> actions, boolean canPass) {
        PlayerAction firstAction = actions.get(0);

        if (firstAction instanceof TilePlacementAction) {
            selectTilePlacement((TilePlacementAction) firstAction);
            return;
        }
        if (firstAction instanceof AbbeyPlacementAction) {
            selectAbbeyPlacement((AbbeyPlacementAction) firstAction);
            return;
        }

        if (isRankingInProcess()) {
            rankAction(actions);
            return;
        }

        if (bestSoFar == null) { //loaded game or wagon phase
            backupGame();
            if (canPass) rankPass();
            rankAction(actions);
            restoreGame();
        }

        if (bestSoFar != null) {
            if (bestSoFar.getAction() instanceof MeepleAction) {
                MeepleAction action = (MeepleAction) bestSoFar.getAction();
                action.perform(getServer(), bestSoFar.getActionPosition(), bestSoFar.getActionLocation());
                cleanRanking();
                return;
            }
            if (bestSoFar.getAction() instanceof BarnAction) {
                BarnAction action = (BarnAction) bestSoFar.getAction();
                action.perform(getServer(), bestSoFar.getActionPosition(), bestSoFar.getActionLocation());
                cleanRanking();
                return;
            }
            if (bestSoFar.getAction() instanceof FairyAction) {
                FairyAction action = (FairyAction) bestSoFar.getAction();
                action.perform(getServer(), bestSoFar.getActionPosition());
                cleanRanking();
                return;
            }
        }
        getServer().pass();
        cleanRanking();
    }



    @Override
    public void selectDragonMove(Set<Position> positions, int movesLeft) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void selectBazaarTile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeBazaarBid(int supplyIndex) {
        throw new UnsupportedOperationException();
    }

    abstract protected double rank();

    @Override
    protected void handleRuntimeError(Exception e) {
        super.handleRuntimeError(e);
        cleanRanking();
        if (original != null) {
            restoreGame();
        }
    }

}
