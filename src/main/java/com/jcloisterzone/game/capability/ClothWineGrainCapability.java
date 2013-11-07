package com.jcloisterzone.game.capability;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class ClothWineGrainCapability extends Capability {

    private final Map<Player, int[]> tradeResources = new HashMap<>();

    public ClothWineGrainCapability(final Game game) {
        super(game);
    }

    @Override
    public void completed(Completable feature, CompletableScoreContext ctx) {
        if (feature instanceof City) {
            int cityTradeResources[] = ((CityScoreContext)ctx).getCityTradeResources();
            if (cityTradeResources != null) {
                int playersTradeResources[] = tradeResources.get(game.getActivePlayer());
                for (int i = 0; i < cityTradeResources.length; i++) {
                    playersTradeResources[i] += cityTradeResources[i];
                }
            }
        }
    }

    @Override
    public Object backup() {
        return new HashMap<>(tradeResources);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        tradeResources.clear();
        tradeResources.putAll((Map<Player, int[]>) data);
    }

    @Override
    public void initPlayer(Player player) {
        tradeResources.put(player, new int[TradeResource.values().length]);
    }

    public void addTradeResources(Player p, TradeResource res, int n) {
        tradeResources.get(p)[res.ordinal()] += n;
    }

    public int getTradeResources(Player p, TradeResource res) {
        return tradeResources.get(p)[res.ordinal()];
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof City && xml.hasAttribute("resource")) {
            City city = (City) feature;
            String val = xml.getAttribute("resource");
            city.setTradeResource(TradeResource.valueOf(val.toUpperCase()));
        }
    }


    @Override
    public void finalScoring() {
        for (TradeResource tr : TradeResource.values()) {
            int hiVal = 1;
            for (Player player: game.getAllPlayers()) {
                int playerValue = getTradeResources(player, tr);
                if (playerValue > hiVal) {
                    hiVal = playerValue;
                }
            }
            for (Player player: game.getAllPlayers()) {
                int playerValue = getTradeResources(player, tr);
                if (playerValue == hiVal) {
                    player.addPoints(10, PointCategory.TRADE_GOODS);
                }
            }

        }
    }



    @Override
    public void saveToSnapshot(Document doc, Element node) {
        for (Player player: game.getAllPlayers()) {
            Element el = doc.createElement("player");
            node.appendChild(el);
            el.setAttribute("index", "" + player.getIndex());
            el.setAttribute("grain", "" + getTradeResources(player, TradeResource.GRAIN));
            el.setAttribute("wine", "" + getTradeResources(player, TradeResource.WINE));
            el.setAttribute("cloth", "" + getTradeResources(player, TradeResource.CLOTH));
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("player");
        for (int i = 0; i < nl.getLength(); i++) {
            Element playerEl = (Element) nl.item(i);
            Player player = game.getPlayer(Integer.parseInt(playerEl.getAttribute("index")));
            addTradeResources(player, TradeResource.GRAIN, Integer.parseInt(playerEl.getAttribute("grain")));
            addTradeResources(player, TradeResource.WINE, Integer.parseInt(playerEl.getAttribute("wine")));
            addTradeResources(player, TradeResource.CLOTH, Integer.parseInt(playerEl.getAttribute("cloth")));
        }
    }
}
