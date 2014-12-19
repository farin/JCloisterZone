package com.jcloisterzone.ai;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.transform.TransformerException;

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

    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    //private Map<Feature, AiScoreContext> scoreCache = new HashMap<>();
    //private List<PositionLocation> hopefulGatePlacements = new ArrayList<PositionLocation>();

//    public Map<Feature, AiScoreContext> getScoreCache() {
//        return scoreCache;
//    }

    private final GameRanking gameRanking;
    private final AtomicReference<AiChoice> bestChain = new AtomicReference<>();


    public RankingAiPlayer() {
        gameRanking = createGameRanking();
    }

    abstract protected GameRanking createGameRanking();

    public GameRanking getGameRanking() {
        return gameRanking;
    }


    public AiChoice getBestChain() {
        return bestChain.get();
    }

    public void setBestChain(AiChoice bestChain) {
        this.bestChain.set(bestChain);
    }

    protected void popActionChain() {
        AiChoice toExecute = null;
        AiChoice best = bestChain.get();
        if (best.getPrevious() == null) {
            toExecute = best;
            bestChain.set(null);
        } else {
            AiChoice choice = best;
            while (choice.getPrevious().getPrevious() != null) {
                choice = choice.getPrevious();
            }
            toExecute = choice.getPrevious();
            choice.setPrevious(null); //cut last element from chain
        }
        //logger.info("pop chain " + this.toString() + ": " + toExecute.toString());
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
            } catch (TransformerException | IOException e) {
                logger.error("Auto save before ranking failed.", e);
            }
        }
    }

    @Subscribe
    public void selectAction(SelectActionEvent ev) {
        if (getPlayer().equals(ev.getTargetPlayer())) {
            //logger.info("SA " + game.getTilePack().size() + "|" + ev.getPlayer() + " > " + ev.getActions().toString() + " ?" + (getBestChain()==null?"null":"chain"));
            if (getBestChain() != null) {
                popActionChain();
            } else {
                autosave();
                executor.submit(new SelectActionTask(this, ev));
            }
        } else {
            if (getBestChain() != null) {
                logger.warn("AI action chain wasn't fully used! There is an error in ranking engine.");
                setBestChain(null);
            }
        }
    }

    @Subscribe
    public void selectDragonMove(SelectDragonMoveEvent ev) {
        if (getPlayer().equals(ev.getTargetPlayer())) {
             new Thread(new SelectDragonMoveTask(this, ev), "AI-selectDragonMove").start();
        }
    }

    //TODO is there faster game copying without snapshot? or without re-creating board and tile instances
    protected Game copyGame(Object gameListener) {
        Snapshot snapshot = new Snapshot(game);
        Game copy = snapshot.asGame(game.getGameId());
        copy.setConfig(game.getConfig());
        copy.setPreparedGame(game.isPreparedGame());
        copy.setPreparedTiles(game.getPreparedTiles());
        copy.getEventBus().register(gameListener);
        LoadGamePhase phase = new LoadGamePhase(copy, snapshot, getConnection());
        phase.setSlots(new PlayerSlot[0]);
        copy.getPhases().put(phase.getClass(), phase);
        copy.setPhase(phase);
        phase.startGame();
        return copy;
    }
}
