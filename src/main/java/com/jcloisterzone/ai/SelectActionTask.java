package com.jcloisterzone.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.action.SelectFollowerAction;
import com.jcloisterzone.action.SelectTileAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.ai.choice.ActionChoice;
import com.jcloisterzone.ai.choice.AiChoice;
import com.jcloisterzone.ai.choice.PassChoice;
import com.jcloisterzone.ai.choice.TilePlacementChoice;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.SelectActionEvent;
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
    private final Deque<AiChoice> queue = new LinkedList<>();

    private AiChoice choice = null;
    private AiChoice bestSoFar = null;
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
        AiChoice step = bestSoFar;
        while (step != null) {
            System.out.print("  - ");
            System.out.println(step.toString());
            step = step.getPrevious();
        }
        System.out.println("*** ranking end ***");
    }

    private void dbgPringStep(AiChoice choice, boolean isFinal) {
        AiChoice ac = choice;
        StringBuilder sb = new StringBuilder("  ");
        while (ac.getPrevious() != null) {
            sb.append("  ");
            ac = ac.getPrevious();
        }
        sb.append("- ").append(choice.toString()).append(" ");

        while (sb.length() < 80) {
           sb.append(".");
        }
        sb.append(" ").append(String.format(Locale.ROOT, "%10.5f", choice.getRanking()));
        if (isFinal) {
            sb.append(" ...... ");
            sb.append(String.format(Locale.ROOT, "%10.5f", choice.getChainRanking()));
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
                choice = queue.pop();
                spm.restore(choice.getSavePoint());
                choice.perform(game.getPhase());
                boolean isFinal = phaseLoop();
                choice.rankPartial(aiPlayer.getGameRanking(), game);
                if (isFinal) {
                    rankFinal(choice);
                }
                if (dbgPrint) dbgPringStep(choice, isFinal);
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

    private void rankFinal(AiChoice step) {
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

        for (PlayerAction<?> action : ev.getActions()) {
            if (action instanceof MeepleAction) {
                meepleActions.add((MeepleAction) action);
            } else if (action instanceof TilePlacementAction) {
                handleTilePlacementAction(savePoint, (TilePlacementAction) action);
            } else if (action instanceof SelectTileAction) {
                handleSelectTileAction(savePoint, (SelectTileAction) action);
            } else if (action instanceof SelectFeatureAction) {
                handleSelectFeatureAction(savePoint, (SelectFeatureAction) action);
            } else if (action instanceof SelectFollowerAction) {
                handleSelectFollowerAction(savePoint, (SelectFollowerAction) action);
            }
        }

        if (!meepleActions.isEmpty()) {
            for (MeepleAction action : preprocessMeepleActions(meepleActions)) {
                handleSelectFeatureAction(savePoint, action);
            }
        }

        if (ev.isPassAllowed()) {
            queue.push(new PassChoice(choice, savePoint));
        }
    }

    protected void handleTilePlacementAction(SavePoint savePoint, TilePlacementAction action) {
        for (TilePlacement tp : action.getOptions()) {
            queue.push(new TilePlacementChoice(choice, savePoint, action, tp));
        }
    }

    protected void handleSelectTileAction(SavePoint savePoint, SelectTileAction action) {
        for (Position pos : action.getOptions()) {
            queue.push(new ActionChoice<Position>(choice, savePoint, action, pos));
        }
    }

    protected void handleSelectFeatureAction(SavePoint savePoint, SelectFeatureAction action) {
        for (FeaturePointer fp : action.getOptions()) {
            queue.push(new ActionChoice<FeaturePointer>(choice, savePoint, action, fp));
        }
    }


    protected void handleSelectFollowerAction(SavePoint savePoint, SelectFollowerAction action) {
        for (MeeplePointer mp : action.getOptions()) {
            queue.push(new ActionChoice<MeeplePointer>(choice, savePoint, action, mp));
        }
    }

    //don't count again for phantom
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