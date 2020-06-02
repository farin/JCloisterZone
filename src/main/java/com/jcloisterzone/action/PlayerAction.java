package com.jcloisterzone.action;

import java.io.Serializable;

import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Iterator;
import io.vavr.collection.Set;

// TODO rename to PlayerChoice?
/**
 * Represents a set of options a player can choose from.
 *
 * @param <T> the type of options the player can choose from; examples are {@link com.jcloisterzone.board.Position},
 *            {@link com.jcloisterzone.board.Location}, {@link com.jcloisterzone.board.PlacementOption},
 *            {@link CornCircleRemoveOrDeployMessage.CornCircleOption}, etc.
 */
public interface PlayerAction<T> extends Iterable<T>, Serializable {

    /**
     * Generates a WebSocket message that informs the receiver that the player chose the given {@code option}.
     *
     * @param option the option chosen
     * @return the WebSocket message
     */
    public WsInGameMessage select(T option);

    /**
     * Returns an iterator over the options the player can choose from.
     *
     * @return an iterator over the options the player can choose from
     */
    @Override
    default public Iterator<T> iterator() {
        return getOptions().iterator();
    }

    /**
     * Returns the options the player can choose from.
     *
     * @return the options the player can choose from
     */
    public Set<T> getOptions();

    /**
     * Checks whether there are any options.
     *
     * @return {@code true} if there are no options, {@code false} otherwise
     */
    default public boolean isEmpty() {
        return getOptions().isEmpty();
    }
}
