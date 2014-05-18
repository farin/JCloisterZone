package com.jcloisterzone.event;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public class MeepleEvent extends PlayEvent implements Undoable {

    public static final int DEPLOY = 1;
    public static final int UNDEPLOY = 2;
    public static final int PRISON = 3;
    public static final int RELEASE = 4;

    private final Meeple meeple;

    public MeepleEvent(int type, Meeple meeple) {
        super(type, meeple.getPlayer(), meeple.getPosition(), meeple.getLocation());
        this.meeple = meeple;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    @Override
    public void undo(Game game) {
        switch (getType()) {
        case DEPLOY:
            meeple.undeploy(false);
            break;
        case UNDEPLOY:
            meeple.setLocation(getLocation());
            if (getPosition() != null) {
                Feature feature = meeple.getDeploymentFeature(game.getBoard().get(getPosition()), getLocation());
                feature.addMeeple(meeple);
                meeple.setPosition(getPosition());
                meeple.setFeature(feature);
            }
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }
}
