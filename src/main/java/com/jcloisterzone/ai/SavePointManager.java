package com.jcloisterzone.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jcloisterzone.Player;
import com.jcloisterzone.ai.operation.MeepleDeployedOperation;
import com.jcloisterzone.ai.operation.MeepleUndeployedOperation;
import com.jcloisterzone.ai.operation.Operation;
import com.jcloisterzone.ai.operation.ScoreOperation;
import com.jcloisterzone.ai.operation.TilePlacedOperation;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.GameEventAdapter;
import com.jcloisterzone.event.GameEventListener;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.phase.Phase;

public class SavePointManager {

    private final Game game;
    protected Deque<Operation> operations = new ArrayDeque<Operation>();
    private GameEventListener operationRecorder = new OperationRecorder();

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public SavePointManager(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void startRecording() {
        game.addGameListener(operationRecorder);
    }

    public void stopRecording() {
        game.removeGameListener(operationRecorder);
        operations.clear();
    }

    public SavePoint save() {
        Operation op = operations.isEmpty() ? null : operations.peekLast();
        return new SavePoint(op, game);
    }

    public void restore(SavePoint sp) {
        game.removeGameListener(operationRecorder);
        Operation spOp = sp == null ? null : sp.getOperation();
        assert spOp == null || operations.contains(spOp);
        Operation item;
        while ((item = operations.peekLast()) != null) {
            if (item == spOp) {
                break;
            }
            //logger.info("      < undo {}", item);
            operations.pollLast().undo(game);
        }
        game.getCapabilities().clear();
        //game.getCapabilities().addAll(sp.getSavedCapabilities());
        //TODO is another copy back needed ?
        game.getCapabilities().addAll(Lists.transform(sp.getSavedCapabilities(), new Function<Capability, Capability>() {
            @Override
            public Capability apply(Capability cap) {
                return cap.copy(game);
            }
        }));

        Phase phase = sp.getPhase();
        game.setPhase(phase);
        phase.setEntered(true);
        game.addGameListener(operationRecorder);
    }

    class OperationRecorder extends GameEventAdapter {
        @Override
        public void tilePlaced(Tile tile) {
            operations.addLast(new TilePlacedOperation(tile));
        }
        @Override
        public void deployed(Meeple meeple) {
            operations.addLast(new MeepleDeployedOperation(meeple));
        }
        @Override
        public void undeployed(Meeple meeple) {
            operations.addLast(new MeepleUndeployedOperation(meeple));
        }
//		@Override
//		public void playerActivated(Player turnPlayer, Player activePlayer) {
//			// TODO Auto-generated method stub
//			super.playerActivated(turnPlayer, activePlayer);
//		}
        @Override
        public void scored(Feature feature, int points, String label, Meeple meeple, boolean isFinal) {
            operations.addLast(new ScoreOperation(meeple.getPlayer(), points));
        }
        @Override
        public void scored(Position position, Player player, int points, String label, boolean isFinal) {
            operations.addLast(new ScoreOperation(player, points));
        }
    }
}