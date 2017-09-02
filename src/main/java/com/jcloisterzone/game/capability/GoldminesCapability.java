package com.jcloisterzone.game.capability;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.CloisterLike;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoringResult;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

/**
 * Model is map of placed gold tokens.
 */
public class GoldminesCapability  extends Capability<Map<Position, Integer>> {

//    private final Map<Position, Integer> boardGold = new HashMap<>();
//    private final Map<Player, Integer> playerGold = new HashMap<>();
//
//    private final Map<Position, Set<Player>> claimedGold = new HashMap<>();

    @Override
    public TileDefinition initTile(GameState state, TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("goldmine").getLength() > 0) {
            return tile.setTileTrigger(TileTrigger.GOLDMINE);
        }
        return tile;
    }


    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, HashMap.empty());
    }

    private Set<Position> getFeatureClaimPositions(GameState state, Scoreable feature) {
        if (feature instanceof CloisterLike) {
            return state.getAdjacentAndDiagonalTiles(feature.getPlaces().get().getPosition())
                .map(PlacedTile::getPosition)
                .toSet();
        }
        if (feature instanceof Castle) {
            return ((Castle) feature).getVicinity();
        }
        return feature.getTilePositions();
    }

    @Override
    public GameState onCompleted(GameState state, HashMap<Scoreable, ScoringResult> completed) {
        Map<Position, Integer> placedGold = getModel(state);
        java.util.Map<Position, java.util.Set<Player>> claimedGold = new java.util.HashMap<>();

        // collect claims for particular tiles
        for (Tuple2<Scoreable, ScoringResult> t : completed) {
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

        for (Player player : state.getPlayers().getPlayers()) {
            awardedGold.put(player, 0);
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
                   ps.addTokenCount(pl.getIndex(), Token.GOLD, count)
                );
            }
        }

        return state;
    }

//    public void awardGoldPieces() {
//        Map<Position, Integer> initialGoldCount = new HashMap<>();
//        List<Entry<Position, Set<Player>>> entries = new ArrayList<>(claimedGold.entrySet());
//        Collections.sort(entries, new Comparator<Entry<Position, Set<Player>>>() {
//            @Override
//            public int compare(Entry<Position, Set<Player>> o1, Entry<Position, Set<Player>> o2) {
//                return o2.getValue().size() - o1.getValue().size();
//            }
//        });
//        int goldPieces = 0;
//        for (Position pos : claimedGold.keySet()) {
//            int count = boardGold.get(pos);
//            goldPieces += count;
//            initialGoldCount.put(pos, count);
//        }
//        Player player = game.getActivePlayer();
//        while (goldPieces > 0) {
//            for (Entry<Position, Set<Player>> entry: entries) {
//                Position pos = entry.getKey();
//                Set<Player> claimingPlayers = entry.getValue();
//                int piecesOnTile = getGoldPiecesOntile(pos);
//                if (piecesOnTile > 0 && claimingPlayers.contains(player)) {
//                    goldPieces--;
//                    playerGold.put(player, playerGold.get(player) + 1);
//                    if (piecesOnTile == 1) {
//                        boardGold.remove(pos);
//                    } else {
//                        boardGold.put(pos, piecesOnTile - 1);
//                    }
//                    break;
//                }
//            }
//            player = game.getNextPlayer(player);
//        }
//        for (Position pos : claimedGold.keySet()) {
//            game.post(new GoldChangeEvent(null, pos, initialGoldCount.get(pos), 0));
//        }
//        claimedGold.clear();
//    }
//
//    @Override
//    public void finalScoring() {
//        for (Player player: game.getAllPlayers()) {
//            int pieces = getPlayerGoldPieces(player);
//            if (pieces == 0) continue;
//            int points = 0;
//            if (pieces < 4) points = 1 * pieces;
//            else if (pieces < 7) points = 2 * pieces;
//            else if (pieces < 10) points = 3 * pieces;
//            else points = 4 * pieces;
//            player.addPoints( points, PointCategory.GOLD);
//        }
//    }
}
