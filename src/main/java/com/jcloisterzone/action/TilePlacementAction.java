package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.controls.ActionPanel;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;

public class TilePlacementAction extends PlayerAction<TilePlacement> {

    private final Tile tile;

    //HACK should be here?, used only for getImage
    private Rotation tileRotation = Rotation.R0;

    public TilePlacementAction(Tile tile) {
        super("tileplacement");
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public Rotation getTileRotation() {
        return tileRotation;
    }
    
    public Map<Position, Set<Rotation>> groupByPosition() {
    	Map<Position, Set<Rotation>> map = new HashMap<>();
    	for (TilePlacement tp: options) {
    		Set<Rotation> rotations = map.get(tp.getPosition());
    		if (rotations == null) {
    			rotations = new HashSet<>();
    			map.put(tp.getPosition(), rotations);
    		}
    		rotations.add(tp.getRotation());
    	}
    	return map;
    }
    
    //TODO direct implementation
    public Set<Rotation> getRotations(Position p) {
    	return groupByPosition().get(p);
    }


    @Override
    public Image getImage(Player player, boolean active) {
        Image img =  client.getResourceManager().getTileImage(tile);
        int w = img.getWidth(null), h = img.getHeight(null);
        BufferedImage bi = UiUtils.newTransparentImage(w, h);
        AffineTransform at = tileRotation.getAffineTransform(w);
        Graphics2D ig = bi.createGraphics();
        ig.drawImage(img, at, null);
        ig.setColor(Color.BLACK);
        ig.drawRect(0, 0, w-1, h-1);
        return bi;
    }

    @Override
    public void perform(Client2ClientIF server, TilePlacement tp) {
        server.placeTile(tp.getRotation(), tp.getPosition());
    }

    @Override
    protected GridLayer createGridLayer() {
        return new TilePlacementLayer(client.getGridPanel(), this);
    }

    @Override
    public void forward() {
        tileRotation = tileRotation.next();
        ActionPanel panel = client.getControlPanel().getActionPanel();
        panel.refreshImageCache();
    }

    @Override
    public void backward() {
        tileRotation = tileRotation.prev();
        ActionPanel panel = client.getControlPanel().getActionPanel();
        panel.refreshImageCache();
    }

}
