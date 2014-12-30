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
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;
import com.jcloisterzone.wsio.RmiProxy;

public class TilePlacementAction extends PlayerAction<TilePlacement> {

    private final Tile tile;
    private ForwardBackwardListener forwardBackwardDelegate;

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

    public void setTileRotation(Rotation tileRotation) {
		this.tileRotation = tileRotation;
	}

    @Override
    public void forward() {
    	forwardBackwardDelegate.forward();
    }

    @Override
    public void backward() {
    	forwardBackwardDelegate.backward();
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

    public Set<Rotation> getRotations(Position p) {
    	Set<Rotation> rotations = new HashSet<>();
    	for (TilePlacement tp: options) {
    		if (tp.getPosition().equals(p)) {
    			rotations.add(tp.getRotation());
    		}
    	}
        return rotations;
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
    public void perform(RmiProxy server, TilePlacement tp) {
        server.placeTile(tp.getRotation(), tp.getPosition());
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return TilePlacementLayer.class;
    }

    @Override
    public String toString() {
        return "place tile " + tile.getId();
    }

	public ForwardBackwardListener getForwardBackwardDelegate() {
		return forwardBackwardDelegate;
	}

	public void setForwardBackwardDelegate(
			ForwardBackwardListener forwardBackwardDelegate) {
		this.forwardBackwardDelegate = forwardBackwardDelegate;
	}
}
