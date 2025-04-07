package com.jcloisterzone.game.state;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.figure.Phantom;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

import java.io.Serializable;

/**
 * Represents all the sets of options a player can choose from in a certain turn.
 */
@Immutable
public class ActionsState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Player player;
    private final Vector<PlayerAction<?>> actions;
    private final boolean passAllowed;

    /**
     * Instantiates a new {@code ActionState}.
     *
     * @param player      the player allowed the choices contained in this instance
     * @param actions     the sets of options
     * @param passAllowed whether the player is allowed to pass
     */
    public ActionsState(Player player, Vector<PlayerAction<?>> actions, boolean passAllowed) {
        this.player = player;
        this.actions = actions;
        this.passAllowed = passAllowed;
    }

    /**
     * Instantiates a new {@code ActionState}.
     *
     * @param player      the player allowed the choices contained in this instance
     * @param action      a set of options
     * @param passAllowed whether the player is allowed to pass
     */
    public ActionsState(Player player, PlayerAction<?> action, boolean passAllowed) {
        this(player, Vector.of(action), passAllowed);
    }

    /**
     * Sets the sets of options.
     *
     * @param actions the sets of options
     * @return a new instance with the sets of options set
     */
    public ActionsState setActions(Vector<PlayerAction<?>> actions) {
        if (this.actions == actions) return this;
        return new ActionsState(player, actions, passAllowed);
    }

    /**
     * Appends a set of options to the sets of options already present.
     *
     * @param action the set of options to append
     * @return a new instance with the set of options appended
     */
    public ActionsState appendAction(PlayerAction<?> action) {
        return setActions(actions.append(action));
    }

    /**
     * Appends some sets of options to the sets of options already present.
     *
     * @param actions the sets of options to append
     * @return a new instance with the sets of options appended
     */
    public ActionsState appendActions(Iterable<PlayerAction<?>> actions) {
        return setActions(this.actions.appendAll(actions));
    }

    /**
     * Aggregates {@code MeepleAction}s by {@code meepleType}; every group is merged in a new {@code MeepleAction}
     * containing the options of all the instances that go into the aggregation.
     *
     * @return new instance with the {@code MeepleAction}s merged
     */
    public ActionsState mergeMeepleActions() {
        Seq<Vector<MeepleAction>> grouped = this.actions
            .filter(a -> a instanceof MeepleAction)
            .map(a -> (MeepleAction) a)
            .groupBy(MeepleAction::getMeepleType)
            .values(); // meeple actions grouped by meeple type (no keys, only values)

        // if only one action per meeple type, return
        if (grouped.find(v -> v.length() > 1).isEmpty()) {
            return this; // nothing to merge
        }

        Vector<PlayerAction<?>> actions = Vector.ofAll(
            grouped.map(v -> v.reduce(MeepleAction::merge))
        ); // one MeepleAction per meeple type, each containing as options those of all other MeepleActions of the same meeple type
        actions = actions.appendAll(
            this.actions.filter(a -> !(a instanceof MeepleAction))
        ); // merge back all other non-MeepleActions
        return setActions(actions);
    }

    /**
     * Reorder actions to logical order, like Phantom at end of actions
     *
     * @return new instance with the logical order of Actions
     */
    public ActionsState reorderActions() {
    	Vector<PlayerAction<?>> actions = this.actions.partition(
    			a -> !(a instanceof MeepleAction)
    			||
    			!((MeepleAction) a).getMeepleType().equals(Phantom.class)
    			).apply((nonPhantom, phantom) -> nonPhantom.appendAll(phantom));
    
        return setActions(actions);
    }

    /**
     * Gets the player associated with this instance.
     *
     * @return the player associated with this instance
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the sets of options associated with this instance.
     *
     * @return the sets of options associated with this instance
     */
    public Vector<PlayerAction<?>> getActions() {
        return actions;
    }

    /**
     * Checks whether the player is allowed to pass.
     *
     * @return {@code true} of the player is allowed to pass, {@code false} otherwise
     */
    public boolean isPassAllowed() {
        return passAllowed;
    }
}
