package com.jcloisterzone.ai;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.Undoable;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.Phase;

public class SavePointManager {

    private final Game game;
    protected Deque<Undoable> operations = new ArrayDeque<Undoable>();
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

    public void restore(SavePoint sp) {
        game.getEventBus().unregister(operationRecorder);
        Undoable target = sp.getOperation();
        //assert target == null || operations.contains(target);
        while (operations.peekLast() != target) {
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
        public void record(Undoable u) {
            operations.addLast(u);
        }
    }
}