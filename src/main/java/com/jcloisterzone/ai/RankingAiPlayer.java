package com.jcloisterzone.ai;

import java.io.FileOutputStream;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.ai.choice.AiChoice;
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
    private AiChoice bestChain = null;


    public RankingAiPlayer() {
        gameRanking = createGameRanking();
    }

    abstract protected GameRanking createGameRanking();

    public GameRanking getGameRanking() {
        return gameRanking;
    }


    public AiChoice getBestChain() {
        return bestChain;
    }

    public void setBestChain(AiChoice bestChain) {
        this.bestChain = bestChain;
    }

    protected void popActionChain() {
        AiChoice toExecute = null;
        if (bestChain.getPrevious() == null) {
            toExecute = bestChain;
            bestChain = null;
        } else {
            AiChoice choice = bestChain;
            while (choice.getPrevious().getPrevious() != null) {
                choice = choice.getPrevious();
            }
            toExecute = choice.getPrevious();
            choice.setPrevious(null); //cut last element from chain
        }
        //execute after chain update is done
        toExecute.perform(getServer());
    }

    private void autosave() {
        DebugConfig debugConfig = game.getConfig().getDebug();
        if (debugConfig != null && debugConfig.getAutosave() != null && debugConfig.getAutosave().length() > 0) {
            Snapshot snapshot = new Snapshot(game);
            if ("plain".equals(debugConfig.getSave_format())) {
                snapshot.setGzipOutput(false);
            }
            try {
                snapshot.save(new FileOutputStream(debugConfig.getAutosave()));
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
                new Thread(new SelectActionTask(this, ev), "AI-selectAction").start();
            }
        }
    }

    @Subscribe
    public void selectDragonMove(SelectDragonMoveEvent ev) {
        if (isAiPlayerActive()) {
             new Thread(new SelectDragonMoveTask(this, ev), "AI-selectDragonMove").start();
        }
    }

    //TODO is there faster game copying without snapshot? or without re-creating board and tile instances
    protected Game copyGame(Object gameListener) {
        Snapshot snapshot = new Snapshot(game);
        Game copy = snapshot.asGame();
        copy.setConfig(game.getConfig());
        copy.getEventBus().register(gameListener);
        LoadGamePhase phase = new LoadGamePhase(copy, snapshot, getServer(), getConnection());
        phase.setSlots(new PlayerSlot[0]);
        copy.getPhases().put(phase.getClass(), phase);
        copy.setPhase(phase);
        phase.startGame();
        return copy;
    }
}
