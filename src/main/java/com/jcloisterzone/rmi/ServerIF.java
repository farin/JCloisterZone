package com.jcloisterzone.rmi;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.PlayerSlot;

/**
 * Declares complete server API. In addition to Client2ClientIF messages, which are just passed
 * to all other connecte clients, messages described on this class represent some server side logic.
 */
public interface ServerIF extends Client2ClientIF {


    /**
     * Generates random tiles indexes. For security reasons all random selections are made on server side.
     */
    public void selectTiles(int tilesCount, int drawCount); //generate random numbers
    public void rollFlierDice(Class<? extends Meeple> meepleType);

}
