package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import java.util.List;

import com.jcloisterzone.figure.Meeple;


public class Tower extends TileFeature {

    private int height;

    public int increaseHeight() {
        return ++height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Meeple getMeeple() {
        List<Meeple> meeples = getMeeples();
        if (meeples.isEmpty()) return null;
        return meeples.get(0);
    }

    public static String name() {
        return _("Tower");
    }

}
