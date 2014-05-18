package com.jcloisterzone.ai;

import java.io.File;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.ai.step.Step;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.LoadGamePhase;

public abstract class RankingAiPlayer extends AiPlayer {

    //private Map<Feature, AiScoreContext> scoreCache = new HashMap<>();
    //private List<PositionLocation> hopefulGatePlacements = new ArrayList<PositionLocation>();

//    public Map<Feature, AiScoreContext> getScoreCache() {
//        return scoreCache;
//    }

    private final GameRanking gameRanking;
    private Step bestChain = null;


    public RankingAiPlayer() {
        gameRanking = createGameRanking();
    }

    abstract protected GameRanking createGameRanking();

    public GameRanking getGameRanking() {
        return gameRanking;
    }


    public Step getBestChain() {
        return bestChain;
    }

    public void setBestChain(Step bestChain) {
        this.bestChain = bestChain;
    }

    protected void popActionChain() {
        if (bestChain.getPrevious() == null) {
            bestChain.performOnServer(getServer());
            bestChain = null;
            return;
        }

        Step step = bestChain;
        while (step.getPrevious().getPrevious() != null) {
            step = step.getPrevious();
        }
        step.getPrevious().performOnServer(getServer());
        step.setPrevious(null); //cut last element from chain
    }

    private void autosave() {
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
    }

    @Subscribe
    public void selectAction(SelectActionEvent ev) {
        if (isAiPlayerActive()) {
            if (bestChain != null) {
                popActionChain();
            } else {
                autosave();
                new Thread(new SelectActionTask(this, ev)).start();
            }
        }
    }

    @Subscribe
    public void selectDragonMove(SelectDragonMoveEvent ev) {
        if (isAiPlayerActive()) {
             new Thread(new SelectDragonMoveTask(this, ev)).start();
        }
    }

    //TODO is there faster game copying without snapshot? or without re-creating board and tile instances
    protected Game copyGame(Object gameListener) {
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
}
