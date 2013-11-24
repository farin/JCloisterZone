package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TowerCapability;


public class TowerCapturePhase extends Phase {

    public TowerCapturePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(TowerCapability.class);
    }

    @Override
    public void enter() {
        //TODO move handle tower placement here from action phase or not ?
    }

    @Override
    public void takePrisoner(Position p, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        Follower m = (Follower) game.getMeeple(p, loc, meepleType, game.getPlayer(meepleOwner));
        m.undeploy();
        //unplace figure returns figure to owner -> we must handle capture / prisoner exchange
        Player me = getActivePlayer();
        if (m.getPlayer() != me) {
            TowerCapability towerCap = game.getCapability(TowerCapability.class);
            List<Follower> prisoners = towerCap.getPrisoners().get(m.getPlayer());
            List<Follower> myCapturedFollowers = new ArrayList<>();
            for (Follower f : prisoners) {
                if (f.getPlayer() == me) {
                    myCapturedFollowers.add(f);
                }
            }

            if (myCapturedFollowers.isEmpty()) {
                towerCap.inprison(m, me);
            } else {
                //opponent has my prisoner - figure exchage
                Follower exchanged = myCapturedFollowers.get(0); //TODO same type?
                boolean removeOk = prisoners.remove(exchanged);
                assert removeOk;
                game.fireGameEvent().undeployed(exchanged);
                exchanged.clearDeployment();
                //? some events ?
            }
        }
        next();
    }

}
