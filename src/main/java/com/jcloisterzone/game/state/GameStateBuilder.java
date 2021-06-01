package com.jcloisterzone.game.state;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.*;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.io.message.GameSetupMessage.PlacedTileItem;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Seq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


public class GameStateBuilder {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final GameSetup setup;
    private final int playersCount;

    private Array<Player> players;
    private Map<String, Object> gameAnnotations;

    private GameState state;
    java.util.List<String> definitions;


    public GameStateBuilder(java.util.List<String> definitions, GameSetup setup, int playersCount) {
        if (playersCount < 1) {
            throw new IllegalArgumentException("No player in game");
        }
        this.definitions = definitions;
        this.setup = setup;
        this.playersCount = playersCount;
    }

    public GameState createInitialState() {
        //temporary code should be configured by player as rules
        io.vavr.collection.List<Capability<?>> capabilities = createCapabilities(setup.getCapabilities());
        createPlayers();

        state = GameState.createInitial(
            setup.getRules(), setup.getElements(), capabilities, players, 0
        );

        state = state.mapPlayers(ps ->
            ps.setFollowers(
                players.map(p -> createPlayerFollowers(p))
            ).setSpecialMeeples(
                players.map(p -> createPlayerSpecialMeeples(p))
            )
        );

        createTilePack();
        for (PlacedTileItem pt : setup.getStart()) {
            Tuple2<Tile, TilePack> draw = state.getTilePack().drawTile(pt.getTile());
            Rotation rot = Rotation.valueOf("R" + pt.getRotation());
            state = state.setTilePack(draw._2);
            state = (new PlaceTile(draw._1, new Position(pt.getX(), pt.getY()), rot)).apply(state);
        }

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onStartGame(state);
        }

        //prepareAiPlayers(muteAi);

        state = processGameAnnotations(state);
        state = state.appendEvent(new PlayerTurnEvent(PlayEventMeta.createWithoutPlayer(), state.getTurnPlayer()));
        return state;
    }

    /**
     *  Debug helper, allows loading integration tests in UI
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private GameState processGameAnnotations(GameState state) {
        if (gameAnnotations == null) {
            return state;
        }
        Map<String, Object> tilePackAnnotation = (Map) gameAnnotations.get("tilePack");
        if (tilePackAnnotation != null) {
            try {
                String clsName = (String) tilePackAnnotation.get("className");
                Object params = tilePackAnnotation.get("params");
                TilePack replacement = (TilePack) Class.forName(clsName).getConstructor(LinkedHashMap.class, java.util.Map.class).newInstance(state.getTilePack().getGroups(), params);
                state = state.setTilePack(replacement);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return state;
    }

    private void createPlayers() {
        this.players = Array.empty();
        for (int i = 0; i < playersCount; i++) {
            this.players = this.players.append(new Player(i));
        }


    }

    private void createTilePack() {
        TilePackBuilder tilePackBuilder = new TilePackBuilder();
        tilePackBuilder.setGameState(state);
        tilePackBuilder.setTileSets(setup.getTileSets());

        try {
            TilePack tilePack = tilePackBuilder.createTilePack(definitions);
            state = state.setTilePack(tilePack);
        } catch (IOException e) {
            throw new RuntimeException("Can't parse tile definitions", e);
        }
    }

    private Capability<?> createCapabilityInstance(Class<? extends Capability<?>> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create " + clazz.getSimpleName(), e);
        }
    }

    public io.vavr.collection.List<Capability<?>> createCapabilities(io.vavr.collection.Set<Class<? extends Capability<?>>> classes) {
        return io.vavr.collection.List.narrow(
            classes.map(this::createCapabilityInstance).toList()
        );
    }

    private io.vavr.collection.List<Follower> createPlayerFollowers(Player p) {
        MeepleIdProvider idProvider = new MeepleIdProvider(p);
        io.vavr.collection.List<Follower> followers = io.vavr.collection.List.empty();

        for (Tuple2<Class<? extends Meeple>, Integer> t: setup.getMeeples()) {
            if (Follower.class.isAssignableFrom(t._1)) {
                try {
                    for (int i = 0; i < t._2; i++) {
                        Constructor<? extends Meeple> ctor = t._1.getConstructor(String.class, Player.class);
                        followers = followers.append((Follower) ctor.newInstance(idProvider.generateId(t._1), p));
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return followers;
    }

    public Seq<Special> createPlayerSpecialMeeples(Player p) {
        MeepleIdProvider idProvider = new MeepleIdProvider(p);
        io.vavr.collection.List<Special> specials = io.vavr.collection.List.empty();
        for (Tuple2<Class<? extends Meeple>, Integer> t: setup.getMeeples()) {
            if (Special.class.isAssignableFrom(t._1)) {
                try {
                    for (int i = 0; i < t._2; i++) {
                        Constructor<? extends Meeple> ctor = t._1.getConstructor(String.class, Player.class);
                        specials = specials.append((Special) ctor.newInstance(idProvider.generateId(t._1), p));
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return specials;
    }

    public Map<String, Object> getGameAnnotations() {
        return gameAnnotations;
    }

    public void setGameAnnotations(Map<String, Object> gameAnnotations) {
        this.gameAnnotations = gameAnnotations;
    }
}