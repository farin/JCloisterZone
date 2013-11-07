package com.jcloisterzone.ai;

import java.io.File;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFollowerAction;
import com.jcloisterzone.action.SelectTileAction;
import com.jcloisterzone.action.TakePrisonerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.ai.PositionRanking.SelectedAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.EscapePhase;
import com.jcloisterzone.game.phase.LoadGamePhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.TowerCapturePhase;

public abstract class RankingAiPlayer extends AiPlayer {

    class PositionLocation {
        Position position;
        Location location;
    }

    private Game original;
    private SavePointManager spm;

    private UserInterface defaultInteractionHandler;
    private UserInterface interactionHandler;

    //private Map<Feature, AiScoreContext> scoreCache = new HashMap<>();
    private PositionRanking bestSoFar;
    //private List<PositionLocation> hopefulGatePlacements = new ArrayList<PositionLocation>();

//    public Map<Feature, AiScoreContext> getScoreCache() {
//        return scoreCache;
//    }

    public RankingAiPlayer() {
        defaultInteractionHandler = createDefaultInteractionHandler();
        interactionHandler = defaultInteractionHandler;
    }

    protected UserInterface createDefaultInteractionHandler() {
        return new DefaultInteraction();
    }

    /* TODP COPIED FROM CLIENT STUB */
    private void phaseLoop() {
        Phase phase = getGame().getPhase();
        List<Class<? extends Phase>> allowed = Lists.newArrayList(ActionPhase.class, EscapePhase.class, TowerCapturePhase.class);
        while (!phase.isEntered()) {
            if (!Iterables.contains(allowed, phase.getClass())) {
                break;
            }
            //logger.info("  * not entered {} -> {}", phase, phase.getDefaultNext());
            phase.setEntered(true);
            phase.enter();
            phase = getGame().getPhase();
        }
    }

    //TODO is there faster game copying without snapshot? or without re-creating board and tile instances
    private Game deepGameCopy(Game game) {
        Snapshot snapshot = new Snapshot(game, 0);
        Game copy = snapshot.asGame();
        copy.setConfig(game.getConfig());
        copy.addUserInterface(this);

        LoadGamePhase phase = new LoadGamePhase(copy, snapshot, null);
        phase.setSlots(new PlayerSlot[0]);
        copy.getPhases().put(phase.getClass(), phase);
        copy.setPhase(phase);
        phase.startGame();
        return copy;
    }


    protected void backupGame() {
        assert original == null;
        original = getGame();
        Game copy = deepGameCopy(original);
        setGame(copy);
        interactionHandler = new RankingInteractionHanlder();

        spm = new SavePointManager(copy);
        bestSoFar = new PositionRanking(Double.NEGATIVE_INFINITY);
        spm.startRecording();
    }

    protected void restoreGame() {
        assert original != null;
        spm.stopRecording();
        spm = null;
        setGame(original);
        original = null;
        interactionHandler = defaultInteractionHandler;
    }

//    private boolean isRankingInProcess() {
//        return original != null;
//    }

//    public PositionRanking getBestSoFar() {
//        return bestSoFar;
//    }


    protected void selectAbbeyPlacement(AbbeyPlacementAction action) {
        Map<Position, Set<Rotation>> placements = new HashMap<>();
        for (Position pos : action.getSites()) {
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
        String autosave = game.getConfig().get("debug", "save_before_ranking");
        if (autosave != null && autosave.length() > 0) {
            Snapshot snapshot = new Snapshot(game, 0);
            if ("plain".equals(game.getConfig().get("debug", "save_format"))) {
                snapshot.setGzipOutput(false);
            }
            try {
                snapshot.save(new File(autosave));
            } catch (Exception e) {
                logger.error("Auto save before ranking failed.", e);
            }
        }

        Map<Position, Set<Rotation>> placements = action.getAvailablePlacements();
        rankTilePlacement(placements);
        getServer().placeTile(bestSoFar.getRotation(), bestSoFar.getPosition());
    }

    protected void rankTilePlacement(Map<Position, Set<Rotation>> placements) {
        //logger.info("---------- Ranking start ---------------");
        //logger.info("Positions: {} ", placements.keySet());

        backupGame();
        SavePoint sp = spm.save();
        for (Entry<Position, Set<Rotation>> entry : placements.entrySet()) {
            Position pos = entry.getKey();
            for (Rotation rot : entry.getValue()) {
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
        logger.info("Rank {} > {}", getGame().getCurrentTile() == null ? "Abbey" : getGame().getCurrentTile().getId(), bestSoFar);
    }

    protected void rankAction(List<PlayerAction> actions) {
        Tile currTile = getGame().getCurrentTile();
        Position pos = currTile.getPosition();
        for (PlayerAction action : actions) {
            if (action instanceof MeepleAction) {
                MeepleAction ma = (MeepleAction) action;
                rankMeeplePlacement(currTile, ma, ma.getMeepleType(), pos, ma.getLocationsMap().get(pos));
//				for (PositionLocation posloc : hopefulGatePlacements) {
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
            if (action instanceof TowerPieceAction) {
                rankTowerPiecePlacement(currTile, (TowerPieceAction) action);
            }
        }
    }

    protected void rankFairyPlacement(Tile currTile, FairyAction action) {
        SavePoint sp = spm.save();
        for (Position pos: action.getSites()) {
            getGame().getPhase().moveFairy(pos);
            double currRank = rank();
            if (currRank > bestSoFar.getRank()) {
                bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
                bestSoFar.getSelectedActions().add(new SelectedAction(action, pos, null));
            }
            spm.restore(sp);
        }
    }

    protected void rankTowerPiecePlacementOnTile(final Tile currTile, final TowerPieceAction towerPieceAction, final Position towerPiecePos) {
        this.interactionHandler = new NotSupportedInteraction() {
            public void selectAction(List<PlayerAction> actions, boolean canPass) {
                phaseLoop();
                SavePoint sp = spm.save();
                TakePrisonerAction prisonerAction = (TakePrisonerAction) actions.get(0);
                for (Entry<Position, Set<Location>> entry : prisonerAction.getLocationsMap().entrySet()) {
                    Position pos = entry.getKey();
                    for (Location loc : entry.getValue()) {
                        for (Meeple m : getBoard().get(pos).getFeature(loc).getMeeples()) {
                            getGame().getPhase().takePrisoner(pos, loc, m.getClass(), m.getPlayer().getIndex());
                            double currRank = rank();
                            if (currRank > bestSoFar.getRank()) {
                                bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
                                Deque<SelectedAction> sa = bestSoFar.getSelectedActions();
                                sa.add(new SelectedAction(towerPieceAction, towerPiecePos, null));
                                sa.add(new SelectedAction(prisonerAction, pos, loc, m.getClass(), m.getPlayer()));
                            }
                            spm.restore(sp);
                        }
                    }
                }
            };
        };
        getGame().getPhase().placeTowerPiece(towerPiecePos);
    }

    protected void rankTowerPiecePlacement(Tile currTile,TowerPieceAction action) {
        UserInterface interactionHandlerBackup = this.interactionHandler;
        SavePoint sp = spm.save();
        for (Position pos: action.getSites()) {
            rankTowerPiecePlacementOnTile(currTile, action, pos);
            spm.restore(sp);
        }

        this.interactionHandler = interactionHandlerBackup;
    }

    protected void rankMeeplePlacement(Tile currTile, PlayerAction action, Class<? extends Meeple> meepleType, Position pos, Set<Location> locations) {
        if (locations == null) {
            return;
        }
        SavePoint sp = spm.save();
        for (Location loc : locations) {
            //logger.info("    . deploying {}", meepleType);
            getGame().getPhase().deployMeeple(pos, loc, meepleType);
            double currRank = rank();
            if (currRank > bestSoFar.getRank()) {
                bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
                bestSoFar.getSelectedActions().add(new SelectedAction(action, pos, loc));
            }
            spm.restore(sp);
        }
    }

    public void rankPass() {
        SavePoint sp = spm.save();
        getGame().getPhase().pass();
        double currRank = rank();
        if (currRank > bestSoFar.getRank()) {
            bestSoFar = new PositionRanking(currRank);
        }
        spm.restore(sp);
    }

    public void cleanRanking() {
        bestSoFar = null;
        //scoreCache.clear();
    }

    @Override
    public void selectAction(List<PlayerAction> actions, boolean canPass) {
        interactionHandler.selectAction(actions, canPass);
    }

    @Override
    public void selectDragonMove(Set<Position> positions, int movesLeft) {
        interactionHandler.selectDragonMove(positions, movesLeft);
    }

    @Override
    public void selectBazaarTile() {
        interactionHandler.selectBazaarTile();
    }

    @Override
    public void makeBazaarBid(int supplyIndex) {
        interactionHandler.makeBazaarBid(supplyIndex);
    }

    @Override
    public void selectBuyOrSellBazaarOffer(int supplyIndex) {
        interactionHandler.selectBuyOrSellBazaarOffer(supplyIndex);
    }

    @Override
    public void selectCornCircleOption() {
        interactionHandler.selectCornCircleOption();
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

    class RankingInteractionHanlder extends NotSupportedInteraction {
        @Override
        public void selectAction(List<PlayerAction> actions, boolean canPass) {
            rankAction(actions);
        }
    }

    class DefaultInteraction extends NotSupportedInteraction {

        @Override
        public void selectAction(List<PlayerAction> actions, boolean canPass) {
            if (actions.size() > 0) {
                PlayerAction firstAction = actions.get(0);

                if (firstAction instanceof TilePlacementAction) {
                    selectTilePlacement((TilePlacementAction) firstAction);
                    return;
                }
                if (firstAction instanceof AbbeyPlacementAction) {
                    selectAbbeyPlacement((AbbeyPlacementAction) firstAction);
                    return;
                }
                if (firstAction instanceof UndeployAction ) {
                    //hack, ai never use escape, TODO
                    if (firstAction.getName().equals("escape")) {
                        getServer().pass();
                    }
                }
            }

            if (bestSoFar == null) { //loaded game or wagon phase
                backupGame();
                if (canPass) rankPass();
                rankAction(actions);
                restoreGame();
            }

            Deque<SelectedAction> selected = bestSoFar.getSelectedActions();
            SelectedAction sa = selected.pollFirst();
            if (sa != null) {
                //logger.info("Polling " + sa.action);
                if (sa.action instanceof MeepleAction) {
                    MeepleAction action = (MeepleAction) sa.action;
                    //debug, should never happen, but it happens sometimes when Tower game is enabled
                    try {
                        getPlayer().getMeepleFromSupply(action.getMeepleType());
                    } catch (NoSuchElementException e) {
                        logger.error(e.getMessage(), e);
                        throw e;
                    }
                    action.perform(getServer(), sa.position, sa.location);
                } else if (sa.action instanceof BarnAction) {
                    BarnAction action = (BarnAction) sa.action;
                    action.perform(getServer(), sa.position, sa.location);
                } else if (sa.action instanceof SelectTileAction) {
                    SelectTileAction action = (SelectTileAction) sa.action;
                    action.perform(getServer(), sa.position);
                } else if (sa.action instanceof SelectFollowerAction) {
                    SelectFollowerAction action = (SelectFollowerAction) sa.action;
                    action.perform(getServer(), sa.position, sa.location, sa.meepleType, sa.meepleOwner);
                } else {
                    throw new UnsupportedOperationException("Unhandled action type " + sa.action.getName()); //should never happen
                }
            } else {
                getServer().pass();
            }

            if (selected.isEmpty()) {
                cleanRanking();
            }
        }
    }

}
