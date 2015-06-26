package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.MageAndWitchAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.FeatureVisitor;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class MageAndWitchCapability extends Capability {

	private Mage mage;
	private Witch witch;

    public MageAndWitchCapability(Game game) {
        super(game);
        mage = new Mage(game);
        witch = new Witch(game);
        game.getNeutralFigures().add(mage);
        game.getNeutralFigures().add(witch);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("mage").getLength() > 0) {
            tile.setTrigger(TileTrigger.MAGE);
        }
    }

    public List<PlayerAction<?>> prepareMageWitchActions() {
        Set<Feature> touchedFeatures = new HashSet<>();
        if (mage.isDeployed()) {
            touchedFeatures.add(getBoard().get(mage.getFeaturePointer()));
        }
        if (witch.getFeaturePointer() != null) {
            touchedFeatures.add(getBoard().get(witch.getFeaturePointer()));
        }
        Set<FeaturePointer> placements = new HashSet<>();
        for (Tile tile: getBoard().getAllTiles()) {
            for (Feature f: tile.getFeatures()) {
                if (f instanceof Road || f instanceof City) {
                    placements.addAll(f.walk(new IsUnfinished(touchedFeatures)));
                }
            }
        }
        if (placements.isEmpty()) return null;
        List<PlayerAction<?>> actions = new ArrayList<>(2);
        MageAndWitchAction action = new MageAndWitchAction(true);
        action.addAll(placements);
        actions.add(action);
        action = new MageAndWitchAction(false);
        action.addAll(placements);
        actions.add(action);
        return actions;
    }

    public boolean isMageAndWitchPlacedOnSameFeature() {
        if (mage.isInSupply() || witch.isInSupply()) return false;
        return getBoard().get(mage.getFeaturePointer()).walk(new ContainsFeature(witch.getFeaturePointer()));
    }

    public Mage getMage() {
		return mage;
	}

    public Witch getWitch() {
		return witch;
	}

    static class ContainsFeature implements FeatureVisitor<Boolean> {

        private FeaturePointer searchFor;
        private boolean result = false;

        public ContainsFeature(FeaturePointer searchFor) {
            this.searchFor = searchFor;
        }

        @Override
        public VisitResult visit(Feature feature) {
            if (searchFor.match(feature)) {
                result = true;
                return VisitResult.STOP;
            }
            return VisitResult.CONTINUE;
        }

        @Override
        public Boolean getResult() {
            return result;
        }

    }

    static class IsUnfinished implements FeatureVisitor<Set<FeaturePointer>> {

        private final Set<Feature> touchedFeatures;
        private final Set<FeaturePointer> result = new HashSet<>();
        private boolean isCompleted = true;

        public IsUnfinished(Set<Feature> touchedFeatures) {
            this.touchedFeatures = touchedFeatures;
        }

        @Override
        public VisitResult visit(Feature feature) {
            if (touchedFeatures.contains(feature)) {
                isCompleted = true; //force ignore
                return VisitResult.STOP;
            }
            Completable f = (Completable) feature;
            if (f.isOpen()) isCompleted = false;
            touchedFeatures.add(feature);
            result.add(new FeaturePointer(f.getTile().getPosition(), f.getLocation()));
            return VisitResult.CONTINUE;
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
            mage.getFeaturePointer(),
            witch.getFeaturePointer()
         };
    }

    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        mage.setFeaturePointer((FeaturePointer) a[0]);
        witch.setFeaturePointer((FeaturePointer) a[1]);
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (mage.isDeployed()) {
            Element mageEl = doc.createElement("mage");
            XMLUtils.injectFeaturePoiner(mageEl, mage.getFeaturePointer());
            node.appendChild(mageEl);
        }
        if (witch.isDeployed()) {
            Element witchEl = doc.createElement("witch");
            XMLUtils.injectFeaturePoiner(witchEl, witch.getFeaturePointer());
            node.appendChild(witchEl);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("mage");
        if (nl.getLength() > 0) {
            Element mageEl = (Element) nl.item(0);
            FeaturePointer fp = XMLUtils.extractFeaturePointer(mageEl);
            mage.deploy(fp);
        }
        nl = node.getElementsByTagName("witch");
        if (nl.getLength() > 0) {
            Element witchEl = (Element) nl.item(0);
            FeaturePointer fp  = XMLUtils.extractFeaturePointer(witchEl);
            witch.deploy(fp);
        }
    }
}
