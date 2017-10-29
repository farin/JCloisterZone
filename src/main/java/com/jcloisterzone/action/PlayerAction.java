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
public abstract class PlayerAction<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The options the player can choose from.
     */
    protected final Set<T> options;

    /**
     * Instantiates a new {@code PlayerAction}.
     *
     * @param options the options the player can choose from
     */
    public PlayerAction(Set<T> options) {
       this.options = options;
    }

    /**
     * Generates a WebSocket message that informs the receiver that the player chose the given {@code option}.
     *
     * @param option the option chosen
     * @return the WebSocket message
     */
    public abstract WsInGameMessage select(T option);

    /**
     * Returns an iterator over the options the player can choose from.
     *
     * @return an iterator over the options the player can choose from
     */
    @Override
    public Iterator<T> iterator() {
        return options.iterator();
    }

    /**
     * Returns the options the player can choose from.
     *
     * @return the options the player can choose from
     */
    public Set<T> getOptions() {
        return options;
    }

    /**
     * Checks whether there are any options.
     *
     * @return {@code true} if there are no options, {@code false} otherwise
     */
    public boolean isEmpty() {
        return options.isEmpty();
    }
}
