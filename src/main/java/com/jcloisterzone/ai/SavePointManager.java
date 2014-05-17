package com.jcloisterzone.ai;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.ai.operation.MeepleDeployedOperation;
import com.jcloisterzone.ai.operation.MeepleUndeployedOperation;
import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.ai.operation.ScoreOperation;
import com.jcloisterzone.ai.operation.TilePlacedOperation;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.Phase;

public class SavePointManager {

    private final Game game;
    protected Deque<Operation> operations = new ArrayDeque<Operation>();
    private OperationRecorder operationRecorder = new OperationRecorder();

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public SavePointManager(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void startRecording() {
        game.getEventBus().register(operationRecorder);
    }

    public void stopRecording() {
        game.getEventBus().unregister(operationRecorder);
        operations.clear();
    }

    public SavePoint save() {
        Object[] backups = new Object[game.getCapabilities().size()];
        int i = 0;
        for (Capability cap : game.getCapabilities()) {
            backups[i++] = cap.backup();
        }
        return new SavePoint(operations.peekLast(), game.getPhase(), backups);

    }

    //TODO !!! probably must wait for dispatch of all events !!!
    public void restore(SavePoint sp) {
        game.getEventBus().unregister(operationRecorder);
        Operation target = sp.getOperation();
        while (operations.peekLast() != target) {
            //logger.info("      < undo {}", item);
            operations.pollLast().undo(game);
        }
        int i = 0;
        for (Capability cap : game.getCapabilities()) {
            cap.restore(sp.getCapabilitiesBackups()[i++]);
        }

        Phase phase = sp.getPhase();
        game.setPhase(phase);
        phase.setEntered(true);
        game.getEventBus().register(operationRecorder);
    }

    class OperationRecorder  {

        @Subscribe
        public void tilePlaced(TileEvent ev) {
            if (ev.getType() == TileEvent.PLACEMENT) {
                operations.addLast(new TilePlacedOperation(ev.getTile()));
            }
        }

        @Subscribe
        public void meeple(MeepleEvent ev) {
            if (ev.getType() == MeepleEvent.DEPLOY) {
                operations.addLast(new MeepleDeployedOperation(ev.getMeeple()));
            }
            if (ev.getType() == MeepleEvent.UNDEPLOY) {
                operations.addLast(new MeepleUndeployedOperation(ev.getMeeple()));
            }
        }

        @Subscribe
        public void scored(ScoreEvent ev) {
            operations.addLast(new ScoreOperation(ev.getPlayer(), ev.getPoints()));
        }
    }
}