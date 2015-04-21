package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.feature.visitor.score.RoadScoreContext;

public class Road extends CompletableFeature {

    private boolean inn = false;
    /*
     * 0 - no tunnel -1 - open tunnel n - player token n+100 - player token B (2
     * players game)
     */
    private int tunnelEnd;
    public static final int OPEN_TUNNEL = -1;

    public boolean isInn() {
        return inn;
    }

    public void setInn(boolean inn) {
        this.inn = inn;
    }

    public int getTunnelEnd() {
        return tunnelEnd;
    }

    public void setTunnelEnd(int tunnelEnd) {
        this.tunnelEnd = tunnelEnd;
    }

    public boolean isTunnelEnd() {
        return tunnelEnd != 0;
    }

    public boolean isTunnelOpen() {
        return tunnelEnd == OPEN_TUNNEL;
    }

    @Override
    public void setLocation(Location location) {
        super.setLocation(location);
        if (isTunnelEnd()) {
            // reallocate - extra edge for tunnel
            edges = new MultiTileFeature[edges.length + 1];
        }
    }

    public void setTunnelEdge(MultiTileFeature f) {
        edges[edges.length - 1] = f;
    }

    @Override
    public RoadScoreContext getScoreContext() {
        return new RoadScoreContext(getGame());
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.ROAD;
    }

    public static String name() {
        return _("Road");
    }
}
