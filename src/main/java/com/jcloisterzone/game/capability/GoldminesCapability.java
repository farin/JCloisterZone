package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.event.GoldChangeEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.score.ScoringStrategy;
import com.jcloisterzone.feature.visitor.score.CloisterScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class GoldminesCapability  extends Capability {

    private final Map<Position, Integer> boardGold = new HashMap<>();
    private final Map<Player, Integer> playerGold = new HashMap<>();

    private final Map<Position, Set<Player>> claimedGold = new HashMap<>();

    public GoldminesCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        Object[] a = new Object[2];
        a[0] = new HashMap<>(boardGold);
        a[1] = new HashMap<>(playerGold);
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        Map<Position, Integer> boardBackup = (Map<Position, Integer>) a[0];
        boardGold.clear();
        boardGold.putAll(boardBackup);
        Map<Player, Integer> playerBackup = (Map<Player, Integer>) a[1];
        playerGold.clear();
        playerGold.putAll(playerBackup);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("goldmine").getLength() > 0) {
            tile.setTrigger(TileTrigger.GOLDMINE);
        }
    }

    @Override
    public void initPlayer(Player player) {
        playerGold.put(player, 0);
    }

    public int addGold(Position pos) {
        Integer prev = boardGold.get(pos);
        Integer curr = prev == null ? 1 : prev + 1;
        boardGold.put(pos, curr);
        return curr;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        for (Entry<Position, Integer> entry : boardGold.entrySet()) {
            Element el = doc.createElement("gold");
            node.appendChild(el);
            XmlUtils.injectPosition(el, entry.getKey());
            el.setAttribute("count", "" + entry.getValue());
        }
        for (Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            el.setAttribute("goldPieces", "" + playerGold.get(player));
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("gold");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            Position pos = XmlUtils.extractPosition(el);
            int count = XmlUtils.attributeIntValue(el, "count");
            boardGold.put(pos, count);
            game.post(new GoldChangeEvent(null, pos, count));
        }
        nl = node.getElementsByTagName("player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            int count = XmlUtils.attributeIntValue(playerEl, "goldPieces");
            playerGold.put(player, count);
        }
    }

    @Override
    public void scoreCompleted(CompletableScoreContext ctx) {
        if (ctx.getMajorOwners().isEmpty()) return;
        Set<Position> positions;
        if (ctx instanceof CloisterScoreContext) {
            positions = new HashSet<>();
            Position cloisterPos = ((CloisterScoreContext) ctx).getMasterFeature().getTile().getPosition();
            positions.add(cloisterPos);
            positions.addAll(Lists.transform(getBoard().getAdjacentAndDiagonalTiles(cloisterPos), new Function<Tile, Position>() {
                @Override
                public Position apply(Tile t) {
                    return t.getPosition();
                }
            }));
        } else {
            positions = ctx.getPositions();
        }
        registerClaim(positions, ctx.getMajorOwners());
    }

    public void castleCompleted(Castle castle, Player p) {
        Set<Position> positions = new HashSet<>();
        Collections.addAll(positions, castle.getVicinity());
        registerClaim(positions, Collections.singleton(p));
    }

    private void registerClaim(Set<Position> positions, Set<Player> claimingPlayers) {
        for (Position pos : positions) {
            if (boardGold.containsKey(pos)) {
                Set<Player> players = claimedGold.get(pos);
                if (players == null) {
                    players = new HashSet<>();
                    claimedGold.put(pos, players);
                }
                players.addAll(claimingPlayers);
            }
        }
    }

    public int getPlayerGoldPieces(Player player) {
        Integer pieces = playerGold.get(player);
        return pieces == null ? 0 : pieces;
    }

    private int getGoldPiecesOntile(Position pos) {
        Integer pieces = boardGold.get(pos);
        return pieces == null ? 0 : pieces;
    }

    public void awardGoldPieces() {
        List<Entry<Position, Set<Player>>> entries = new ArrayList<>(claimedGold.entrySet());
        Collections.sort(entries, new Comparator<Entry<Position, Set<Player>>>() {
            @Override
            public int compare(Entry<Position, Set<Player>> o1, Entry<Position, Set<Player>> o2) {
                return o2.getValue().size() - o1.getValue().size();
            }
        });
        int goldPieces = 0;
        for (Position pos : claimedGold.keySet()) {
            goldPieces += boardGold.get(pos);
        }
        Player player = game.getActivePlayer();
        while (goldPieces > 0) {
            for (Entry<Position, Set<Player>> entry: entries) {
                Position pos = entry.getKey();
                Set<Player> claimingPlayers = entry.getValue();
                int piecesOnTile = getGoldPiecesOntile(pos);
                if (piecesOnTile > 0 && claimingPlayers.contains(player)) {
                    goldPieces--;
                    playerGold.put(player, playerGold.get(player) + 1);
                    if (piecesOnTile == 1) {
                        boardGold.remove(pos);
                    } else {
                        boardGold.put(pos, piecesOnTile - 1);
                    }
                }
            }
            player = game.getNextPlayer(player);
        }
        for (Position pos : claimedGold.keySet()) {
            game.post(new GoldChangeEvent(null, pos, 0));
        }
        claimedGold.clear();
    }

    @Override
    public void finalScoring(ScoringStrategy strategy) {
        for (Player player: game.getAllPlayers()) {
            int pieces = getPlayerGoldPieces(player);
            if (pieces == 0) continue;
            int points = 0;
            if (pieces < 4) points = 1 * pieces;
            else if (pieces < 7) points = 2 * pieces;
            else if (pieces < 10) points = 3 * pieces;
            else points = 4 * pieces;
            strategy.addPoints(player, points, PointCategory.GOLD);
        }
    }
}
