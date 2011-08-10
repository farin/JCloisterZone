package com.jcloisterzone.rmi;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.PlayerSlot;


public interface ClientIF extends Client2ClientIF {

	public void updateSlot(PlayerSlot slot);
	public void updateSupportedExpansions(EnumSet<Expansion> expansions);

	/* ---------------------- STARTED GAME MESSAGES ------------------*/

	void nextTile(Integer tileIndex);

}
