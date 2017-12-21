package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.CastleCreated;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;

import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

public class CastleLayer extends AbstractGridLayer {

    public static class CastleLayerModel {
        List<Edge> castles = List.empty();
    }

    private final Image castleImage;
    private CastleLayerModel model = new CastleLayerModel();


    public CastleLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        castleImage = rm.getImage("neutral/castle");
    }


    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        boolean changed = false;
        for (PlayEvent pe : ev.getPlayEventsSymmetricDifference()) {
            if (pe instanceof CastleCreated) {
                changed = true;
                break;
            }
        }

        if (changed) {
            model = createModel(ev.getCurrentState());
            gridPanel.repaint();
        }
    }

    private CastleLayerModel createModel(GameState state) {
        CastleLayerModel model = new CastleLayerModel();

        model.castles = Stream.ofAll(state.getFeatureMap().values())
            .filter(Predicates.instanceOf(Castle.class))
            .distinct()
            .map(c -> ((Castle)c).getEdge())
            .toList();
        return model;
    }


    @Override
    public void paint(Graphics2D g2) {
        for (Edge edge : model.castles) {
            Position pos = edge.getP1();
            if (edge.isHorizontal()) {
                int size = getTileWidth();
                g2.drawImage(castleImage, getOffsetX(pos), getOffsetY(pos) + size/2, size, size, null);
            } else {
                int size = getTileHeight();

//              AffineTransform at = Rotation.R90.getAffineTransform(size);
//              at.concatenate(AffineTransform.getTranslateInstance(getOffsetX(dc.position) + size/2, getOffsetY(dc.position)));
//              g2.drawImage(castleImage, at, null);

                //TODO rotated
                g2.drawImage(castleImage, getOffsetX(pos) + size/2, getOffsetY(pos), size, size, null);
            }
        }
    }
}
