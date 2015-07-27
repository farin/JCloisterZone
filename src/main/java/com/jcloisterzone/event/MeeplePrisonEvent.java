package com.jcloisterzone.event;

import java.util.List;
import java.util.Map;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TowerCapability;

public class MeeplePrisonEvent extends MoveEvent<Player> implements Undoable {

    private final Follower meeple;

    public MeeplePrisonEvent(Follower meeple, Player from, Player to) {
        super(meeple.getPlayer(), from, to);
        this.meeple = meeple;
    }

    private Map<Player, List<Follower>> getPrisoners(Game game) {
        TowerCapability cap = game.getCapability(TowerCapability.class);
        return cap.getPrisoners();
    }

    @Override
    public void undo(Game game) {
        if (getFrom() != null) {
            meeple.setInPrison(true);
            getPrisoners(game).get(getFrom()).add(meeple);
        }
        if (getTo() != null) {
            for (List<Follower> prisoners : getPrisoners(game).values()) {
                prisoners.remove(meeple);
            }
            meeple.setInPrison(false);
        }
    }

    @Override
    public Event getInverseEvent() {
        throw new UnsupportedOperationException();
    }
}
