package com.jcloisterzone.ai;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
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
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.phase.AbbeyPhase;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.EscapePhase;
import com.jcloisterzone.game.phase.LoadGamePhase;
import com.jcloisterzone.game.phase.PhantomPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.TowerCapturePhase;

public abstract class RankingAiPlayer extends AiPlayer {

    class PositionLocation {
        Position position;
        Location location;
    }

   // private Game original;
   /// private SavePointManager spm;

//    private AiInteraction defaultInteractionHandler;
//    private AiInteraction interactionHandler = null;

    //private Map<Feature, AiScoreContext> scoreCache = new HashMap<>();
    //private List<PositionLocation> hopefulGatePlacements = new ArrayList<PositionLocation>();

//    public Map<Feature, AiScoreContext> getScoreCache() {
//        return scoreCache;
//    }

    public RankingAiPlayer() {
        //defaultInteractionHandler = createDefaultInteractionHandler();
    }

    @Subscribe
    public void selectAction(SelectActionEvent ev) {
        if (isAiPlayerActive()) {
            new Thread(new SelectActionTask(ev)).start();
        }
    }

    @Subscribe
    public void selectDragonMove(SelectDragonMoveEvent ev) {
        if (isAiPlayerActive()) {
             new Thread(new SelectDragonMoveTask(ev)).start();
        }
    }

    //TODO is there faster game copying without snapshot? or without re-creating board and tile instances
    private Game copyGame(Object gameListener) {
        Snapshot snapshot = new Snapshot(game, 0);
        Game copy = snapshot.asGame();
        copy.setConfig(game.getConfig());
        copy.getEventBus().register(gameListener);
        LoadGamePhase phase = new LoadGamePhase(copy, snapshot, null);
        phase.setSlots(new PlayerSlot[0]);
        copy.getPhases().put(phase.getClass(), phase);
        copy.setPhase(phase);
        phase.startGame();
        return copy;
    }

    private void performAction(SelectedAction sa) {
        if (sa == null) {
            getServer().pass();
            return;
        }
        // logger.info("Polling " + sa.action);
        if (sa.action instanceof MeepleAction) {
            MeepleAction action = (MeepleAction) sa.action;
            // debug, should never happen, but it happens sometimes when
            // Tower game is enabled
            // try {
            // getPlayer().getMeepleFromSupply(action.getMeepleType());
            // } catch (NoSuchElementException e) {
            // logger.error(e.getMessage(), e);
            // throw e;
            // }
            action.perform(getServer(), sa.position, sa.location);
            return;
        }
        if (sa.action instanceof BarnAction) {
            BarnAction action = (BarnAction) sa.action;
            action.perform(getServer(), sa.position, sa.location);
            return;
        }
        if (sa.action instanceof SelectTileAction) {
            SelectTileAction action = (SelectTileAction) sa.action;
            action.perform(getServer(), sa.position);
            return;
        }
        if (sa.action instanceof SelectFollowerAction) {
            SelectFollowerAction action = (SelectFollowerAction) sa.action;
            action.perform(getServer(), sa.position, sa.location, sa.meepleType, sa.meepleOwner);
            return;
        }
        throw new UnsupportedOperationException("Unhandled action type " + sa.action.getName()); // should never happen
    }


    class SelectActionTask implements Runnable {

        private SelectActionEvent ev;
        private PositionRanking bestSoFar;
        private SavePointManager spm;
        private Game game;

        public SelectActionTask(SelectActionEvent ev) {
            this.ev = ev;
            this.bestSoFar = new PositionRanking(Double.NEGATIVE_INFINITY);
            this.game = copyGame(this);
        }

        @Subscribe
        public void selectAction(SelectActionEvent ev) {
            if (game.getPhase() instanceof PhantomPhase) return;
            boolean hasSmallFollower = false;
            boolean hasPhantom = false;
            for (PlayerAction a : ev.getActions()) {
                if (a instanceof MeepleAction && ((MeepleAction) a).getMeepleType().equals(SmallFollower.class))
                    hasSmallFollower = true;
                if (a instanceof MeepleAction && ((MeepleAction) a).getMeepleType().equals(Phantom.class))
                    hasPhantom = true;
            }
            if (hasSmallFollower && hasPhantom) {
                rankAction(Collections2.filter(ev.getActions(), new Predicate<PlayerAction>() {
                    @Override
                    public boolean apply(PlayerAction a) {
                        return !(a instanceof MeepleAction && ((MeepleAction) a).getMeepleType().equals(Phantom.class));
                    }
                }));
            } else {
                rankAction(ev.getActions());
            }
        }

        @Override
        public void run() {
            try {
                PlayerAction firstAction = ev.getActions().get(0);
                if (firstAction instanceof TilePlacementAction) {
                    selectTilePlacement((TilePlacementAction) firstAction);
                    return;
                }
                if (firstAction instanceof AbbeyPlacementAction) {
                    selectAbbeyPlacement((AbbeyPlacementAction) firstAction);
                    return;
                }
                if (firstAction instanceof UndeployAction ) {
                    //hack, AI never use escape, TODO
                    if (firstAction.getName().equals(SiegeCapability.UNDEPLOY_ESCAPE)) {
                        getServer().pass();
                    }
                }

                //throw new UnsupportedOperationException("Not implemented");
            } catch (Exception e) {
                 handleRuntimeError(e);
                 selectDummyAction(ev.getActions(), ev.isPassAllowed());
            }

            if (bestSoFar.getRank() == Double.NEGATIVE_INFINITY) { // loaded game or wagon phase or phantom phase
                System.err.println("TODO ###");

                backupGame();
                if (ev.isPassAllowed()) rankPass();
                rankAction(ev.getActions());
                restoreGame();
            }

            Deque<SelectedAction> selected = bestSoFar.getSelectedActions();
            SelectedAction sa = selected.pollFirst();
            performAction(sa);
        }

        protected void backupGame() {
            spm = new SavePointManager(game);
            bestSoFar = new PositionRanking(Double.NEGATIVE_INFINITY);
            spm.startRecording();
        }

        protected void restoreGame() {
            spm.stopRecording();
            spm = null;
        }

        /* TODP COPIED FROM CLIENT STUB */
        @SuppressWarnings("unchecked")
        private void phaseLoop() {
            Phase phase = game.getPhase();
            List<Class<? extends Phase>> allowed = Lists.newArrayList(ActionPhase.class, EscapePhase.class, TowerCapturePhase.class);
            while (!phase.isEntered()) {
                if (!Iterables.contains(allowed, phase.getClass())) {
                    break;
                }
                //logger.info("  * not entered {} -> {}", phase, phase.getDefaultNext());
                phase.setEntered(true);
                phase.enter();
                phase = game.getPhase();
            }
        }

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
            DebugConfig debugConfig = game.getConfig().getDebug();
            if (debugConfig != null && debugConfig.getAutosave() != null && debugConfig.getAutosave().length() > 0) {
                Snapshot snapshot = new Snapshot(game, 0);
                if ("plain".equals(debugConfig.getSave_format())) {
                    snapshot.setGzipOutput(false);
                }
                try {
                    snapshot.save(new File(debugConfig.getAutosave()));
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
                    //logger.info("  * phase {} -> {}", game.getPhase(), game.getPhase().getDefaultNext());
                    //logger.info("  * placing {} {}", pos, rot);
                    game.getPhase().placeTile(rot, pos);
                    //logger.info("  * phase {} -> {}", game.getPhase(), game.getPhase().getDefaultNext());
                    phaseLoop();
                    double currRank = rank(game);
                    if (currRank > bestSoFar.getRank()) {
                        bestSoFar = new PositionRanking(currRank, pos, rot);
                    }
                    spm.restore(sp);
                    //TODO fix hopefulGatePlacement
                    //now rank meeple placements - must restore because rank change game
                    //game.getPhase().placeTile(rot, pos);
                    //hopefulGatePlacements.clear();
                    //spm.restore(sp);
                    //TODO add best placements for MAGIC GATE
                    //game.getPhase().enter();
                }
            }
            restoreGame();
            logger.info("Rank {} > {}", game.getCurrentTile() == null ? "Abbey" : game.getCurrentTile().getId(), bestSoFar);
        }

        protected void rankAction(Collection<PlayerAction> actions) {
            Tile currTile = game.getCurrentTile();
            Position pos = currTile.getPosition();
            for (PlayerAction action : actions) {
                if (action instanceof MeepleAction) {
                    MeepleAction ma = (MeepleAction) action;
                    rankMeeplePlacement(currTile, ma, ma.getMeepleType(), pos, ma.getLocationsMap().get(pos));
//    				for (PositionLocation posloc : hopefulGatePlacements) {
//    					rankMeepleAction(currTile, ma, posloc.position, Collections.singleton(posloc.location));
//    				}
                }
                if (action instanceof BarnAction) {
                    BarnAction ba = (BarnAction) action;
                    rankMeeplePlacement(currTile, ba, Barn.class, pos, ba.get(pos));
                }
                if (action instanceof FairyAction) {
                    rankFairyPlacement(currTile, (FairyAction) action);
                }
//                if (action instanceof TowerPieceAction) {
//                    rankTowerPiecePlacement(currTile, (TowerPieceAction) action);
//                }
            }
        }

        protected void rankFairyPlacement(Tile currTile, FairyAction action) {
            SavePoint sp = spm.save();
            for (Position pos: action.getSites()) {
                game.getPhase().moveFairy(pos);
                double currRank = rank(game);
                if (currRank > bestSoFar.getRank()) {
                    bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
                    bestSoFar.getSelectedActions().add(new SelectedAction(action, pos, null));
                }
                spm.restore(sp);
            }
        }

//        protected void rankTowerPiecePlacementOnTile(final Tile currTile, final TowerPieceAction towerPieceAction, final Position towerPiecePos) {
//            this.interactionHandler = new AiInteractionAdapter() {
//                public void selectAction(List<PlayerAction> actions, boolean canPass) {
//                    phaseLoop();
//                    SavePoint sp = spm.save();
//                    TakePrisonerAction prisonerAction = (TakePrisonerAction) actions.get(0);
//                    for (Entry<Position, Set<Location>> entry : prisonerAction.getLocationsMap().entrySet()) {
//                        Position pos = entry.getKey();
//                        for (Location loc : entry.getValue()) {
//                            for (Meeple m : getBoard().get(pos).getFeature(loc).getMeeples()) {
//                                game.getPhase().takePrisoner(pos, loc, m.getClass(), m.getPlayer().getIndex());
//                                double currRank = rank();
//                                if (currRank > bestSoFar.getRank()) {
//                                    bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
//                                    Deque<SelectedAction> sa = bestSoFar.getSelectedActions();
//                                    sa.add(new SelectedAction(towerPieceAction, towerPiecePos, null));
//                                    sa.add(new SelectedAction(prisonerAction, pos, loc, m.getClass(), m.getPlayer()));
//                                }
//                                spm.restore(sp);
//                            }
//                        }
//                    }
//                };
//            };
//            game.getPhase().placeTowerPiece(towerPiecePos);
//        }

//        protected void rankTowerPiecePlacement(Tile currTile,TowerPieceAction action) {
//            AiInteraction interactionHandlerBackup = this.interactionHandler;
//            SavePoint sp = spm.save();
//            for (Position pos: action.getSites()) {
//                rankTowerPiecePlacementOnTile(currTile, action, pos);
//                spm.restore(sp);
//            }
//
//            this.interactionHandler = interactionHandlerBackup;
//        }

        protected void rankMeeplePlacement(Tile currTile, PlayerAction action, Class<? extends Meeple> meepleType, Position pos, Set<Location> locations) {
            if (locations == null) {
                return;
            }
            SavePoint sp = spm.save();
            for (Location loc : locations) {
                //logger.info("    . deploying {}", meepleType);
                game.getPhase().deployMeeple(pos, loc, meepleType);
                double currRank = rank(game);
                if (currRank > bestSoFar.getRank()) {
                    bestSoFar = new PositionRanking(currRank, currTile.getPosition(), currTile.getRotation());
                    bestSoFar.getSelectedActions().add(new SelectedAction(action, pos, loc));
                }
                spm.restore(sp);
            }
        }

        public void rankPass() {
            SavePoint sp = spm.save();
            game.getPhase().pass();
            double currRank = rank(game);
            if (currRank > bestSoFar.getRank()) {
                bestSoFar = new PositionRanking(currRank);
            }
            spm.restore(sp);
        }
    }

    class SelectDragonMoveTask implements Runnable {
        private SelectDragonMoveEvent ev;

        public SelectDragonMoveTask(SelectDragonMoveEvent ev) {
            this.ev = ev;
        }

        @Override
        public void run() {
            try {
                //TODO
                throw new UnsupportedOperationException("use task from legacy ai player");
            } catch (Exception e) {
                 handleRuntimeError(e);
                 selectDummyDragonMove(ev.getPositions(), ev.getMovesLeft());
            }
        }
    }





//    public void cleanRanking() {
//        bestSoFar = null;
//        //scoreCache.clear();
//    }

    abstract protected double rank(Game game);

//    @Override
//    protected void handleRuntimeError(Exception e) {
//        super.handleRuntimeError(e);
//        cleanRanking();
//        if (original != null) {
//            restoreGame();
//        }
//    }

//    class RankingInteractionHanlder extends AiInteractionAdapter {
//        @Override
//        public void selectAction(List<PlayerAction> actions, boolean canPass) {
//            if (game.getPhase() instanceof PhantomPhase) return;
//            boolean hasSmallFollower = false;
//            boolean hasPhantom = false;
//            for (PlayerAction a : actions) {
//                if (a instanceof MeepleAction && ((MeepleAction) a).getMeepleType().equals(SmallFollower.class)) hasSmallFollower = true;
//                if (a instanceof MeepleAction && ((MeepleAction) a).getMeepleType().equals(Phantom.class)) hasPhantom = true;
//            }
//            if (hasSmallFollower && hasPhantom) {
//                rankAction(Collections2.filter(actions, new Predicate<PlayerAction>() {
//                    @Override
//                    public boolean apply(PlayerAction a) {
//                        return !(a instanceof MeepleAction && ((MeepleAction) a).getMeepleType().equals(Phantom.class));
//                    }
//                }));
//            } else {
//                rankAction(actions);
//            }
//        }
//    }

//    class DefaultInteraction extends AiInteractionAdapter {
//
//        @Override
//        public void selectAction(List<PlayerAction> actions, boolean canPass) {
//            if (actions.size() > 0) {
//                PlayerAction firstAction = actions.get(0);
//
//                if (firstAction instanceof TilePlacementAction) {
//                    selectTilePlacement((TilePlacementAction) firstAction);
//                    return;
//                }
//                if (firstAction instanceof AbbeyPlacementAction) {
//                    selectAbbeyPlacement((AbbeyPlacementAction) firstAction);
//                    return;
//                }
//                if (firstAction instanceof UndeployAction ) {
//                    //hack, ai never use escape, TODO
//                    if (firstAction.getName().equals(SiegeCapability.UNDEPLOY_ESCAPE)) {
//                        getServer().pass();
//                    }
//                }
//            }
//
//            if (bestSoFar == null) { //loaded game or wagon phase or phantom phase
//                backupGame();
//                if (canPass) rankPass();
//                rankAction(actions);
//                restoreGame();
//            }
//
//            Deque<SelectedAction> selected = bestSoFar.getSelectedActions();
//            SelectedAction sa = selected.pollFirst();
//            if (sa != null) {
//                //logger.info("Polling " + sa.action);
//                if (sa.action instanceof MeepleAction) {
//                    MeepleAction action = (MeepleAction) sa.action;
//                    //debug, should never happen, but it happens sometimes when Tower game is enabled
////                    try {
////                        getPlayer().getMeepleFromSupply(action.getMeepleType());
////                    } catch (NoSuchElementException e) {
////                        logger.error(e.getMessage(), e);
////                        throw e;
////                    }
//                    action.perform(getServer(), sa.position, sa.location);
//                } else if (sa.action instanceof BarnAction) {
//                    BarnAction action = (BarnAction) sa.action;
//                    action.perform(getServer(), sa.position, sa.location);
//                } else if (sa.action instanceof SelectTileAction) {
//                    SelectTileAction action = (SelectTileAction) sa.action;
//                    action.perform(getServer(), sa.position);
//                } else if (sa.action instanceof SelectFollowerAction) {
//                    SelectFollowerAction action = (SelectFollowerAction) sa.action;
//                    action.perform(getServer(), sa.position, sa.location, sa.meepleType, sa.meepleOwner);
//                } else {
//                    throw new UnsupportedOperationException("Unhandled action type " + sa.action.getName()); //should never happen
//                }
//            } else {
//                getServer().pass();
//            }
//
//            if (selected.isEmpty()) {
//                cleanRanking();
//            }
//        }
//    }

}
