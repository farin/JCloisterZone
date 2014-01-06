package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.ResourceManager;

public class FarmHintsLayer extends AbstractGridLayer {

    private static final int FULL_SIZE = 300;
    private static final AlphaComposite HINT_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);

    private boolean visible;
    private boolean doRefreshHints;
    final Map<Tile, Map<Location, Area>> areas = new HashMap<>();
    private final List<FarmHint> hints = new ArrayList<>();

    public FarmHintsLayer(GridPanel gridPanel) {
        super(gridPanel);
        refreshHints();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        gridPanel.repaint();
    }

    @Override
    public void paint(Graphics2D g2) {
        if (!visible) return;

        Composite old = g2.getComposite();
        g2.setComposite(HINT_ALPHA_COMPOSITE);
        int sqSize = getSquareSize();
        Double scale = sqSize == FULL_SIZE ? null : (double) sqSize / FULL_SIZE;
        TextureFactory textures = new TextureFactory(sqSize);

        if (doRefreshHints) {
            doRefreshHints = false;
            fillHints();
        }

        for (FarmHint fh : hints) {
            if (fh.scaledArea == null) {
                if (scale == null) {
                    fh.scaledArea = fh.area;
                } else {
                    fh.scaledArea = fh.area.createTransformedArea(AffineTransform.getScaleInstance(scale, scale));
                }
            }
            Area area = transformArea(fh.scaledArea, fh.position);
            if (fh.colors.length > 1) {
                g2.setPaint(textures.createMultiColor(fh.colors));
                g2.fill(area);
            } else {
                g2.setPaint(textures.create(fh.colors[0]));
                g2.fill(area);
            }
        }
        g2.setPaint(null);
        g2.setComposite(old);
    }

    @Override
    public int getZIndex() {
        return 10;
    }

    public void tilePlaced(Tile tile) {
        ResourceManager resourceManager = getClient().getResourceManager();
        Set<Location> farmLocations = new HashSet<>();
        for (Feature f : tile.getFeatures()) {
            if (f instanceof Farm) {
                farmLocations.add(f.getLocation());
            }
        }
        if (farmLocations.isEmpty()) return;
        Map<Location, Area> tAreas = resourceManager.getFeatureAreas(tile, FULL_SIZE, farmLocations);
        areas.put(tile, tAreas);
        refreshHints();
    }

    public void meepleUndeployed(Meeple m) {
        if (m.getFeature() instanceof Farm) {
            refreshHints();
        }
    }

    public void meepleDeployed(Meeple m) {
        if (m.getFeature() instanceof Farm) {
            refreshHints();
        }
    }

    public void refreshHints() {
        doRefreshHints = true;
    }

    private void fillHints() {
        hints.clear();
        final Set<Feature> processed = new HashSet<>();
        for (Entry<Tile, Map<Location, Area>> entry : areas.entrySet()) {
            for (Feature f : entry.getKey().getFeatures()) {
                if (!(f instanceof Farm)) continue;
                if (processed.contains(f)) continue;

                FarmHint fh = f.walk(new FeatureVisitor<FarmHint>() {
                    FarmHint result = new FarmHint(new Area(), null);
                    int x = Integer.MAX_VALUE;
                    int y = Integer.MAX_VALUE;
                    int size = 0;
                    boolean hasCity = false;
                    int[] power = new int[getGame().getAllPlayers().length];

                    @Override
                    public boolean visit(Feature feature) {
                        Farm f = (Farm) feature;
                        processed.add(f);
                        size++;
                        hasCity = hasCity || f.getAdjoiningCities() != null || f.isAdjoiningCityOfCarcassonne();
                        for (Meeple m : f.getMeeples()) {
                            if (m instanceof Follower) {
                                power[m.getPlayer().getIndex()] += ((Follower)m).getPower();
                            }
                            if (m instanceof Barn) {
                                power[m.getPlayer().getIndex()] += 1;
                            }
                        }
                        Position pos = f.getTile().getPosition();
                        if (pos.x < x) {
                            if (x != Integer.MAX_VALUE) result.area.transform(AffineTransform.getTranslateInstance(FULL_SIZE * (x-pos.x), 0));
                            x = pos.x;
                        }
                        if (pos.y < y) {
                            if (y != Integer.MAX_VALUE) result.area.transform(AffineTransform.getTranslateInstance(0, FULL_SIZE * (y-pos.y)));
                            y = pos.y;
                        }
                        Map<Location, Area> tileAreas = areas.get(f.getTile());
                        if (tileAreas != null) { //sync issue, feature can be extended in other thread, so it is not registered in areas yet
                            Area featureArea = new Area(tileAreas.get(f.getLocation()));
                            featureArea.transform(AffineTransform.getTranslateInstance(FULL_SIZE * (pos.x-x), FULL_SIZE*(pos.y-y)));
                            result.area.add(featureArea);
                        }
                        return true;
                    }

                    @Override
                    public FarmHint getResult() {
                        result.position = new Position(x, y);

                        int bestPower = 0;
                        List<Integer> bestPlayerIndexes = new ArrayList<>();
                        for (int i = 0; i < power.length; i++) {
                            if (power[i] == bestPower) {
                                bestPlayerIndexes.add(i);
                            }
                            if (power[i] > bestPower) {
                                bestPower = power[i];
                                bestPlayerIndexes.clear();
                                bestPlayerIndexes.add(i);
                            }
                        }
                        if (bestPower == 0) {
                            if (size < 2 || !hasCity) return null; //don't display unimportant farms
                            result.colors = new Color[] { Color.DARK_GRAY };
                        } else {
                            result.colors = new Color[bestPlayerIndexes.size()];
                            int i = 0;
                            for (Integer index : bestPlayerIndexes) {
                                result.colors[i++] = getGame().getPlayer(index).getColors().getMeepleColor();
                            }
                        }
                        return result;
                    }
                });
                if (fh == null) continue; //to small farm
                hints.add(fh);
            }
        }
    }

    @Override
    public void zoomChanged(int squareSize) {
        for (FarmHint fh : hints) {
            fh.scaledArea = null;
        }
    }

    static class FarmHint {
        public Area area;
        public Area scaledArea;
        public Position position;
        public Color colors[];

        public FarmHint(Area area, Position position) {
            this.area = area;
            this.position = position;
        }
    }

}
