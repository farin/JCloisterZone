package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.event.PrisonerExchangedEvent;
import com.jcloisterzone.event.SelectPrisonerToExchangeEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TowerCapability;


public class PrisonerExchangePhase extends Phase {

    private final TowerCapability towerCap;

    public PrisonerExchangePhase(Game game) {
        super(game);
        towerCap = game.getCapability(TowerCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(TowerCapability.class);
    }

    @Override
    public void enter() {
        game.post(new SelectPrisonerToExchangeEvent(getActivePlayer()));
    }

    @Override
    public void exchangePrisoners(Class<? extends Follower> meepleType) {
        int lastPos = towerCap.getPrisoners().get(game.getActivePlayer()).size() - 1;
        List<Follower> opponentPrisoners = towerCap.getPrisoners().get(towerCap.getPrisoners().get(game.getActivePlayer()).get(lastPos).getPlayer());
        List<Follower> myPrisoners = towerCap.getPrisoners().get(game.getActivePlayer());
        for (Follower exchanged: opponentPrisoners) {
            if (exchanged.getClass() == meepleType) {
                boolean removeOk = opponentPrisoners.remove(exchanged);
                assert removeOk;
                exchanged.setInPrison(false);

                towerCap.getPrisoners().get(game.getActivePlayer()).get(lastPos).setInPrison(false);
                game.post(new PrisonerExchangedEvent(towerCap.getPrisoners().get(game.getActivePlayer()).get(lastPos).getPlayer()));
                removeOk = myPrisoners.remove(towerCap.getPrisoners().get(game.getActivePlayer()).get(lastPos));
                assert removeOk;

                next();
                break;
            }
        }
    }
}
