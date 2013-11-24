package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;

public class Farm extends MultiTileFeature {

    protected Feature[] adjoiningCities; //or castles
    protected boolean pigHerd;


    public Feature[] getAdjoiningCities() {
        return adjoiningCities;
    }

    public void setAdjoiningCities(Feature[] adjoiningCities) {
        this.adjoiningCities = adjoiningCities;
    }

    public boolean isPigHerd() {
        return pigHerd;
    }

    public void setPigHerd(boolean pigHerd) {
        this.pigHerd = pigHerd;
    }

    @Override
    protected Location[] getSides() {
        return Location.sidesFarm();
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.FARM;
    }

    @Override
    public FarmScoreContext getScoreContext() {
        return new FarmScoreContext(getGame());
    }

    public static String name() {
        return _("Farm");
    }
}
