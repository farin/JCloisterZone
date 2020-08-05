package com.jcloisterzone.engine;

import com.google.gson.Gson;
import com.jcloisterzone.Player;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.*;
import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.message.*;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map.Entry;
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

    private Map<Class<? extends Meeple>, Integer> addMeeples(
            Map<Class<? extends Meeple>, Integer> meeples, GameSetupMessage setupMsg, String key, Class<? extends Meeple> cls) {
        Object cnt = setupMsg.getElements().get(key);
        if (cnt == null) {
            return meeples;
        }
        int count = Integer.parseInt(cnt.toString().split("\\.")[0]);
        if (count <= 0) {
            return meeples;
        }
        return meeples.put(cls, count);
    }

    private Set<Class<? extends Capability<?>>> addCapabilities(
            Set<Class<? extends Capability<?>>> capabilties, GameSetupMessage setupMsg, String key, Class<? extends Capability<?>> cls) {
        Object value = setupMsg.getElements().get(key);
        if (value == null) {
            return capabilties;
        }
        return capabilties.add(cls);
    }

    private GameSetup createSetupFromMessage(GameSetupMessage setupMsg) {
        Map<Class<? extends Meeple>, Integer> meeples = HashMap.empty();
        meeples = addMeeples(meeples, setupMsg, "small-follower", SmallFollower.class);
        // meeples = addMeeples(meeples, setupMsg, "abbot", Abbot.class);
        meeples = addMeeples(meeples, setupMsg, "phantom", Phantom.class);
        meeples = addMeeples(meeples, setupMsg, "big-follower", BigFollower.class);
        meeples = addMeeples(meeples, setupMsg, "builder", Builder.class);
        meeples = addMeeples(meeples, setupMsg, "pig", Pig.class);
        meeples = addMeeples(meeples, setupMsg, "barn", Barn.class);
        meeples = addMeeples(meeples, setupMsg, "wagon", Wagon.class);
        meeples = addMeeples(meeples, setupMsg, "mayor", Mayor.class);
        meeples = addMeeples(meeples, setupMsg, "shepherd", Shepherd.class);

        Set<Class<? extends Capability<?>>> capabilities = HashSet.empty();
        capabilities = addCapabilities(capabilities, setupMsg,"barn", BarnCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"builder", BuilderCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"phantom", PhantomCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"shepherd", SheepCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"wagon", WagonCapability.class);

        capabilities = addCapabilities(capabilities, setupMsg,"dragon", DragonCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"fairy", FairyCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"count", CountCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"mage", MageAndWitchCapability.class);

        capabilities = addCapabilities(capabilities, setupMsg,"abbey", AbbeyCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"bridge", BridgeCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"castle", CastleCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"tower", TowerCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"tunnel", TunnelCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"ferry", FerriesCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"little-buildings", LittleBuildingsCapability.class);

        capabilities = addCapabilities(capabilities, setupMsg,"traders", TradeGoodsCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"king", KingCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"robber-baron", RobberBaronCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"gold", GoldminesCapability.class);

        capabilities = addCapabilities(capabilities, setupMsg,"cathedral", CathedralCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"inn", InnCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"princess", PrincessCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"portal", PortalCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"pig-herd", PigHerdCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"bazaar", BazaarCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"hill", HillCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"vineyard", VineyardCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"shrine", ShrineCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"festival", FestivalCapability.class);

        //TODO escape
        // capabilities = addCapabilities(capabilities, setupMsg,"escape", EscapeCapability.class);

        boolean containsRiver = setupMsg.getSets().keySet().stream().anyMatch(s -> s.startsWith("river/"));
        boolean containsCornCircle = setupMsg.getSets().keySet().stream().anyMatch(s -> s.startsWith("corn-circles/"));
        if (containsRiver) {
            capabilities = capabilities.add(RiverCapability.class);
        }
        if (containsCornCircle) {
            capabilities = capabilities.add(CornCircleCapability.class);
        }
        if (setupMsg.getSets().containsKey("darmstadt")) {
            capabilities = capabilities.add(ChurchCapability.class);
        }
        if (setupMsg.getSets().containsKey("labyrinth")) {
            capabilities = capabilities.add(LabyrinthCapability.class);
        }

        //capabilities.forEach(c -> { System.err.println(c.getSimpleName()); });

        // TODO farmers and gardens
        Map<Rule, Object> rules = HashMap.empty();
        if (setupMsg.getElements().containsKey("farmers")) {
            rules = rules.put(Rule.FARMERS, true);
        }
        if (setupMsg.getElements().containsKey("escape")) {
            rules = rules.put(Rule.ESCAPE, true);
        }

        for (Entry<String, Object> entry : setupMsg.getRules().entrySet()) {
            rules = rules.put(Rule.byKey(entry.getKey()), entry.getValue());
        }

        // TODO implement capabilities rules
        GameSetup gameSetup = new GameSetup(
                HashMap.ofAll(setupMsg.getSets()),
                meeples,
                capabilities,
                rules,
                io.vavr.collection.List.ofAll(setupMsg.getStart())
        );
        return gameSetup;
    }

    // sample setup
    // {"type":"GAME_SETUP","payload":{"sets":{"basic":1,"inns-and-cathedrals":1},"elements":{"small-follower":7,"farmers":true,"big-follower":1,"cathedral":true,"inn":true},"rules":{"princess-action":"may","fairy-placement":"next-follower","dragon-move":"before-scoring","barn-placement":"not-occupied","bazaar-auction":false,"hill-tiebreaker":"at-least-one-follower","espace-variant":"any-tile","gq11-pig-herd":"pig","tunnelize-other-expansions":true,"more-tunnel-tokens":"3/2","festival-return":"meeple","keep-monasteries":"replace","labyrinth-variant":"advanced","little-buildings-scoring":"1/1/1","king-and-robber-baron-scoring":"default","tiny-city-scoring":"4"},"timer":null,"start":[{"tile":"BA/RCr","x":0,"y":0,"rotation":0}],"players":[{"state":"local","name":"Grace","slot":5},{"state":"local","name":"Grace","slot":1}]}}
    // {"type":"GAME_SETUP","payload":{"sets":{"basic":1,"river/1":1},"elements":{"small-follower":7,"farmers":true,"big-follower":1,"cathedral":true,"inn":true},"rules":{"princess-action":"may","fairy-placement":"next-follower","dragon-move":"before-scoring","barn-placement":"not-occupied","bazaar-auction":false,"hill-tiebreaker":"at-least-one-follower","espace-variant":"any-tile","gq11-pig-herd":"pig","tunnelize-other-expansions":true,"more-tunnel-tokens":"3/2","festival-return":"meeple","keep-monasteries":"replace","labyrinth-variant":"advanced","little-buildings-scoring":"1/1/1","king-and-robber-baron-scoring":"default","tiny-city-scoring":"4"},"timer":null,"start":[{"tile":"RI/s","x":0,"y":0,"rotation":0}],"players":[{"state":"local","name":"Grace","slot":5},{"state":"local","name":"Grace","slot":1}]}}

    // count setup
    // {"type":"GAME_SETUP","payload":{"sets":{"basic":1,"count":1},"elements":{"small-follower":7,"farmers":true,"count":1},"rules":{"princess-action":"may","fairy-placement":"next-follower","dragon-move":"before-scoring","barn-placement":"not-occupied","bazaar-no-auction":false,"hill-tiebreaker":"at-least-one-follower","espace-variant":"any-tile","gq11-pig-herd":"pig","tunnelize-other-expansions":true,"more-tunnel-tokens":"3/2","festival-return":"meeple","keep-monasteries":"replace","labyrinth-variant":"advanced","little-buildings-scoring":"1/1/1","king-and-robber-baron-scoring":"default","tiny-city-scoring":"4"},"timer":null,"start":[{"tile":"CO/1","x":-2,"y":-1,"rotation":0},{"tile":"CO/2","x":-1,"y":-1,"rotation":0},{"tile":"CO/3","x":0,"y":-1,"rotation":0},{"tile":"CO/4","x":1,"y":-1,"rotation":0},{"tile":"CO/5","x":-2,"y":0,"rotation":0},{"tile":"CO/6","x":-1,"y":0,"rotation":0},{"tile":"CO/7","x":0,"y":0,"rotation":0},{"tile":"CO/8","x":1,"y":0,"rotation":0},{"tile":"CO/9","x":-2,"y":1,"rotation":0},{"tile":"CO/10","x":-1,"y":1,"rotation":0},{"tile":"CO/11","x":0,"y":1,"rotation":0},{"tile":"CO/12","x":1,"y":1,"rotation":0}],"players":[{"state":"local","name":"Wendy","slot":2},{"state":"local","name":"Grace","slot":1}]}}

    @Override
    public void run() {
        Config config = new Config();

        initialSeed = random.nextLong();
        // debug seeds
        // initialSeed = -1507029652130839674L; // L
        // initialSeed = 4125305802896227250L; // RR
        // initialSeed = -5589071459783070185L; // CFC.2

        // initialSeed = -6476999185744582589L; // BA.C as first tile
        // initialSeed = 5898276208915289755L; // Princess and Dragon: early volcano + move, + also early princess (in general this provides early non BA tile, works also at least for Tower
        err.println("initial seed is " + initialSeed);

        String line = in.nextLine();
        GameSetupMessage setupMsg = (GameSetupMessage) parser.fromJson(line);

        PlayerSlot[] slots = createPlayerSlots(setupMsg.getPlayers().size());
        GameSetup gameSetup = createSetupFromMessage(setupMsg);
        game = new Game(gameSetup);

        GameStatePhaseReducer phaseReducer = new GameStatePhaseReducer(gameSetup, initialSeed);
        GameStateBuilder builder = new GameStateBuilder(gameSetup, slots, config);

        if (setupMsg.getGameAnnotations() != null) {
            builder.setGameAnnotations(setupMsg.getGameAnnotations());
        }

        GameState state = builder.createInitialState();
        Phase firstPhase = phaseReducer.getFirstPhase();
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
