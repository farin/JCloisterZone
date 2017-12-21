package com.jcloisterzone.ui.grid.layer;

import static com.jcloisterzone.ui.resources.ResourceManager.POINT_NORMALIZED_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.game.state.PlacedTunnelToken;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;

public class TokenLayer extends AbstractGridLayer {

    public static class TokenLayerModel {
        Seq<Tuple2<ImmutablePoint, Image>> tunnels = List.empty();
        Seq<Tuple2<ImmutablePoint, Image>> buildings = List.empty();
    }

    private TokenLayerModel model = new TokenLayerModel();

    public TokenLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        Map<FeaturePointer, PlacedTunnelToken> currTunnelModel = ev.getCurrentState().getCapabilityModel(TunnelCapability.class);
        if (currTunnelModel != null) {
            Map<FeaturePointer, PlacedTunnelToken> prevTunnelModel = ev.getPrevState().getCapabilityModel(TunnelCapability.class);
            if (prevTunnelModel != currTunnelModel) {
                 model.tunnels = createTunnelsViewModel(ev.getCurrentState(), currTunnelModel);
            }
        }

        Map<Position, Token> currLbModel = ev.getCurrentState().getCapabilityModel(LittleBuildingsCapability.class);
        if (currLbModel != null) {
            Map<Position, Token> prevLbModel = ev.getPrevState().getCapabilityModel(LittleBuildingsCapability.class);
            if (prevLbModel != currLbModel) {
                model.buildings = createLbViewModel(ev.getCurrentState(), currLbModel);
            }
        }
    }

    private Seq<Tuple2<ImmutablePoint, Image>> createLbViewModel(GameState state, Map<Position, Token> lbState) {
        ImmutablePoint point = new ImmutablePoint(65, 35);
        return lbState.map(t -> {
            Position pos = t._1;
            Token lb = t._2;
            String imgName = lb.name().toLowerCase();
            Image img = rm.getImage("neutral/" + imgName);
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            if (lb == Token.LB_TOWER) {
                img = img.getScaledInstance(w, (int) (h * 0.7), Image.SCALE_SMOOTH);
            }
            return new Tuple2<>(
                point.translate(pos.x * POINT_NORMALIZED_SIZE, pos.y * POINT_NORMALIZED_SIZE),
                img
            );
        });
    }

    private Seq<Tuple2<ImmutablePoint, Image>> createTunnelsViewModel(GameState state, Map<FeaturePointer, PlacedTunnelToken> tunnelsState) {
        return tunnelsState
            .filterValues(Predicates.isNotNull())
            .map(t -> {
                FeaturePointer fp  = t._1;
                Position pos = fp.getPosition();
                PlacedTunnelToken placedTunnel = t._2;
                Player player = state.getPlayers().getPlayer(placedTunnel.getPlayerIndex());
                Color color = player.getSlot().getColors().getTunnelColors().get(t._2.getToken());
                Image img = rm.getLayeredImage(new LayeredImageDescriptor("player-meeples/tunnel", color));
                PlacedTile pt = state.getPlacedTiles().get(pos).get();
                ImmutablePoint point = rm.getMeeplePlacement(pt.getTile(), pt.getRotation(), fp.getLocation());
                // HACK move it closer to center - TODO put precise tunnel location into points.xml
                point = new ImmutablePoint(
                    (int) (point.getX() / 2.0 + POINT_NORMALIZED_SIZE / 4.0),
                    (int) (point.getY() / 2.0 + POINT_NORMALIZED_SIZE / 4.0)
                );
                return new Tuple2<>(
                    point.translate(pos.x * POINT_NORMALIZED_SIZE, pos.y * POINT_NORMALIZED_SIZE),
                    img
                );
            });
    }

    @Override
    public void paint(Graphics2D g2) {
        AffineTransform originalTransform = g2.getTransform();
        int baseSize = getTileWidth();
        AffineTransform scaleTx = getPointZoomScale();
        for (Tuple2<ImmutablePoint, Image> tunnel : Stream.concat(model.tunnels, model.buildings)) {
            ImmutablePoint scaledOffset = tunnel._1.transform(scaleTx);
            Image scaled = tunnel._2.getScaledInstance((int) (baseSize / 2.1), (int) (baseSize / 2.1), Image.SCALE_SMOOTH);
            ImageIcon ico = new ImageIcon(scaled);
            int width = ico.getIconWidth();
            int height = ico.getIconHeight();
            int x = scaledOffset.getX();
            int y = scaledOffset.getY();

            g2.rotate(-gridPanel.getBoardRotation().getTheta(), x, y);
            g2.drawImage(ico.getImage(), x - width / 2, y - height / 2, gridPanel);
            g2.setTransform(originalTransform);
        }
    }
}
