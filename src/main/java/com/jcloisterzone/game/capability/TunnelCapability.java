package com.jcloisterzone.game.capability;

import java.awt.Color;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.TunnelPiecePlacedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;

import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

/**
 * Capability model is {@code Map<FeaturePointer, String>} - tunnels,
 * 	 key is tunnel token id or null if nothing is placed
 */
public final class TunnelCapability extends Capability<Map<FeaturePointer, String>> {

    public TunnelCapability() {
//        for (PlayerSlot slot : game.getPlayerSlots()) {
//            if (!slot.isOccupied()) continue;
//            int slotNumber = (slot.getNumber() + 2) % PlayerSlot.COUNT;
//            if (game.getPlayerSlots()[slotNumber].isOccupied()) {
//                slotNumber = (slotNumber + 1) % PlayerSlot.COUNT;
//            }
//            PlayerSlot fakeSlot = new PlayerSlot(slotNumber);
//            //HACK to get second color - TODO fix it!
//            Color tunnelBColor = Client.getInstance().getConfig().getPlayerColor(fakeSlot).getMeepleColor();
//            slot.getColors().setTunnelBColor(tunnelBColor);
//
//        }
    }

    @Override
    public GameState onStartGame(GameState state) {
        int playersCount = state.getPlayers().getPlayers().length();
        boolean moreTokens = state.getBooleanValue(CustomRule.MORE_TUNNEL_TOKENS);
        state = state.mapPlayers(ps -> {
            ps = ps.setTokenCountForAllPlayers(Token.TUNNEL_A, 2);
            if (playersCount == 3 && moreTokens) {
                ps = ps.setTokenCountForAllPlayers(Token.TUNNEL_B, 2);
            }
            if (playersCount < 3) {
                ps = ps.setTokenCountForAllPlayers(Token.TUNNEL_B, 2);
                if (moreTokens) {
                    ps = ps.setTokenCountForAllPlayers(Token.TUNNEL_C, 2);
                }
            }
            return ps;
        });
        state = setModel(state, HashMap.empty());
        return state;
    }

//    public List<Road> getOpenTunnels() {
//        List<Road> openTunnels = new ArrayList<>();
//        for (Road road : tunnels) {
//            if (road.getTile().getPosition() != null && road.isTunnelOpen()) {
//                openTunnels.add(road);
//            }
//        }
//        return openTunnels;
//    }
//
//    public int getTunnelTokens(Player player, boolean isB) {
//        Map<Player, Integer> map = isB ? tunnelTokensB : tunnelTokensA;
//        return map.get(player);
//    }
//
//    public void decreaseTunnelTokens(Player player, boolean isB) {
//        Map<Player, Integer> map = isB ? tunnelTokensB : tunnelTokensA;
//        int tokens = map.get(player);
//        if (tokens == 0) throw new IllegalStateException("Player has no tunnel token");
//        map.put(player, tokens-1);
//    }
//
//    @Override
//    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
//        if (isTunnelUsedThisTurn()) return;
//        List<Road> openTunnels = getOpenTunnels();
//        if (openTunnels.isEmpty()) return;
//
//        List<TunnelAction> tunnelActions = new ArrayList<>(2);
//        if (getTunnelTokens(game.getActivePlayer(), false) > 0) {
//            tunnelActions.add(new TunnelAction(false));
//        }
//        if (getTunnelTokens(game.getActivePlayer(), true) > 0) {
//            tunnelActions.add(new TunnelAction(true));
//        }
//        for (TunnelAction ta : tunnelActions) {
//            for (Road tunnelEnd : openTunnels) {
//                ta.add(new FeaturePointer(tunnelEnd.getTile().getPosition(), tunnelEnd.getLocation()));
//            }
//        }
//        actions.addAll(tunnelActions);
//    }
//
//    public boolean isTunnelUsedThisTurn() {
//        return placedTunnelCurrentTurn != null;
//    }
//    public Road getPlacedTunnel() {
//        return placedTunnelCurrentTurn;
//    }

//    @Override
//    public void turnCleanUp() {
//        placedTunnelCurrentTurn = null;
//    }

//    private int getTunnelId(Player p, boolean isB) {
//        return p.getIndex() + (isB ? 100 : 0);
//    }
//
//    public void placeTunnelPiece(FeaturePointer fp, boolean isB) {
//        Road road = (Road) getBoard().getPlayer(fp);
//        if (!road.isTunnelOpen()) {
//            throw new IllegalStateException("No open tunnel here.");
//        }
//        placedTunnelCurrentTurn = road;
//        Player player = game.getActivePlayer();
//        placeTunnelPiece(road, player, fp, isB);
//    }
//
//    private void placeTunnelPiece(Road road, Player player, FeaturePointer fp, boolean isB) {
//        int connectionId = getTunnelId(player, isB);
//        decreaseTunnelTokens(player, isB);
//        for (Road r : tunnels) {
//            if (r.getTunnelEnd() == connectionId) {
//                r.setTunnelEdge(road);
//                road.setTunnelEdge(r);
//                break;
//            }
//        }
//        road.setTunnelEnd(connectionId);
//        game.post(new TunnelPiecePlacedEvent(player, fp, isB));
//    }
}
