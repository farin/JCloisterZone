package com.jcloisterzone.board;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Snapshot;


public class LoadGameTilePackFactory extends TilePackFactory {

    public static final String PLACED_GROUP = "placed";

    private Snapshot snapshot;

    private List<Meeple> preplacedMeeples = new ArrayList<>();

    //TODO is required to store information from snapshot outside ?
    class PreplacedTile {
        String tileId;
        Position pos;
        Rotation rot;
        Tile tile;
        Element element;
    }
    PreplacedTile[] preplaced;

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;

        NodeList nl = snapshot.getTileElements();
        preplaced = new PreplacedTile[nl.getLength()];

        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            preplaced[i] = new PreplacedTile();
            preplaced[i].tileId = el.getAttribute("name");
            preplaced[i].pos = XmlUtils.extractPosition(el);
            preplaced[i].rot = snapshot.extractTileRotation(el);
            preplaced[i].element = el;
            preplacedMeeples.addAll(snapshot.extractTileMeeples(el, game, preplaced[i].pos));
        }
    }


    @Override
    public LinkedList<Position> getPreplacedPositions(String tileId, Element card) {
        return null;
    }

    @Override
    public List<Tile> createTiles(Expansion expansion, String tileId, Element card, Map<String, Integer> discardList) {
        List<Tile> result =  super.createTiles(expansion, tileId, card, discardList);
        for (PreplacedTile pt : preplaced) {
            if (pt.tile == null && pt.tileId.equals(tileId)) {
                pt.tile = result.remove(result.size()-1);
                game.loadTileFromSnapshot(pt.tile, pt.element);
            }
            if (result.isEmpty()) {
                break;
            }
        }
        return result;
    }

    public List<Meeple> getPreplacedMeeples() {
        return preplacedMeeples;
    }

    @Override
    protected String getTileGroup(Tile tile, Element card) {
        if (tile.getPosition() != null) {
            return PLACED_GROUP; //special placed group (because all placed must be in active group)
        }
        return super.getTileGroup(tile, card);
    }

    @Override
    public DefaultTilePack createTilePack() {
        DefaultTilePack pack = super.createTilePack();
        for (PreplacedTile pt : preplaced) {
            pt.tile.setRotation(pt.rot);
            pt.tile.setPosition(pt.pos);
            pack.addTile(pt.tile, PLACED_GROUP);
        }
        pack.activateGroup(PLACED_GROUP);
        if (preplaced.length > 0) {
            game.setCurrentTile(preplaced[preplaced.length-1].tile);
        }
        return pack;
    }

    public void activateGroups(DefaultTilePack pack) {
        pack.deactivateAllGroups();
        for (String group : snapshot.getActiveGroups()) {
            if (pack.getGroups().contains(group)) {
                pack.activateGroup(group);
            }
        }
    }

}
