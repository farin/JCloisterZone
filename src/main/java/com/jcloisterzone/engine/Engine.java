package com.jcloisterzone.engine;

import com.github.zafarkhaja.semver.Version;
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
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.Manifest;

public class Engine implements Runnable {

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
    private double initialRandom;

    private boolean bulk;

    private ArrayList<String> tileDefinitions = new ArrayList<>();

    public Engine(InputStream in, PrintStream out, PrintStream err, PrintStream log) {
        this.in = new Scanner(in, "UTF-8");
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
            Set<Class<? extends Capability<?>>> capabilities, GameSetupMessage setupMsg, String key, Class<? extends Capability<?>> cls) {
        Object value = setupMsg.getElements().get(key);
        if (value == null) {
            return capabilities;
        }
        return capabilities.add(cls);
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
        meeples = addMeeples(meeples, setupMsg, "ringmaster", Ringmaster.class);

        meeples = addMeeples(meeples, setupMsg, "obelisk", Obelisk.class);

        Set<Class<? extends Capability<?>>> capabilities = HashSet.empty();
        capabilities = addCapabilities(capabilities, setupMsg,"abbot", AbbotCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"barn", BarnCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"builder", BuilderCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"phantom", PhantomCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"shepherd", SheepCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"wagon", WagonCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"ringmaster", RingmasterCapability.class);

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

        capabilities = addCapabilities(capabilities, setupMsg,"princess", PrincessCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"portal", PortalCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"pig-herd", PigHerdCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"bazaar", BazaarCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"hill", HillCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"vineyard", VineyardCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"shrine", ShrineCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"festival", FestivalCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"big-top", BigTopCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"acrobats", AcrobatsCapability.class);

        capabilities = addCapabilities(capabilities, setupMsg,"river", RiverCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"corn-circle", CornCircleCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"siege", SiegeCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"flier", FlierCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"church", ChurchCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"wind-rose", WindRoseCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"monastery", MonasteriesCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"russian-trap", RussianPromosTrapCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"watchtower", WatchtowerCapability.class);

        capabilities = addCapabilities(capabilities, setupMsg,"robbers-son", RobbersSonCapability.class);
        capabilities = addCapabilities(capabilities, setupMsg,"obelisk", ObeliskCapability.class);

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
                HashMap.ofAll(setupMsg.getElements()),
                meeples,
                capabilities,
                rules,
                io.vavr.collection.List.ofAll(setupMsg.getStart())
        );
        return gameSetup;
    }

    private void parseDirective(String line) {
        String[] s = line.split("\\s+", 2);
        var directive = s[0];
        var value = s.length > 1 ? s[1] : null;
        switch (directive) {
            case "%bulk":
                bulk = "on".equals(value);
                if (!bulk) {
                    out.println(gson.toJson(game));
                }
                break;
            case "%compat":
                Version compat = Version.valueOf(value);
//                if (compat.lessThan(Version.valueOf("5.7.0"))) {
//                    compatJavaRandom = true;
//                }
                break;
            case "%load":
                tileDefinitions.add(value);
                break;
            case "%state":
                out.println(gson.toJson(game));
                break;
            default:
                err.println("#unknown directive " + line);
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
        initialRandom = setupMsg.getInitialRandom();

        GameSetup gameSetup = createSetupFromMessage(setupMsg);
        game = new Game(gameSetup);

        GameStatePhaseReducer phaseReducer = new GameStatePhaseReducer(gameSetup, initialRandom);
        GameStateBuilder builder = new GameStateBuilder(tileDefinitions, gameSetup, setupMsg.getPlayers());

        if (setupMsg.getGameAnnotations() != null) {
            builder.setGameAnnotations(setupMsg.getGameAnnotations());
        }

        GameState state = builder.createInitialState();
        Phase firstPhase = phaseReducer.getFirstPhase();
        state = state.setPhase(firstPhase);
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
                if (msg instanceof RandomChangingMessage) {
                    RandomChangingMessage rndChangeMsg = (RandomChangingMessage) msg;
                    if (rndChangeMsg.getRandom() != null) {
                        phaseReducer.getRandomGanerator().setRandom(rndChangeMsg.getRandom());
                    }
                }
                state = phaseReducer.apply(state, msg);

                Player newActivePlayer = state.getActivePlayer();
                boolean undoAllowed = (!(msg instanceof RandomChangingMessage) || ((RandomChangingMessage) msg).getRandom() == null)
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

            gameIsOver = game.getState().getPhase() instanceof GameOverPhase;

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

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        } while (reload);
    }
}
