package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.RemoveTileException;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;

import static com.jcloisterzone.XmlUtils.attributeBoolValue;

public class GermanMonasteriesCapability extends Capability {

     public GermanMonasteriesCapability(Game game) {
        super(game);
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof Cloister) {
            ((Cloister)feature).setMonastery(attributeBoolValue(xml, "monastery"));
        }
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (!game.hasRule(CustomRule.KEEP_CLOISTERS)) {
            if (tile.getId().equals("BA.L") || tile.getId().equals("BA.LR")) {
                throw new RemoveTileException();
            }
        }
    }

    @Override
    public void postPrepareActions(List<PlayerAction<?>> actions) {
        for (MeepleAction ma : findFollowerActions(actions)) {
            List<FeaturePointer> abbots = new ArrayList<>();
            for (FeaturePointer fp : ma.getOptions()) {
                if (fp.getLocation() == Location.CLOISTER && ((Cloister) getBoard().get(fp)).isMonastery()) {
                    abbots.add(new FeaturePointer(fp.getPosition(), Location.ABBOT));
                }
            }
            ma.addAll(abbots);
        }
    }
}
