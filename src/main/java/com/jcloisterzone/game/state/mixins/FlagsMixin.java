package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Set;

public interface FlagsMixin {

    Set<Flag> getFlags();
    GameState setFlags(Set<Flag> flags);


    default boolean hasFlag(Flag flag) {
        return getFlags().contains(flag);
    }

    default GameState addFlag(Flag flag) {
        //HashSet makes contains check and returns same instance, no need to do it again here
        return setFlags(getFlags().add(flag));
    }

}
