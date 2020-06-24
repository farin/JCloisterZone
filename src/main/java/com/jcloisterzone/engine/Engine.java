package com.jcloisterzone.engine;

import com.google.gson.Gson;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.KeyUtils;
import com.jcloisterzone.Player;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.*;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.StandardGameCapability;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.message.*;
import com.jcloisterzone.wsio.server.ServerPlayerSlot;
import io.vavr.Tuple2;
import io.vavr.collection.List;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Engine implements  Runnable {
    private Scanner in;
    private PrintStream out;
    private PrintStream err;

    private final Gson gson;
    private MessageParser parser = new MessageParser();

    private Random random = new Random();


    private Game game;
    private long initialSeed;
    //protected final ServerPlayerSlot[] slots;
    protected int slotSerial;



    public Engine(InputStream in, PrintStream out, PrintStream err) {
        this.in = new Scanner(in);
        this.out = out;
        this.err = err;

        gson = new StateGsonBuilder().create();

        // gameId = KeyUtils.createRandomId();
        initialSeed = random.nextLong();

//        for (int i = 0; i < slots.length; i++) {
//            slots[i] = new ServerPlayerSlot(i);
//        }


    }

    private PlayerSlot[] createPlayerSlots(int count) {
        PlayerSlot[] slots = new PlayerSlot[count];
        int idx = 0;
        for (idx = 0; idx < count; idx++) {
            slots[idx] = new PlayerSlot(idx);
            slots[idx].setNickname(String.format("Player %d", idx+1));
            slots[idx].setSerial(idx + 1);
            slots[idx].setState(PlayerSlot.SlotState.OWN);
        }
        return slots;
    }

    @Override
    public void run() {
        Config config = new Config();
        PlayerSlot[] slots = createPlayerSlots(2);
        // debug seeds
        // initialSeed = 4125305802896227250L; // RR
        initialSeed = -5589071459783070185L; // CFC.2

        GameSetup gameSetup = new GameSetup(
                io.vavr.collection.HashMap.of(Expansion.BASIC, 1),
                //io.vavr.collection.HashSet.of(StandardGameCapability.class, BridgeCapability.class),
                io.vavr.collection.HashSet.of(StandardGameCapability.class),
                Rule.getDefaultRules()
        );
        game = new Game(gameSetup);


        GameStatePhaseReducer phaseReducer = new GameStatePhaseReducer(gameSetup, initialSeed);
        GameStateBuilder builder = new GameStateBuilder(gameSetup, slots, config);
        // builder.setGameAnnotations(...);

        GameState state = builder.createInitialState();
        Phase firstPhase = phaseReducer.getFirstPhase();
        state = builder.createReadyState(state);
        state = state.setPhase(firstPhase.getClass());
        state = phaseReducer.applyStepResult(firstPhase.enter(state));
        game.replaceState(state);

        //out.println(initialSeed);
        out.println(gson.toJson(game));

        while (true) {
            String line = in.nextLine();
            if (line.length() == 0) {
                break;
            }
            //err.println("RECEIVED:" + line);

            WsMessage msg = parser.fromJson(line);
            Player oldActivePlayer = state.getActivePlayer();

            if (msg instanceof WsReplayableMessage) {
                if (msg instanceof WsSaltMessage) {
                    phaseReducer.getRandom().setSalt(((WsSaltMessage) msg).getSalt());
                }
                state = phaseReducer.apply(state, (WsInGameMessage) msg);

                Player newActivePlayer = state.getActivePlayer();
                boolean undoAllowed = !(msg instanceof WsSaltMessage) &&
                        newActivePlayer != null && newActivePlayer.equals(oldActivePlayer);

                if (undoAllowed) {
                    game.markUndo();
                } else {
                    game.clearUndo();
                }

                game.replaceState(state);
                game.setReplay(game.getReplay().prepend((WsReplayableMessage) msg));
            } else if (msg instanceof UndoMessage) {
                game.undo();
                state = game.getState();
            } else {
                throw new IllegalStateException("Unknown message");
            }

            out.println(gson.toJson(game));
        }
    }

    public static void main(String[] args) {
        Engine engine = new Engine(System.in, System.out, System.err);
        engine.run();
    }
}
