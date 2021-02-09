package com.jcloisterzone.engine;

import com.google.gson.Gson;
import com.jcloisterzone.Player;
import com.jcloisterzone.figure.*;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.GameStatePhaseReducer;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.io.MessageParser;
import com.jcloisterzone.io.message.*;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import io.vavr.Predicates;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.jar.Manifest;

public class Engine implements  Runnable {

    @Argument(alias = "p", description = "Use socket connection on given port instead of stdin stdout")
    private static Integer port;

    @Argument(alias = "r", description = "Rerun engine on game end.")
    private static Boolean reload = false;


    private Scanner in;
    private PrintStream out;
    private PrintStream err;
    private PrintStream log;

    private final Gson gson;
    private MessageParser parser = new MessageParser();

    private Game game;
    private long initialSeed;

    private boolean bulk;

    public Engine(InputStream in, PrintStream out, PrintStream err, PrintStream log) {
        this.in = new Scanner(in);
        this.out = out;
        this.err = err;
        this.log = log;

        gson = new StateGsonBuilder().create();
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
        meeples = addMeeples(meeples, setupMsg, "abbot", Abbot.class);
        meeples = addMeeples(meeples, setupMsg, "phantom", Phantom.class);
        meeples = addMeeples(meeples, setupMsg, "big-follower", BigFollower.class);
        meeples = addMeeples(meeples, setupMsg, "builder", Builder.class);
        meeples = addMeeples(meeples, setupMsg, "pig", Pig.class);
        meeples = addMeeples(meeples, setupMsg, "barn", Barn.class);
        meeples = addMeeples(meeples, setupMsg, "wagon", Wagon.class);
        meeples = addMeeples(meeples, setupMsg, "mayor", Mayor.class);
        meeples = addMeeples(meeples, setupMsg, "shepherd", Shepherd.class);

        Set<Class<? extends Capability<?>>> capabilities = HashSet.empty();
        capabilities = addCapabilities(capabilities, setupMsg,"abbot", AbbotCapability.class);
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
        capabilities = addCapabilities(capabilities, setupMsg,"garden", GardenCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"tower", TowerCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"tunnel", TunnelCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"ferry", FerriesCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"little-buildings", LittleBuildingsCapability.class);

        capabilities = addCapabilities(capabilities, setupMsg,"traders", TradeGoodsCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"king", KingCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"robber", RobberCapability.class);
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

        boolean containsRiver = setupMsg.getSets().keySet().stream().anyMatch(s -> s.startsWith("river/"));
        boolean containsCornCircle = setupMsg.getSets().keySet().stream().anyMatch(s -> s.startsWith("corn-circles/"));
        boolean containsSiege = setupMsg.getSets().keySet().stream().anyMatch(s -> s.startsWith("siege/"));
        boolean containsFlier = setupMsg.getSets().keySet().stream().anyMatch(s -> s.startsWith("flier:"));
        if (containsRiver) {
            capabilities = capabilities.add(RiverCapability.class);
        }
        if (containsCornCircle) {
            capabilities = capabilities.add(CornCircleCapability.class);
        }
        if (containsSiege) {
            capabilities = capabilities.add(SiegeCapability.class);
        }
        if (containsFlier) {
            capabilities = capabilities.add(FlierCapability.class);
        }
        if (setupMsg.getSets().containsKey("darmstadt")) {
            capabilities = capabilities.add(ChurchCapability.class);
        }
        if (setupMsg.getSets().containsKey("labyrinth")) {
            capabilities = capabilities.add(LabyrinthCapability.class);
        }
        if (setupMsg.getSets().containsKey("wind-roses")) {
            capabilities = capabilities.add(WindRoseCapability.class);
        }
        if (setupMsg.getSets().containsKey("monasteries")) {
            capabilities = capabilities.add(MonasteriesCapability.class);
        }
        if (setupMsg.getSets().containsKey("russian-promos/2013")) {
            capabilities = capabilities.add(YagaCapability.class);
        }
        if (setupMsg.getSets().containsKey("russian-promos/2016")) {
            capabilities = capabilities.add(RussianPromosTrapCapability.class);
        }

        Map<Rule, Object> rules = HashMap.empty();
        if (setupMsg.getElements().containsKey("farmers")) {
            rules = rules.put(Rule.FARMERS,true);
        }
        if (setupMsg.getElements().containsKey("escape")) {
            rules = rules.put(Rule.ESCAPE, true);
        }

        for (Entry<String, Object> entry : setupMsg.getRules().entrySet()) {
            rules = rules.put(Rule.byKey(entry.getKey()), entry.getValue());
        }

        GameSetup gameSetup = new GameSetup(
                HashMap.ofAll(setupMsg.getSets()),
                meeples,
                capabilities,
                rules,
                io.vavr.collection.List.ofAll(setupMsg.getStart())
        );
        return gameSetup;
    }

    private void parseDirective(String line) {
        if (line.equals("%bulk on")) {
            bulk = true;
        } else if (line.equals("%bulk off")) {
            bulk = false;
            out.println(gson.toJson(game));
        }
    }


    @Override
    public void run() {
        String line;

        while (true) {
            line = in.nextLine();
            if (log != null) {
                log.println(line);
            }

            if (line.charAt(0) != '%') {
                break;
            }
            parseDirective(line);
        }

        GameSetupMessage setupMsg = (GameSetupMessage) parser.fromJson(line);
        initialSeed = Long.valueOf(setupMsg.getInitialSeed());

        GameSetup gameSetup = createSetupFromMessage(setupMsg);
        game = new Game(gameSetup);

        GameStatePhaseReducer phaseReducer = new GameStatePhaseReducer(gameSetup, initialSeed);
        GameStateBuilder builder = new GameStateBuilder(gameSetup, setupMsg.getPlayers());

        if (setupMsg.getGameAnnotations() != null) {
            builder.setGameAnnotations(setupMsg.getGameAnnotations());
        }

        GameState state = builder.createInitialState();
        Phase firstPhase = phaseReducer.getFirstPhase();
        state = state.setPhase(firstPhase.getClass());
        state = phaseReducer.applyStepResult(firstPhase.enter(state));
        game.replaceState(state);

        if (!bulk) {
            out.println(gson.toJson(game));
        }

        boolean gameIsOver = false;
        while (!gameIsOver) {
            try {
                line = in.nextLine();
            } catch (NoSuchElementException ex) {
                break;
            }
            if (line.length() == 0) {
                break;
            }

            if (log != null) {
                log.println(line);
            }

            if (line.charAt(0) == '%') {
                parseDirective(line);
                continue;
            }

            Message msg = parser.fromJson(line);
            Player oldActivePlayer = state.getActivePlayer();

            if (msg instanceof ReplayableMessage) {
                if (msg instanceof SaltMessage) {
                    SaltMessage saltedMsg = (SaltMessage) msg;
                    if (saltedMsg.getSalt() != null) {
                        phaseReducer.getRandom().setSalt(Long.valueOf(saltedMsg.getSalt()));
                    }
                }
                state = phaseReducer.apply(state, msg);

                Player newActivePlayer = state.getActivePlayer();
                boolean undoAllowed = (!(msg instanceof SaltMessage) || ((SaltMessage) msg).getSalt() == null)
                        && newActivePlayer != null
                        && newActivePlayer.equals(oldActivePlayer)
                        && !(msg instanceof DeployMeepleMessage && ((DeployMeepleMessage)msg).getMeepleId().contains("shepherd"))
                        && !(msg instanceof MoveNeutralFigureMessage && ((MoveNeutralFigureMessage)msg).getFigureId().contains("dragon"));

                if (undoAllowed) {
                    game.markUndo();
                } else {
                    game.clearUndo();
                }

                game.replaceState(state);
                game.setReplay(game.getReplay().prepend((ReplayableMessage) msg));
            } else if (msg instanceof UndoMessage) {
                game.undo();
                state = game.getState();
            } else {
                throw new IllegalStateException("Unknown message");
            }

            gameIsOver = game.getState().getPhase().equals(GameOverPhase.class);

            if (!bulk || gameIsOver) {
                out.println(gson.toJson(game));
            }
        }
    }

    private static String readVersion() throws IOException {
        String version = null;
        String buildDate = null;
        Enumeration<URL> resources = Engine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            if ("JCloisterZone Game Engine".equals(manifest.getMainAttributes().getValue("Implementation-Title"))) {
                version = manifest.getMainAttributes().getValue("Implementation-Version");
                buildDate = manifest.getMainAttributes().getValue("Release-Date");
            }
        }
        if (version != null && buildDate != null) {
            return String.format("%s (%s)", version, buildDate);
        }
        return "dev-snapshot";
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "--version".equals(args[0])) {
            try {
                System.out.println(readVersion());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }

        Engine engine;

        Args.parseOrExit(Engine.class, args);

        do {

            if (port != null) {
                System.out.println("Listening on port " + port);
                ServerSocket server = new ServerSocket(port);
                Socket socket = server.accept();
                engine = new Engine(socket.getInputStream(), new PrintStream(socket.getOutputStream()), System.err, System.out);
            } else {
                engine = new Engine(System.in, System.out, System.err, null);
            }

            try {
                engine.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (reload);
    }
}
