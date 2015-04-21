package com.jcloisterzone.game.capability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class CornCircleCapability extends Capability {

    public static enum CornCicleOption {
        DEPLOYMENT,
        REMOVAL;
    }

    private Player cornCirclePlayer;
    private CornCicleOption cornCircleOption;

    public CornCircleCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        return new Object[] { cornCirclePlayer, cornCircleOption };
    }

    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        cornCirclePlayer = (Player) a[0];
        cornCircleOption = (CornCicleOption) a[1];
    }


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
    public void saveToSnapshot(Document doc, Element node) {
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
