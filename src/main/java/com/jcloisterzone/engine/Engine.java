package com.jcloisterzone.engine;

import com.google.gson.Gson;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.KeyUtils;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.GameStatePhaseReducer;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.StandardGameCapability;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;
import com.jcloisterzone.wsio.message.WsSaltMessage;
import com.jcloisterzone.wsio.server.ServerPlayerSlot;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Engine implements  Runnable {
    private Scanner in;
    private PrintStream out;

    private final Gson gson;
    private MessageParser parser = new MessageParser();

    private Random random = new Random();

    private GameSetup gameSetup;
    private String gameId;
    private long initialSeed;
    //protected final ServerPlayerSlot[] slots;
    protected int slotSerial;
    private List<WsReplayableMessage> replay;


    public Engine(InputStream in, PrintStream out) {
        this.in = new Scanner(in);
        this.out = out;

        gson = new StateGsonBuilder().create();

        gameId = KeyUtils.createRandomId();
        initialSeed = random.nextLong();
        replay = new ArrayList<>();
//        for (int i = 0; i < slots.length; i++) {
//            slots[i] = new ServerPlayerSlot(i);
//        }

        gameSetup = new GameSetup(
                io.vavr.collection.HashMap.of(Expansion.BASIC, 1),
                io.vavr.collection.HashSet.of(StandardGameCapability.class, BridgeCapability.class),
                Rule.getDefaultRules()
        );
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
        GameStatePhaseReducer phaseReducer = new GameStatePhaseReducer(gameSetup, initialSeed);
        GameStateBuilder builder = new GameStateBuilder(gameSetup, slots, config);
        // builder.setGameAnnotations(...);

        GameState state = builder.createInitialState();

        Phase firstPhase = phaseReducer.getFirstPhase();
        state = builder.createReadyState(state);
        state = state.setPhase(firstPhase.getClass());
        state = phaseReducer.applyStepResult(firstPhase.enter(state));

        out.println(gson.toJson(state));

        while (true) {
            String line = in.nextLine();
            if (line.length() == 0) {
                break;
            }
            WsMessage msg = parser.fromJson(line);
            if (msg instanceof WsReplayableMessage) {
                if (msg instanceof WsSaltMessage) {
                    phaseReducer.getRandom().setSalt(((WsSaltMessage) msg).getSalt());
                }
                state = phaseReducer.apply(state, (WsInGameMessage) msg);
            }
            out.println(gson.toJson(state));
        }
    }

    public static void main(String[] args) {
        Engine engine = new Engine(System.in, System.out);
        engine.run();
    }

}
