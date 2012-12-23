package com.jcloisterzone.game.phase;

import java.util.List;

import com.google.common.collect.Lists;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.TowerGame;


public class TowerCapturePhase extends Phase {

    public TowerCapturePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasExpansion(Expansion.TOWER);
    }

    @Override
    public void enter() {
        //TODO move handle tower placement here from action phase or not ?
    }

    @Override
    public void takePrisoner(Position p, Location loc, Class<? extends Meeple> meepleType) {
        Follower m = (Follower) game.getMeeple(p, loc, meepleType);
        m.undeploy();
        //unplace figure returns figure to owner -> we must handle capture / prisoner exchange
        Player me = getActivePlayer();
        if (m.getPlayer() != me) {
            TowerGame tg = game.getTowerGame();
            List<Follower> myCapturedFollowers = Lists.newArrayList();
            for(Follower f : tg.getPrisoners().get(m.getPlayer())) {
                if (f.getPlayer() == me) {
                    myCapturedFollowers.add(f);
                }
            }

            if (myCapturedFollowers.isEmpty()) {
                tg.inprison(m, me);
            } else {
                //opponent has my prisoner - figure exchage
                Follower exchanged = myCapturedFollowers.get(0); //TODO same type
                tg.getPrisoners().get(m.getPlayer()).remove(exchanged);
                exchanged.clearDeployment();
                //? some events ?
            }
        }
        next();
    }

}
