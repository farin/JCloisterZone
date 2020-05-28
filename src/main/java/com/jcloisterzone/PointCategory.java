package com.jcloisterzone;

public enum PointCategory {

    // TODO use untyped string with subtype ?
    // eg.
    // cloister
    // cloister.church
    // cloister.yaga-hut
    // foiry.turn
    // fairy.finished-object

    ROAD(true),
    CITY(true),
    FARM(true),
    CLOISTER(true),
    CASTLE(true),

    TRADE_GOODS(true),
    GOLD(true),
    FAIRY(false),
    SHEEP(false),
    TOWER_RANSOM(false),
    BIGGEST_CITY(false),
    LONGEST_ROAD(false),
    BAZAAR_AUCTION(false),
    WIND_ROSE(false);

    /** flag for resolving The Count*/
    boolean landscapeSource;

    PointCategory(boolean landscapeSource) {
        this.landscapeSource = landscapeSource;
    }

    public boolean hasLandscapeSource() {
        return landscapeSource;
    }
}
