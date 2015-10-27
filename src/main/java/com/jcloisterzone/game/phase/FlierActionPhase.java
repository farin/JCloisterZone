package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.IsCompleted;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FlierCapability;

public class FlierActionPhase extends Phase {

    private final FlierCapability flierCap;

    public FlierActionPhase(Game game) {
        super(game);
        flierCap = game.getCapability(FlierCapability.class);
    }

    @Override
    public void enter() {
        int distance = flierCap.getFlierDistance();
        Tile origin = game.getCurrentTile();
        Location direction = origin.getFlier().rotateCW(origin.getRotation());
        Position pos = game.getCurrentTile().getPosition();
        for (int i = 0; i < distance; i++) {
            pos = pos.add(direction);
        }
        Tile target = getBoard().get(pos);

        Class<? extends Meeple> meepleType = flierCap.getMeepleType();
        Follower follower = (Follower) getActivePlayer().getMeepleFromSupply(meepleType);

        if (target == null || !game.isDeployAllowed(target, meepleType)) {
            next();
            return;
        }

        MeepleAction action = new MeepleAction(meepleType);
        for (Feature f : target.getFeatures()) {
            if (!(f instanceof Completable)) continue;
            if (f instanceof Cloister) {
                Cloister cloister = (Cloister) f;
                if (cloister.isMonastery()) {
                    action.add(new FeaturePointer(pos, Location.ABBOT));
                }
            }

            if (f.walk(new IsCompleted())) continue;
            if (follower.isDeploymentAllowed(f).result) {
                action.add(new FeaturePointer(pos, f.getLocation()));
            }
        }
        if (action.isEmpty()) {
            next();
            return;
        }
        game.post(new SelectActionEvent(getActivePlayer(), action, false, false));
    }

    @Override
    public void notifyRansomPaid() {
        enter(); //recompute available actions
    }

    @Override
    public void next() {
        boolean isPhantom = Phantom.class.equals(flierCap.getMeepleType());
        flierCap.setFlierDistance(null, 0); //resets meepleType
        if (isPhantom) {
            PhantomPhase pp = game.getPhases().getInstance(PhantomPhase.class);
            super.next(pp.getDefaultNext());
        } else {
            super.next();
        }
    }

    @Override
    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
        if (!meepleType.equals(flierCap.getMeepleType())) {
            throw new IllegalArgumentException("Invalid meeple type.");
        }
        Meeple m = getActivePlayer().getMeepleFromSupply(meepleType);
        m.deploy(fp);
        next();
    }

}
