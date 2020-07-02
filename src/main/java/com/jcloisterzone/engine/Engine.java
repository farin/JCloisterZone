package com.jcloisterzone.engine;

import com.google.gson.Gson;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.GameStatePhaseReducer;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.StandardGameCapability;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.message.*;

import java.io.InputStream;
import java.io.PrintStream;
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

    private GameSetup createSetupFromMessage(GameSetupMessage setupMsg) {
        // TODO implement capabilities rules
        GameSetup gameSetup = new GameSetup(
                io.vavr.collection.HashMap.ofAll(setupMsg.getSets()),
                //io.vavr.collection.HashSet.of(StandardGameCapability.class, BridgeCapability.class),
                io.vavr.collection.HashSet.of(StandardGameCapability.class),
                Rule.getDefaultRules(),
                io.vavr.collection.List.ofAll(setupMsg.getStart())
        );
        return gameSetup;
    }

    // sample setup
    // {"type":"GAME_SETUP","payload":{"sets":{"basic":1,"inns-and-cathedrals":1},"elements":{"small-follower":7,"farmers":true,"big-follower":1,"cathedral":true,"inn":true},"rules":{"princess-action":"may","fairy-placement":"next-follower","dragon-move":"before-scoring","barn-placement":"not-occupied","bazaar-auction":false,"hill-tiebreaker":"at-least-one-follower","espace-variant":"any-tile","gq11-pig-herd":"pig","tunnelize-other-expansions":true,"more-tunnel-tokens":"3/2","festival-return":"meeple","keep-monasteries":"replace","labyrinth-variant":"advanced","little-buildings-scoring":"1/1/1","king-and-robber-baron-scoring":"default","tiny-city-scoring":"4"},"timer":null,"start":[{"tile":"BA/RCr","x":0,"y":0,"rotation":0}],"players":[{"state":"local","name":"Grace","slot":5},{"state":"local","name":"Grace","slot":1}]}}

    @Override
    public void run() {
        Config config = new Config();

        initialSeed = random.nextLong();
        // debug seeds
        // initialSeed = 4125305802896227250L; // RR
        // initialSeed = -5589071459783070185L; // CFC.2
        err.println("initial seed is " + initialSeed);

        String line = in.nextLine();
        GameSetupMessage setupMsg = (GameSetupMessage) parser.fromJson(line);

        PlayerSlot[] slots = createPlayerSlots(setupMsg.getPlayers().size());
        GameSetup gameSetup = createSetupFromMessage(setupMsg);
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
            line = in.nextLine();
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
