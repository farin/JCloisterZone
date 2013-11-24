package com.jcloisterzone.rmi;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.PlayerSlot;

/**
 * Declares complete client API.
 * In addition to Client2ClientIF this class ass messages which has orginin on server side.
 *
 */
public interface ClientIF extends Client2ClientIF {

    public void updateSlot(PlayerSlot slot);
    public void updateSupportedExpansions(EnumSet<Expansion> expansions);

    /* ---------------------- STARTED GAME MESSAGES (server triggered) ------------------*/

    void drawTiles(int[] tileIndexes);
    void setFlierDistance(int distance);

}
