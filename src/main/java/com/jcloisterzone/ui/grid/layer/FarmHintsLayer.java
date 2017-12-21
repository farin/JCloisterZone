package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Area;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.ResourceManager;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

public class FarmHintsLayer extends AbstractGridLayer {

    static class FarmHint {
        public Farm farm;
        public Area area;
        public Area scaledArea;
        public List<Color> colors;


        public FarmHint(Farm farm, Area area, List<Color> colors) {
            this.farm = farm;
            this.area = area;
            this.colors = colors;
        }
    }

    static class FarmHintsLayerModel {
        List<FarmHint> hints = List.empty();
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final AlphaComposite HINT_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);

    private FarmHintsLayerModel model = new FarmHintsLayerModel();


    public FarmHintsLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        if (ev.hasMeeplesChanged() || ev.hasPlacedTilesChanged()) {
            model = createModel(ev.getCurrentState());
            gridPanel.repaint();
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        if (!visible) return;

        Composite old = g2.getComposite();
        g2.setComposite(HINT_ALPHA_COMPOSITE);
        TextureFactory textures = new TextureFactory(getTileWidth());

        for (FarmHint fh : model.hints) {
            if (fh.scaledArea == null) {
                fh.scaledArea = fh.area.createTransformedArea(getZoomScale());
            }
            if (fh.colors.length() > 1) {
                g2.setPaint(textures.createMultiColor(fh.colors.toJavaArray(Color.class)));
            } else {
                g2.setPaint(textures.create(fh.colors.get()));
            }
            g2.fill(fh.scaledArea);
        }
        g2.setPaint(null);
        g2.setComposite(old);
    }

    private FarmHintsLayerModel createModel(GameState state) {
        ResourceManager rm = gc.getClient().getResourceManager();

        FarmHintsLayerModel model = new FarmHintsLayerModel();
        model.hints = state.getFeatures(Farm.class)
            .map(farm -> new Tuple2<>(farm, farm.getOwners(state)))
            .filter(t -> {
                Farm farm = t._1;
                //don't display unimportant farms
                if (t._2.isEmpty()) {
                    boolean hasCity = !farm.getAdjoiningCities().isEmpty() || farm.isAdjoiningCityOfCarcassonne();
                    return farm.getPlaces().size() > 1 && hasCity;
                }
                return true;
            })
            .map(t -> {
                Farm farm = t._1;
                Area area = getFeatureArea(state, farm);

                List<Color> colors;
                if (t._2.isEmpty()) {
                    colors = List.of(Color.DARK_GRAY);
                } else {
                    colors = Stream.ofAll(t._2).map(p -> p.getColors().getMeepleColor()).toList();
                }
                return new FarmHint(farm, area, colors);
            })
            .toList();

        return model;
    }

    @Override
    public void zoomChanged(int squareSize) {
        for (FarmHint fh : model.hints) {
            fh.scaledArea = null;
        }
    }
}
