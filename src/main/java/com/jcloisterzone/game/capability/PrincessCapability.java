package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XmlUtils.attributeBoolValue;

import java.util.List;

import org.w3c.dom.Element;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.PrincessAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class PrincessCapability extends Capability {

    public PrincessCapability(Game game) {
        super(game);
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof City && attributeBoolValue(xml, "princess")) {
            ((City)feature).setPricenss(true);
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        City c = getTile().getCityWithPrincess();
        if (c == null || ! c.walk(new IsOccupied().with(Follower.class))) return;
        Feature cityRepresentative = c.getMaster();

        PrincessAction princessAction = new PrincessAction();
        for (Meeple m : game.getDeployedMeeples()) {
            if (!(m.getFeature() instanceof City)) continue;
            if (m.getFeature().getMaster().equals(cityRepresentative) && m instanceof Follower) {
                princessAction.getOrCreate(m.getPosition()).add(m.getLocation());
            }
        }
        if (!princessAction.getLocationsMap().isEmpty()) {
            actions.add(princessAction);
        }
    }

}
