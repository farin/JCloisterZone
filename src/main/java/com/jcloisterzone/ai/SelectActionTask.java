package com.jcloisterzone.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.action.TakePrisonerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.action.UndeployAction;
import com.jcloisterzone.ai.step.DeployMeepleStep;
import com.jcloisterzone.ai.step.MoveFairyStep;
import com.jcloisterzone.ai.step.PassStep;
import com.jcloisterzone.ai.step.PlaceAbbeyStep;
import com.jcloisterzone.ai.step.PlaceTileStep;
import com.jcloisterzone.ai.step.PlaceTowerPieceStep;
import com.jcloisterzone.ai.step.Step;
import com.jcloisterzone.ai.step.TakePrisonerStep;
import com.jcloisterzone.ai.step.UndeployMeepleStep;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.EscapePhase;
import com.jcloisterzone.game.phase.PhantomPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.TowerCapturePhase;

public class SelectActionTask implements Runnable {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final RankingAiPlayer aiPlayer;
    private final SelectActionEvent rootEv;
    private final Deque<Step> queue = new LinkedList<Step>();

    private Step step = null;
    private Step bestSoFar = null;
    private double bestSoFarRanking = Double.NEGATIVE_INFINITY;

    private SavePointManager spm;
    private Game game;

    public SelectActionTask(RankingAiPlayer aiPlayer, SelectActionEvent rootEv) {
        this.aiPlayer = aiPlayer;
        this.rootEv = rootEv;
    }

    private void dbgPringHeader() {
        StringBuilder sb = new StringBuilder("*** ranking start * ");
        sb.append(game.getPhase().getClass().getSimpleName());
        sb.append(" * ");
        Player p = game.getActivePlayer();
        sb.append(p.getNick());
        sb.append(" ***");
        System.out.println(sb);
    }

    private void dbgPringFooter() {
        System.out.println("=== selected chain (reversed) " + bestSoFarRanking);
        Step step = bestSoFar;
        while (step != null) {
            System.out.print("  - ");
            System.out.println(step.toString());
            step = step.getPrevious();
        }
        System.out.println("*** ranking end ***");
    }

    private void dbgPringStep(Step step, boolean isFinal) {
        Step s = step;
        StringBuilder sb = new StringBuilder("  ");
        while (s.getPrevious() != null) {
            sb.append("  ");
            s = s.getPrevious();
        }
        sb.append("- ").append(step.toString()).append(" ");

        while (sb.length() < 80) {
           sb.append(".");
        }
        sb.append(" ").append(String.format(Locale.ROOT, "%10.5f", step.getRanking()));
        if (isFinal) {
            sb.append(" ...... ");
            sb.append(String.format(Locale.ROOT, "%10.5f", step.getChainRanking()));
        }
        System.out.println(sb);
    }


    @Override
    public void run() {
        boolean dbgPrint = false;
        try {
            this.game = aiPlayer.copyGame(this);
            if (dbgPrint) dbgPringHeader();

            spm = new SavePointManager(game);
            spm.startRecording();

            handleActionEvent(rootEv);

            while (!queue.isEmpty()) {
                step = queue.pop();
                spm.restore(step.getSavePoint());
                step.performLocal(game);
                boolean isFinal = phaseLoop();
                step.rankPartial(aiPlayer.getGameRanking(), game);
                if (isFinal) {
                    rankFinal(step);
                }
                if (dbgPrint) dbgPringStep(step, isFinal);
            }
            if (dbgPrint) dbgPringFooter();
            aiPlayer.setBestChain(bestSoFar);
            aiPlayer.popActionChain();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            aiPlayer.setBestChain(null);
            aiPlayer.selectDummyAction(rootEv.getActions(), rootEv.isPassAllowed());
        }
    }

    @SuppressWarnings("unchecked")
    private boolean phaseLoop() {
        Phase phase = game.getPhase();
        List<Class<? extends Phase>> allowed = Lists.newArrayList(ActionPhase.class, EscapePhase.class, TowerCapturePhase.class);
        while (!phase.isEntered()) {
            if (!Iterables.contains(allowed, phase.getClass())) {
                return true;
            }
            phase.setEntered(true);
            phase.enter();
            phase = game.getPhase();
        }
        return false;
    }

    private void rankFinal(Step step) {
        step.setRanking(step.getRanking() + aiPlayer.getGameRanking().getFinal(game));
        double currChainRanking = step.getChainRanking();
        if (currChainRanking > bestSoFarRanking) {
            bestSoFar = step;
            bestSoFarRanking = currChainRanking;
        }
    }

    /*
     * possible improvements (TODO)
     *  - pass action for Abbey don't count there is another tile which brings some points
     *  - Wagon usually not moved (worse rank then pass, expect cloister), common ranking action don't know anything about following wagon phase1
     *    and wagon is ranked separately
     */

    @Subscribe
    public void handleActionEvent(SelectActionEvent ev) {
        List<MeepleAction> meepleActions = new ArrayList<MeepleAction>();
        SavePoint savePoint = spm.save();

        for (PlayerAction action : ev.getActions()) {
            if (action instanceof MeepleAction) {
                meepleActions.add((MeepleAction) action);
            } else if (action instanceof TilePlacementAction) {
                handleTilePlacementAction(savePoint, (TilePlacementAction) action);
            } else if (action instanceof AbbeyPlacementAction) {
                handleAbbeyPlacement(savePoint, (AbbeyPlacementAction) action);
            } else if (action instanceof UndeployAction) {
                handleUndeployAction(savePoint, (UndeployAction) action);
            } else if (action instanceof BarnAction) {
                handleBarnAction(savePoint, (BarnAction) action);
            } else if (action instanceof FairyAction) {
                handleFairyAction(savePoint, (FairyAction) action);
            } else if (action instanceof TowerPieceAction) {
            	handleTowerPieceAction(savePoint, (TowerPieceAction) action);
            } else if (action instanceof TakePrisonerAction) {
            	handleTakePrisonerAction(savePoint, (TakePrisonerAction) action);
            }
        }

        if (!meepleActions.isEmpty()) {
            handleMeepleActions(savePoint, preprocessMeepleActions(meepleActions));
        }

        if (ev.isPassAllowed()) {
            queue.push(new PassStep(step, savePoint));
        }
    }

    protected void handleTilePlacementAction(SavePoint savePoint, TilePlacementAction action) {
        for (Entry<Position, Set<Rotation>> entry : action.getAvailablePlacements().entrySet()) {
            Position pos = entry.getKey();
            for (Rotation rot : entry.getValue()) {
                queue.push(new PlaceTileStep(step, savePoint, action, rot, pos));
            }
        }
    }

    protected void handleAbbeyPlacement(SavePoint savePoint, AbbeyPlacementAction action) {
        for (Position pos : action.getSites()) {
            queue.push(new PlaceAbbeyStep(step, savePoint, action, pos));
        }
    }

    protected void handleFairyAction(SavePoint savePoint, FairyAction action) {
        for (Position pos : action.getSites()) {
            queue.push(new MoveFairyStep(step, savePoint, action, pos));
        }
    }
    
    protected void handleTowerPieceAction(SavePoint savePoint, TowerPieceAction action) {
    	for (Position pos : action.getSites()) {
            queue.push(new PlaceTowerPieceStep(step, savePoint, action, pos));
    	}
    }
    
    protected void handleBarnAction(SavePoint savePoint, BarnAction ba) {
        handleMeepleActions(savePoint, Collections.singleton(ba));
    }

    protected void handleUndeployAction(SavePoint savePoint, UndeployAction action) {
        for (Entry<Position, Set<Location>> entry : action.getLocationsMap().entrySet()) {
            Position pos = entry.getKey();
            for (Location loc: entry.getValue()) {
                //TODO ineffective, full descriptor not contained in undeploy action
                for (Meeple m : game.getDeployedMeeples()) {
                    if (m.at(pos) && m.getLocation().equals(loc)) {
                        if (action.getPlayers().isAllowed(m.getPlayer())) {
                            queue.push(new UndeployMeepleStep(step, savePoint, action, pos, loc, m.getClass(), m.getPlayer()));
                        }
                    }
                }
            }
        }
    }

    protected Collection<MeepleAction> preprocessMeepleActions(List<MeepleAction> actions) {
        if (game.getPhase() instanceof PhantomPhase) return Collections.emptyList();
        boolean hasSmallFollower = false;
        boolean hasPhantom = false;
        for (MeepleAction a : actions) {
            if (a instanceof MeepleAction && a.getMeepleType().equals(SmallFollower.class))
                hasSmallFollower = true;
            if (a instanceof MeepleAction && a.getMeepleType().equals(Phantom.class))
                hasPhantom = true;
        }
        if (hasSmallFollower && hasPhantom) {
            return Collections2.filter(actions, new Predicate<MeepleAction>() {
                @Override
                public boolean apply(MeepleAction a) {
                    return !(a.getMeepleType().equals(Phantom.class));
                }
            });
        }
        return actions;
    }

    protected void handleMeepleActions(SavePoint savePoint, Collection<? extends SelectFeatureAction> actions) {
        Tile currTile = game.getCurrentTile();
        Position pos = currTile.getPosition();

        for (SelectFeatureAction action : actions) {
            Set<Location> locations = action.getLocationsMap().get(pos);
            if (locations == null) continue;
            for (Location loc : locations) {
                queue.push(new DeployMeepleStep(step, savePoint, action, pos, loc));
            }
        }
    }
    
    protected void handleTakePrisonerAction(SavePoint savePoint, TakePrisonerAction action) {
        for (Entry<Position, Set<Location>> entry : action.getLocationsMap().entrySet()) {
            Position pos = entry.getKey();
            for (Location loc: entry.getValue()) {
            	//TODO copy paste ugly code from undeply action
                for (Meeple m : game.getDeployedMeeples()) {
                    if (m.at(pos) && m.getLocation().equals(loc)) {
                        if (action.getPlayers().isAllowed(m.getPlayer())) {
                            queue.push(new TakePrisonerStep(step, savePoint, action, pos, loc, m.getClass(), m.getPlayer()));
                        }
                    }
                }
          
            }
        }
    }
    
   

    // ---- refactor done boundary -----


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

}