package com.jcloisterzone.game.state;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.mixins.*;
import io.vavr.collection.*;

import java.io.Serializable;
import java.util.function.Function;

@Immutable
public class GameState implements ActionsMixin, BoardMixin,
        RulesMixin, CapabilitiesMixin, PlayersMixin, EventsMixin,
        FlagsMixin, PlacementsMixin, Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Object> elements;
    private final Map<Rule, Object> rules;

    private final CapabilitiesState capabilities;
    private final PlayersState players;

    private final TilePack tilePack;
    private final Tile drawnTile;

    private final LinkedHashMap<Position, PlacedTile> placedTiles;
    private final List<Tile> discardedTiles;
    private final Map<Position, Map<FeaturePointer, Feature>> featureMap;

    private final NeutralFiguresState neutralFigures;
    private final LinkedHashMap<Meeple, FeaturePointer> deployedMeeples;

    //Flags for marking once per turn actions (like princess, portal, ransom ...)
    private final Set<Flag> flags;

    private final ActionsState playerActions;
    private final Queue<PlayEvent> events;

    private final Phase phase;
    private final int turnNumber;
    private final boolean commited;

    public static GameState createInitial(
            Map<Rule, Object> rules,
            Map<String, Object> elements,
            Seq<Capability<?>> capabilities,
            Array<Player> players,
            int turnPlayerIndex) {
        return new GameState(
            rules,
            elements,
            CapabilitiesState.createInitial(capabilities),
            PlayersState.createInitial(players, turnPlayerIndex),
            null,
            null,
            LinkedHashMap.empty(),
            List.empty(),
            HashMap.empty(),
            new NeutralFiguresState(),
            LinkedHashMap.empty(),
            null,
            HashSet.empty(),
            Queue.empty(),
            null,
            1,
            false
        );
    }

    public GameState(
            Map<Rule, Object> rules,
            Map<String, Object> elements,
            CapabilitiesState capabilities,
            PlayersState players,
            TilePack tilePack, Tile drawnTile,
            LinkedHashMap<Position, PlacedTile> placedTiles,
            List<Tile> discardedTiles, Map<Position, Map<FeaturePointer, Feature>> featureMap,
            NeutralFiguresState neutralFigures,
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples,
            ActionsState playerActions,
            Set<Flag> flags,
            Queue<PlayEvent> events,
            Phase phase,
            int turnNumber,
            boolean commited) {
        this.rules = rules;
        this.elements = elements;
        this.capabilities = capabilities;
        this.players = players;
        this.tilePack = tilePack;
        this.drawnTile = drawnTile;
        this.placedTiles = placedTiles;
        this.discardedTiles = discardedTiles;
        this.featureMap = featureMap;
        this.neutralFigures = neutralFigures;
        this.deployedMeeples = deployedMeeples;
        this.playerActions = playerActions;
        this.flags = flags;
        this.events = events;
        this.phase = phase;
        this.turnNumber = turnNumber;
        this.commited = commited;
    }

    @Override
    public GameState setCapabilities(CapabilitiesState capabilities) {
        if (capabilities == this.capabilities) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    @Override
    public GameState setPlayers(PlayersState players) {
        if (players == this.players) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState setTilePack(TilePack tilePack) {
        if (tilePack == this.tilePack) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState mapTilePack(Function<TilePack, TilePack> fn) {
        return setTilePack(fn.apply(tilePack));
    }

    public GameState setDrawnTile(Tile drawnTile) {
        if (drawnTile == this.drawnTile) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    @Override
    public GameState setPlacedTiles(LinkedHashMap<Position, PlacedTile> placedTiles) {
        if (placedTiles == this.placedTiles) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    @Override
    public GameState setFeatureMap(Map<Position, Map<FeaturePointer, Feature>> featureMap) {
        if (featureMap == this.featureMap) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState setDiscardedTiles(List<Tile> discardedTiles) {
        if (discardedTiles == this.discardedTiles) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState setNeutralFigures(NeutralFiguresState neutralFigures) {
        if (neutralFigures == this.neutralFigures) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState mapNeutralFigures(Function<NeutralFiguresState, NeutralFiguresState> fn) {
        return setNeutralFigures(fn.apply(neutralFigures));
    }

    public GameState setDeployedMeeples(LinkedHashMap<Meeple, FeaturePointer> deployedMeeples) {
        if (deployedMeeples == this.deployedMeeples) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    @Override
    public GameState setPlayerActions(ActionsState playerActions) {
        if (playerActions == this.playerActions) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState mapPlayerActions(Function<ActionsState, ActionsState> fn) {
        return setPlayerActions(fn.apply(playerActions));
    }

    @Override
    public GameState setFlags(Set<Flag> flags) {
        if (flags == this.flags) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    @Override
    public GameState setEvents(Queue<PlayEvent> events) {
        if (events == this.events) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState setPhase(Phase phase) {
        if (phase == this.phase) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState setTurnNumber(int turnNumber) {
        if (turnNumber == this.turnNumber) return this;
        return new GameState(
            rules, elements, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase, turnNumber, commited
        );
    }

    public GameState setCommited(boolean commited) {
        if (commited == this.commited) return this;
        return new GameState(
                rules, elements, capabilities, players,
                tilePack, drawnTile, placedTiles, discardedTiles,
                featureMap, neutralFigures,
                deployedMeeples, playerActions,
                flags, events,
                phase, turnNumber, commited
        );
    }

    public Map<String, Object> getElements() {
        return elements;
    }

    @Override
    public Map<Rule, Object> getRules() {
        return rules;
    }

    @Override
    public CapabilitiesState getCapabilities() {
        return capabilities;
    }

    @Override
    public PlayersState getPlayers() {
        return players;
    }

    public TilePack getTilePack() {
        return tilePack;
    }

    public Tile getDrawnTile() {
        return drawnTile;
    }

    @Override
    public LinkedHashMap<Position, PlacedTile> getPlacedTiles() {
        return placedTiles;
    }

    public List<Tile> getDiscardedTiles() {
        return discardedTiles;
    }

    @Override
    public Map<Position, Map<FeaturePointer, Feature>> getFeatureMap() {
        return featureMap;
    }

    public NeutralFiguresState getNeutralFigures() {
        return neutralFigures;
    }

    public LinkedHashMap<Meeple, FeaturePointer> getDeployedMeeples() {
        return deployedMeeples;
    }

    @Override
    public ActionsState getPlayerActions() {
        return playerActions;
    }

    @Override
    public Set<Flag> getFlags() {
        return flags;
    }

    @Override
    public Queue<PlayEvent> getEvents() {
        return events;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isCommited() {
        return commited;
    }
}
