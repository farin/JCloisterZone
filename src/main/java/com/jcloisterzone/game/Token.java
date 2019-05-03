package com.jcloisterzone.game;

import com.jcloisterzone.wsio.message.adapters.TokenAdapter;

/**
 * All tokens must be inner classes inside capability.
 * (Although at the time nothing happen if not but in future {@link TokenAdapter} may rely on it)
 */
public interface Token {
	String name();
}
