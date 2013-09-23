package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XmlUtils.attributeBoolValue;

import java.util.List;

import org.w3c.dom.Element;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.PrincessAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.GameExtension;

public class PrincessCapability extends GameExtension {

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof City && attributeBoolValue(xml, "princess")) {
            ((City)feature).setPricenss(true);
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        City c = getTile().getPrincessCityPiece();
        if (c == null || ! c.walk(new IsOccupied().with(Follower.class))) return;
        Feature cityRepresentative = c.getMaster();

        PrincessAction princessAction = new PrincessAction();
        for (Meeple m : getGame().getDeployedMeeples()) {
            if (! (m.getFeature() instanceof City)) continue;
            if (m.getFeature().getMaster().equals(cityRepresentative) && m instanceof Follower) {
                princessAction.getOrCreate(m.getPosition()).add(m.getLocation());
            }
        }
        if (! princessAction.getSites().isEmpty()) {
            actions.add(princessAction);
        }
    }

}
