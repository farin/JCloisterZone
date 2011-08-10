package com.jcloisterzone.rmi;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.PlayerSlot;


public interface ServerIF extends Client2ClientIF {

	public void updateSlot(PlayerSlot slot, EnumSet<Expansion> supportedExpansions); //pass null if all expansions are supported

	/* ---------------------- STARTED GAME MESSAGES ------------------*/

	public void selectTile(Integer tiles); //generate random number

}
