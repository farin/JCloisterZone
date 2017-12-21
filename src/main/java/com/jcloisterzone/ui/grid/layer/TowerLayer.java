package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;


public class TowerLayer extends AbstractGridLayer {

    private final static Color FILL_COLOR = new Color(40,40,40,150);

    public static class TowerLayerModel {
        List<Tuple2<PlacedTile, Tower>> towers = List.empty();
    }

    private TowerLayerModel model = new TowerLayerModel();

    public TowerLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }


    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        boolean towersChanged = false;
        for (PlayEvent pe : ev.getPlayEventsSymmetricDifference()) {
            if (pe instanceof TokenPlacedEvent) {
                TokenPlacedEvent tpe = (TokenPlacedEvent) pe;
                if (tpe.getToken() == Token.TOWER_PIECE) {
                    towersChanged = true;
                    break;
                }
            }
        }

        if (towersChanged) {
            model = createModel(ev.getCurrentState());
        }
    }

    private TowerLayerModel createModel(GameState state) {
        TowerLayerModel model = new TowerLayerModel();

        //Board board = state.getBoard();

        model.towers = Stream.ofAll(state.getFeatureMap())
            .filter(t -> (t._2 instanceof Tower) && ((Tower)t._2).getHeight() > 0)
            .distinctBy(t -> t._2)
            .map(t -> {
                Position pos = t._1.getPosition();
                PlacedTile pt = state.getPlacedTile(pos);
                return new Tuple2<>(pt, (Tower) t._2);
            })
            .toList();
        return model;
    }



    @Override
    public void paint(Graphics2D g2) {
        g2.setColor(FILL_COLOR);
        for (Tuple2<PlacedTile, Tower> t: model.towers) {
            PlacedTile pt = t._1;
            Tower tower = t._2;
            //TODO generate are in create model
            Area ra = rm.getFeatureArea(pt.getTile(), pt.getRotation(), Location.TOWER)
                .translateTo(pt.getPosition())
                .getDisplayArea()
                .createTransformedArea(getZoomScale());
            g2.fill(ra);
            drawAntialiasedTextCenteredNoScale(g2,"" + tower.getHeight(), 22, Position.ZERO,
                    new ImmutablePoint((int)ra.getBounds2D().getCenterX(), (int)ra.getBounds2D().getCenterY()), Color.WHITE, null);
        }
    }
}
