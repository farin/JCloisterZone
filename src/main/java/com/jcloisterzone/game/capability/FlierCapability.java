package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.SnapshotCorruptedException;

public class FlierCapability extends Capability<Void> {

    boolean flierUsed = false; //prevent phantom use flier if flier used this turn
    private int flierDistance;
    private Class<? extends Meeple> meepleType;


    public int getFlierDistance() {
        return flierDistance;
    }

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    public void setFlierDistance(Class<? extends Meeple> meepleType, int flierDistance) {
        assert flierDistance > 0 ^ meepleType == null;
        this.meepleType = meepleType;
        this.flierDistance = flierDistance;
    }

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("flier");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            Location flier = XMLUtils.union(XMLUtils.contentAsLocations((Element) nl.item(0)));
            tile.setFlier(flier);
        }
    }

    private List<Feature> getReachableFeatures() {
        List<Feature> result = new ArrayList<>();
        Location direction = getCurrentTile().getFlier().rotateCW(getCurrentTile().getRotation());
        Position pos = getCurrentTile().getPosition();
        for (int i = 0; i < 3; i++) {
            pos = pos.add(direction);
            Tile target = getBoard().getPlayer(pos);
            if (target != null) {
                for (Feature f : target.getFeatures()) {
                    if (f instanceof Cloister) {
                       Cloister cloister = (Cloister) f;
                       if (cloister.isMonastery()) {
                           result.add(f); //monastery is always valid target
                           continue;
                       }
                    }
                    if (f instanceof Completable) {
                        if (f.walk(new IsCompleted())) continue;
                        result.add(f);
                    }
                }
            }
        }
        return result;
    }

    private boolean isLandingExists(Follower follower, List<Feature> reachable) {
        for (Feature feature : reachable) {
            if (follower.isDeploymentAllowed(feature).result) return true;
        }
        return false;
    }

    @Override
    public void postPrepareActions(List<PlayerAction<?>> actions) {
        prepareFlier(actions, true);
    }


    public void prepareFlier(List<PlayerAction<?>> actions, boolean allowAdd) {
        Tile tile = game.getCurrentTile();
        if (flierUsed || tile.getFlier() == null) return;

        List<Feature> reachable = getReachableFeatures();
        if (reachable.isEmpty()) return;

        followerLoop:
        for (Follower f : game.getActivePlayer().getFollowers()) {
            if (!f.isInSupply()) continue;
            boolean landingExists = isLandingExists(f, reachable);

            for (PlayerAction<?> action : actions) {
                if (action instanceof MeepleAction) {
                    MeepleAction ma = (MeepleAction) action;
                    if (ma.getMeepleType().equals(f.getClass())) {
                        if (landingExists) {
                            ma.add(new FeaturePointer(tile.getPosition(), Location.FLIER));
                        }
                        continue followerLoop;
                    }
                }
            }

            if (allowAdd && landingExists) {
                MeepleAction action = new MeepleAction(f.getClass());
                action.add(new FeaturePointer(getCurrentTile().getPosition(), Location.FLIER));
                actions.add(action);
            }
        }
    }

    @Override
    public void turnPartCleanUp() {
        flierUsed = false;
    }

    public boolean isFlierUsed() {
        return flierUsed;
    }

    public void setFlierUsed(boolean flierUsed) {
        this.flierUsed = flierUsed;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (flierUsed) {
            node.setAttribute("flierUsed", "true");
        }
        if (flierDistance > 0) {
            node.setAttribute("flierDistance", ""+flierDistance);
            node.setAttribute("meepleType", meepleType.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
        if (XMLUtils.attributeBoolValue(node, "flierUsed")) {
            flierUsed = true;
        }
        if (node.hasAttribute("flierDistance")) {
             flierDistance = Integer.parseInt(node.getAttribute("flierDistance"));
             meepleType = (Class<? extends Meeple>) XMLUtils.classForName(node.getAttribute("meepleType"));
        }
    }

}
