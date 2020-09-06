package com.jcloisterzone.action;

import com.jcloisterzone.io.message.CornCircleRemoveOrDeployMessage;
import io.vavr.collection.Iterator;
import io.vavr.collection.Set;

import java.io.Serializable;

/**
 * Represents a set of options a player can choose from.
 *
 * @param <T> the type of options the player can choose from; examples are {@link com.jcloisterzone.board.Position},
 *            {@link com.jcloisterzone.board.Location}, {@link com.jcloisterzone.board.PlacementOption},
 *            {@link CornCircleRemoveOrDeployMessage.CornCircleOption}, etc.
 */
public interface PlayerAction<T> extends Iterable<T>, Serializable {

    /**
     * Returns an iterator over the options the player can choose from.
     *
     * @return an iterator over the options the player can choose from
     */
    @Override
    default Iterator<T> iterator() {
        return getOptions().iterator();
    }

    /**
     * Returns the options the player can choose from.
     *
     * @return the options the player can choose from
     */
    Set<T> getOptions();

    /**
     * Checks whether there are any options.
     *
     * @return {@code true} if there are no options, {@code false} otherwise
     */
    default boolean isEmpty() {
        return getOptions().isEmpty();
    }
}
