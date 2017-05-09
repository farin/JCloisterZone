package com.jcloisterzone;

public enum PointCategory {

    ROAD(true),
    CITY(true),
    FARM(true),
    CLOISTER(true),
    CASTLE(true),

    TRADE_GOODS(true),
    GOLD(true),
    FAIRY(false),
    TOWER_RANSOM(false),
    BIGGEST_CITY(false),
    LONGEST_ROAD(false),
    BAZAAR_AUCTION(false),
    WIND_ROSE(false);

    /** flag for resolving The Count*/
    boolean landscapeSource;

    PointCategory(boolean landspaceSource) {
        this.landscapeSource = landspaceSource;
    }

    public boolean hasLandscapeSource() {
        return landscapeSource;
    }
}
