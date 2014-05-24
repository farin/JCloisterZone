package com.jcloisterzone.event;

import java.util.List;
import java.util.Map;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TowerCapability;

public class MeepleEvent extends PlayEvent implements Undoable {

    public static final int DEPLOY = 1;
    public static final int UNDEPLOY = 2;
    public static final int PRISON = 3;
    public static final int RELEASE = 4;

    private final Meeple meeple;
    private Player jailer; //to support undo
    
    //TODO meeple events are fired before meeple args are changes
    //that is quite unintuitive, but current impl read origin data from meeple
    
    public MeepleEvent(int type, Meeple meeple) {
        super(type, meeple.getPlayer(), meeple.getPosition(), meeple.getLocation());
        this.meeple = meeple;
    }
    
    public MeepleEvent(int type, Meeple meeple, Player jailer) {
    	this(type, meeple);
    	this.jailer = jailer;
    }
    

    public Meeple getMeeple() {
        return meeple;
    }
    
    private Map<Player, List<Follower>> getPrisoners(Game game) {
    	TowerCapability cap = game.getCapability(TowerCapability.class);
    	return cap.getPrisoners();
    }

    @Override
    public void undo(Game game) {
        switch (getType()) {
        case DEPLOY:
            meeple.undeploy(false);
            break;
        case RELEASE:
        	//TODO depends on inner impl of capability
        	meeple.setLocation(Location.PRISON);
        	getPrisoners(game).get(jailer).add((Follower) meeple);
        	break;
        case PRISON:
        	//TODO depends on inner impl of capability
        	for (List<Follower> prisoners : getPrisoners(game).values()) {
        		prisoners.remove(meeple);
        	}
        	//no break! share undeploy code
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
