package com.jcloisterzone.game.capability;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.action.MageAndWitchAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class MageAndWitchCapability extends Capability {

    public FeaturePointer magePlacement;
    public FeaturePointer witchPlacement;

    public MageAndWitchCapability(Game game) {
        super(game);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("mage").getLength() > 0) {
            tile.setTrigger(TileTrigger.MAGE);
        }
    }

    public MageAndWitchAction prepareMageAction() {
        MageAndWitchAction action = new MageAndWitchAction(true);
        prepareMageWitchLocations(action, magePlacement);
        return action;
    }

    public MageAndWitchAction prepareWitchAction() {
        MageAndWitchAction action = new MageAndWitchAction(false);
        prepareMageWitchLocations(action, witchPlacement);
        return action;
    }

    private void prepareMageWitchLocations(MageAndWitchAction action, FeaturePointer exclude) {
        Set<Feature> touchedFeatures = new HashSet<>();
        if (exclude != null) {
            touchedFeatures.add(getBoard().get(exclude));
        }
        for (Tile tile: getBoard().getAllTiles()) {
            for (Feature f: tile.getFeatures()) {
                if (f instanceof Road || f instanceof City) {
                    action.addAll(f.walk(new IsUnfinished(touchedFeatures)));
                }
            }
        }
    }

    public boolean isMageAndWitchPlacedOnSameFeature() {
        if (magePlacement == null || witchPlacement == null) return false;
        return getBoard().get(magePlacement).walk(new ContainsFeature(witchPlacement));
    }

    public FeaturePointer getMagePlacement() {
        return magePlacement;
    }

    public void setMagePlacement(FeaturePointer magePlacement) {
        this.magePlacement = magePlacement;
    }

    public FeaturePointer getWitchPlacement() {
        return witchPlacement;
    }

    public void setWitchPlacement(FeaturePointer witchPlacement) {
        this.witchPlacement = witchPlacement;
    }

    class ContainsFeature implements FeatureVisitor<Boolean> {

        private FeaturePointer searchFor;
        private boolean result = false;

        public ContainsFeature(FeaturePointer searchFor) {
            this.searchFor = searchFor;
        }

        @Override
        public boolean visit(Feature feature) {
            if (searchFor.match(feature)) {
                result = true;
                return false;
            }
            return true;
        }

        @Override
        public Boolean getResult() {
            return result;
        }

    }

    class IsUnfinished implements FeatureVisitor<Set<FeaturePointer>> {

        private final Set<Feature> touchedFeatures;
        private final Set<FeaturePointer> result = new HashSet<>();
        private boolean isCompleted = true;

        public IsUnfinished(Set<Feature> touchedFeatures) {
            this.touchedFeatures = touchedFeatures;
        }

        @Override
        public boolean visit(Feature feature) {
            if (touchedFeatures.contains(feature)) {
                isCompleted = true; //force ignore
                return false;
            }
            Completable f = (Completable) feature;
            if (f.isOpen()) isCompleted = false;
            touchedFeatures.add(feature);
            result.add(new FeaturePointer(f.getTile().getPosition(), f.getLocation()));
            return true;
        }

        @Override
        public Set<FeaturePointer> getResult() {
            if (isCompleted) return Collections.emptySet();
            return result;
        }
    }


    @Override
    public Object backup() {
        return new Object[] {
            magePlacement,
            witchPlacement
         };
    }

    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        magePlacement = (FeaturePointer) a[0];
        witchPlacement = (FeaturePointer) a[1];
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (magePlacement != null) {
            Element mage = doc.createElement("mage");
            XmlUtils.injectPosition(mage, magePlacement.getPosition());
            mage.setAttribute("location", magePlacement.getLocation().toString());
            node.appendChild(mage);
        }
        if (witchPlacement != null) {
            Element witch = doc.createElement("witch");
            XmlUtils.injectPosition(witch, witchPlacement.getPosition());
            witch.setAttribute("location", witchPlacement.getLocation().toString());
            node.appendChild(witch);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("mage");
        if (nl.getLength() > 0) {
            Element mage = (Element) nl.item(0);
            Position p = XmlUtils.extractPosition(mage);
            Location loc =  Location.valueOf(mage.getAttribute("location"));
            magePlacement = new FeaturePointer(p, loc);
            game.post(new NeutralFigureMoveEvent(NeutralFigureMoveEvent.MAGE, null, null, magePlacement));
        }
        nl = node.getElementsByTagName("witch");
        if (nl.getLength() > 0) {
            Element witch = (Element) nl.item(0);
            Position p  = XmlUtils.extractPosition(witch);
            Location loc =  Location.valueOf(witch.getAttribute("location"));
            witchPlacement = new FeaturePointer(p, loc);
            game.post(new NeutralFigureMoveEvent(NeutralFigureMoveEvent.WITCH, null, null, witchPlacement));
        }
    }
}
