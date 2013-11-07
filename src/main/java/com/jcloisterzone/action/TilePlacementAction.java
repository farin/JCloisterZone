package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.rmi.Client2ClientIF;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.controls.ActionPanel;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;

public class TilePlacementAction extends PlayerAction {

    private final Tile tile;
    private final Map<Position, Set<Rotation>> placements;

    private Rotation tileRotation = Rotation.R0;

    public TilePlacementAction(Tile tile, Map<Position, Set<Rotation>> placements) {
        super("tileplacement");
        this.tile = tile;
        this.placements = placements;
    }

    public Tile getTile() {
        return tile;
    }

    public Rotation getTileRotation() {
        return tileRotation;
    }

    public Map<Position, Set<Rotation>> getAvailablePlacements() {
        return placements;
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

    public void perform(Client2ClientIF server, Rotation rotation, Position p) {
        server.placeTile(rotation, p);
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
