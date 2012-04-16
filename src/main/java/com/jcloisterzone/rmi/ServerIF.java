package com.jcloisterzone.rmi;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.PlayerSlot;


public interface ServerIF extends Client2ClientIF {

    public void updateSlot(PlayerSlot slot, EnumSet<Expansion> supportedExpansions); //pass null if all expansions are supported

    /* ---------------------- STARTED GAME MESSAGES ------------------*/

    /**
     * Generates random tiles indexes.
     */
    public void selectTiles(Integer tiles, int count); //generate random numbers

}
