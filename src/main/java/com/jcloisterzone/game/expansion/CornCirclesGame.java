package com.jcloisterzone.game.expansion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.ExpandedGame;

public class CornCirclesGame extends ExpandedGame {

    public static enum CornCicleOption {
        DEPLOYMENT,
        REMOVAL;
    }

    private Player cornCirclePlayer;
    private CornCicleOption cornCircleOption;

    @Override
    public void initTile(Tile tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("corn-circle");
        if (nl.getLength() > 0) {
            String type = ((Element)nl.item(0)).getAttribute("type");
            Class<? extends Feature> cornCircleType = null;
            if ("Road".equals(type)) cornCircleType = Road.class;
            if ("City".equals(type)) cornCircleType = City.class;
            if ("Farm".equals(type)) cornCircleType = Farm.class;
            if (cornCircleType == null) throw new IllegalArgumentException("Invalid corn cicle type.");
            tile.setCornCircle(cornCircleType);
        }
    }

    public Player getCornCirclePlayer() {
        return cornCirclePlayer;
    }

    public void setCornCirclePlayer(Player cornCirclePlayer) {
        this.cornCirclePlayer = cornCirclePlayer;
    }

    public CornCicleOption getCornCircleOption() {
        return cornCircleOption;
    }

    public void setCornCircleOption(CornCicleOption cornCircleOption) {
        this.cornCircleOption = cornCircleOption;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node, Expansion nodeFor) {
        //don't duplicate arguments
        if (nodeFor == Expansion.CORN_CIRCLES_II && game.hasExpansion(Expansion.CORN_CIRCLES)) return;

        if (cornCircleOption != null) {
            node.setAttribute("selectedOption", cornCircleOption.name());
        }
        if (cornCirclePlayer != null) {
            node.setAttribute("cornPlayer", "" + cornCirclePlayer.getIndex());
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        if (node.hasAttribute("selectedOption")) {
            cornCircleOption =  CornCicleOption.valueOf(node.getAttribute("selectedOption"));
        }
        if (node.hasAttribute("cornPlayer")) {
            Player player = game.getPlayer(Integer.parseInt(node.getAttribute("cornPlayer")));
            cornCirclePlayer = player;
        }
    }
}
