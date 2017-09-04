package com.jcloisterzone.game.state;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.TilePackBuilder;
import com.jcloisterzone.board.TilePackBuilder.Tiles;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.reducers.PlaceTile;

import io.vavr.Predicates;
import io.vavr.collection.Array;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;


public class GameStateBuilder {

//    private final static class PlayerSlotComparator implements Comparator<PlayerSlot> {
//        @Override
//        public int compare(PlayerSlot o1, PlayerSlot o2) {
//            if (o1.getSerial() == null) {
//                return o2.getSerial() == null ? 0 : 1;
//            }
//            if (o2.getSerial() == null) return -1;
//            if (o1.getSerial() < o2.getSerial()) return -1;
//            if (o1.getSerial() > o2.getSerial()) return 1;
//            return 0;
//        }
//    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final GameSetup setup;
    private final PlayerSlot[] slots;
    private final Config config;

    private Array<Player> players;
    private Seq<PlacedTile> preplacedTiles;
    private Map<String, Object> gameAnnotations;

    private GameState state;


    public GameStateBuilder(GameSetup setup, PlayerSlot[] slots, Config config) {
        this.setup = setup;
        this.slots = slots;
        this.config = config;
    }

    public GameState createInitialState() {
        //temporary code should be configured by player as rules
        io.vavr.collection.List<Capability<?>> capabilities = createCapabilities(setup.getCapabilities());
        createPlayers();

        state = GameState.createInitial(
            setup.getRules(), capabilities, players, 0
        );

        state = state.mapPlayers(ps ->
            ps.setFollowers(
                players.map(p -> createPlayerFollowers(p, capabilities))
            ).setSpecialMeeples(
                players.map(p -> createPlayerSpecialMeeples(p, capabilities))
            )
        );

        createTilePack();

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onStartGame(state);
        }

        //prepareAiPlayers(muteAi);

        state = processGameAnnotations(state);
        return state;
    }

    public GameState createReadyState(GameState state) {
        for (PlacedTile pt : preplacedTiles) {
            state = (new PlaceTile(pt.getTile(), pt.getPosition(), pt.getRotation())).apply(state);
        }
        state = state.appendEvent(new PlayerTurnEvent(PlayEventMeta.createWithoutPlayer(), state.getTurnPlayer()));
        return state;
    }

    /**
     *	Debug helper, allows loading integration tests in UI
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
                TilePack replacement = (TilePack) Class.forName(clsName).getConstructor(LinkedHashMap.class, params.getClass()).newInstance(state.getTilePack().getGroups(), params);
                state = state.setTilePack(replacement);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return state;
    }

    private void createPlayers() {
        this.players = Stream.ofAll(Arrays.asList(slots))
            .filter(Predicates.isNotNull())
            .filter(PlayerSlot::isOccupied)
            .sortBy(PlayerSlot::getSerial)
            .foldLeft(Array.empty(), (arr, slot) ->
               arr.append(new Player(slot.getNickname(), arr.size(), slot))
            );

        if (this.players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }
    }

    private void createTilePack() {
        TilePackBuilder tilePackBuilder = new TilePackBuilder();
        tilePackBuilder.setGameState(state);
        tilePackBuilder.setConfig(config);
        tilePackBuilder.setExpansions(setup.getExpansions());

        Tiles tiles = tilePackBuilder.createTilePack();
        TilePack tilePack = tiles.getTilePack();
        state = state.setTilePack(tilePack);
        preplacedTiles = tiles.getPreplacedTiles();
    }

//    protected void prepareAiPlayers(boolean muteAi) {
//        for (PlayerSlot slot : slots) {
//            if (slot != null && slot.isAi() && slot.isOwn()) {
//                try {
//                    AiPlayer ai = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
//                    ai.setMuted(muteAi);
//                    ai.setGame(game);
//                    ai.setGameController(gc);
//                    for (Player player : game.getState().getPlayers().getPlayers()) {
//                        if (player.getSlot().getNumber() == slot.getNumber()) {
//                            ai.setPlayer(player);
//                            break;
//                        }
//                    }
//                    slot.setAiPlayer(ai);
//                    game.getEventBus().register(ai);
//                    logger.info("AI player created - " + slot.getAiClassName());
//                } catch (Exception e) {
//                    logger.error("Unable to create AI player", e);
//                }
//            }
//        }
//    }

    private Capability<?> createCapabilityInstance(Class<? extends Capability<?>> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create " + clazz.getSimpleName(), e);
        }
    }

    public io.vavr.collection.List<Capability<?>> createCapabilities(io.vavr.collection.Set<Class<? extends Capability<?>>> classes) {
        return io.vavr.collection.List.narrow(
            classes.map(cls -> createCapabilityInstance(cls)).toList()
        );
    }

    private io.vavr.collection.List<Follower> createPlayerFollowers(Player p, Seq<Capability<?>> capabilities) {
        MeepleIdProvider idProvider = new MeepleIdProvider(p);
        Stream<Follower> stream = Stream.range(0, SmallFollower.QUANTITY)
                .map(i -> (Follower) new SmallFollower(idProvider.generateId(SmallFollower.class), p));
        io.vavr.collection.List<Follower> followers = io.vavr.collection.List.ofAll(stream);
        followers = followers.appendAll(capabilities.flatMap(c -> c.createPlayerFollowers(p, idProvider)));
        return followers;
    }

    public Seq<Special> createPlayerSpecialMeeples(Player p, Seq<Capability<?>> capabilities) {
        MeepleIdProvider idProvider = new MeepleIdProvider(p);
        return capabilities.flatMap(c -> c.createPlayerSpecialMeeples(p, idProvider));
    }

    public Map<String, Object> getGameAnnotations() {
        return gameAnnotations;
    }

    public void setGameAnnotations(Map<String, Object> gameAnnotations) {
        this.gameAnnotations = gameAnnotations;
    }
}