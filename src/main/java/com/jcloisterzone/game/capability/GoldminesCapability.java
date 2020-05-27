package com.jcloisterzone.game.capability;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenReceivedEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.CloisterLike;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

/**
 * Model is map of placed gold tokens.
 */
public class GoldminesCapability  extends Capability<Map<Position, Integer>> {

	public static enum GoldToken implements Token {
		GOLD;
	}

	private static final long serialVersionUID = 1L;

	 public static final TileModifier GOLDMINE = new TileModifier("Goldmine");

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "goldmine").isEmpty()) {
            tile = tile.addTileModifier(GOLDMINE);
        }
        return tile;
    }

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, HashMap.empty());
    }

    private Set<Position> getFeatureClaimPositions(GameState state, Scoreable feature) {
        if (feature instanceof CloisterLike) {
            Position cloisterPosition = ((CloisterLike) feature).getPosition();
            return state.getAdjacentAndDiagonalTiles(cloisterPosition)
                .map(PlacedTile::getPosition)
                .append(cloisterPosition) // and add also central tile
                .toSet();
        }
        if (feature instanceof Castle) {
            return ((Castle) feature).getVicinity();
        }
        return feature.getTilePositions();
    }

    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
        Map<Position, Integer> placedGold = getModel(state);
        java.util.Map<Position, java.util.Set<Player>> claimedGold = new java.util.HashMap<>();

        // collect claims for particular tiles
        for (Tuple2<Scoreable, ScoreFeatureReducer> t : completed) {
            Scoreable feature = t._1;
            Set<Player> owners = t._2.getOwners();
            if (owners.isEmpty()) {
                continue;
            }
            for (Position pos : getFeatureClaimPositions(state, feature)) {
                if (placedGold.containsKey(pos)) {
                    java.util.Set<Player> claimsOnTile = claimedGold.get(pos);
                    if (claimsOnTile == null) {
                        claimsOnTile = new java.util.HashSet<>();
                        claimedGold.put(pos, claimsOnTile);
                    }
                    claimsOnTile.addAll(owners.toJavaSet());
                }
            }
        }

        // award gold pieces
        java.util.Map<Position, Integer> initialGoldCount = new java.util.HashMap<>();
        java.util.List<Entry<Position, java.util.Set<Player>>> entries = new java.util.ArrayList<>(claimedGold.entrySet());
        java.util.Map<Player, Integer> awardedGold = new java.util.HashMap<>();
        java.util.Map<Player, java.util.Set<Position>> awardedGoldPositions = new java.util.HashMap<>();

        for (Player player : state.getPlayers().getPlayers()) {
            awardedGold.put(player, 0);
            awardedGoldPositions.put(player, new java.util.HashSet<>());
        }

        // best claim strategy is claim on tiles with the most players - make this automatically for players
        Collections.sort(entries, new Comparator<Entry<Position, java.util.Set<Player>>>() {
            @Override
            public int compare(Entry<Position, java.util.Set<Player>> o1, Entry<Position, java.util.Set<Player>> o2) {
                return o2.getValue().size() - o1.getValue().size();
            }
        });
        int goldPieces = 0;
        for (Position pos : claimedGold.keySet()) {
            int count = placedGold.get(pos).getOrElse(0);
            goldPieces += count;
            initialGoldCount.put(pos, count);
        }
        Player player = state.getTurnPlayer();
        while (goldPieces > 0) {
            for (Entry<Position, java.util.Set<Player>> entry: entries) {
                Position pos = entry.getKey();
                java.util.Set<Player> claimingPlayers = entry.getValue();
                int piecesOnTile = placedGold.get(pos).getOrElse(0);
                if (piecesOnTile > 0 && claimingPlayers.contains(player)) {
                    goldPieces--;
                    awardedGold.put(player, awardedGold.get(player) + 1);
                    awardedGoldPositions.get(player).add(pos);
                    if (piecesOnTile == 1) {
                        placedGold = placedGold.remove(pos);
                    } else {
                        placedGold = placedGold.put(pos, piecesOnTile - 1);
                    }
                    break;
                }
            }
            player = player.getNextPlayer(state);
        }

        state = setModel(state, placedGold);
        for (Entry<Player, Integer> entry : awardedGold.entrySet()) {
            Player pl = entry.getKey();
            int count = entry.getValue();
            if (count > 0) {
                state = state.mapPlayers(ps ->
                   ps.addTokenCount(pl.getIndex(), GoldToken.GOLD, count)
                );
                TokenReceivedEvent ev = new TokenReceivedEvent(
                    PlayEventMeta.createWithActivePlayer(state), pl, GoldToken.GOLD, count
                );
                ev.setSourcePositions(Vector.ofAll(awardedGoldPositions.get(pl)));
                state = state.appendEvent(ev);
            }
        }

        return state;
    }

    @Override
    public GameState onFinalScoring(GameState state) {
        PlayersState ps = state.getPlayers();

        for (Player player: ps.getPlayers()) {
            int pieces = ps.getPlayerTokenCount(player.getIndex(), GoldToken.GOLD);
            if (pieces == 0) {
                continue;
            }
            int points = 0;
            if (pieces < 4) {
                points = 1 * pieces;
            } else if (pieces < 7) {
                points = 2 * pieces;
            } else if (pieces < 10) {
                points = 3 * pieces;
            } else {
                points = 4 * pieces;
            }
            state = (new AddPoints(player, points, PointCategory.GOLD)).apply(state);
        }
        return state;
    }
}
