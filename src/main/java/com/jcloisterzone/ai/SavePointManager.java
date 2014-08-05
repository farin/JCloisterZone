package com.jcloisterzone.ai;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.Undoable;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.Phase;

public class SavePointManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Game game;
    protected Deque<Undoable> operations = new ArrayDeque<Undoable>();
    private OperationRecorder operationRecorder = new OperationRecorder();

    private static final boolean DEBUG_VERIFY_SAVE_POINT = !false;


    public SavePointManager(Game game) {
        this.game = game;
        if (DEBUG_VERIFY_SAVE_POINT) {
            logger.warn("Save point verification is enabled!");
        }
    }

    public Game getGame() {
        return game;
    }

    public void startRecording() {
        game.flushEventQueue();
        game.getEventBus().register(operationRecorder);
    }

    public void stopRecording() {
        game.flushEventQueue();
        game.getEventBus().unregister(operationRecorder);
        operations.clear();
    }

    public SavePoint save() {
        Object[] backups = new Object[game.getCapabilities().size()];
        int i = 0;
        for (Capability cap : game.getCapabilities()) {
            backups[i++] = cap.backup();
        }
        SavePoint sp = new SavePoint(operations.peekLast(), game.getPhase(), backups);
        if (DEBUG_VERIFY_SAVE_POINT) {
            sp.setSnapshot(new Snapshot(game));
        }
        return sp;
    }

    public void restore(SavePoint sp) {
        game.flushEventQueue();
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

        if (DEBUG_VERIFY_SAVE_POINT) {
            try {
                String sRestore = new Snapshot(game).saveToString();
                String sSave = sp.getSnapshot().saveToString();
                if (!sSave.equals(sRestore)) {
                    System.err.println("--- saved save point---");
                    System.err.println(sSave);
                    System.err.println("--- doesn't match restored saved point ---");
                    System.err.println(sRestore);
                    System.err.println("-------");
                }
            } catch (IOException | TransformerException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    class OperationRecorder  {
        @Subscribe
        public void record(Undoable u) {
            operations.addLast(u);
        }
    }
}