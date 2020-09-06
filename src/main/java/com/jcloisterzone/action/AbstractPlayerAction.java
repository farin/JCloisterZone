package com.jcloisterzone.action;

import com.jcloisterzone.io.message.CornCircleRemoveOrDeployMessage;
import io.vavr.collection.Set;


/**
 * Represents a set of options a player can choose from.
 *
 * @param <T> the type of options the player can choose from; examples are {@link com.jcloisterzone.board.Position},
 *            {@link com.jcloisterzone.board.Location}, {@link com.jcloisterzone.board.PlacementOption},
 *            {@link CornCircleRemoveOrDeployMessage.CornCircleOption}, etc.
 */
public abstract class AbstractPlayerAction<T> implements PlayerAction<T> {

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
    public AbstractPlayerAction(Set<T> options) {
       this.options = options;
    }


    /**
     * Returns the options the player can choose from.
     *
     * @return the options the player can choose from
     */
    @Override
    public Set<T> getOptions() {
        return options;
    }
}
