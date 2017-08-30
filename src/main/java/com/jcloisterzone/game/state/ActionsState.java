package com.jcloisterzone.game.state;

import java.io.Serializable;

import com.google.common.base.Predicates;
import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

@Immutable
public class ActionsState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Player player;
    private final Vector<PlayerAction<?>> actions;
    private final boolean passAllowed;

    public ActionsState(Player player, Vector<PlayerAction<?>> actions, boolean passAllowed) {
        this.player = player;
        this.actions = actions;
        this.passAllowed = passAllowed;
    }

    public ActionsState(Player player, PlayerAction<?> action, boolean passAllowed) {
        this(player, Vector.of(action), passAllowed);
    }

    public ActionsState setActions(Vector<PlayerAction<?>> actions) {
        if (this.actions == actions) return this;
        return new ActionsState(player, actions, passAllowed);
    }

    public ActionsState appendAction(PlayerAction<?> action) {
        return setActions(actions.append(action));
    }

    public ActionsState appendActions(Iterable<PlayerAction<?>> actions) {
        return setActions(this.actions.appendAll(actions));
    }

    /**
     * Merge all MeepleActions with same meepleType to single action.
     */
    public ActionsState mergeMeepleActions() {
        Seq<Vector<MeepleAction>> groupped = this.actions
            .filter(Predicates.instanceOf(MeepleAction.class))
            .map(a -> (MeepleAction) a)
            .groupBy(a -> ((MeepleAction)a).getMeepleType())
            .values();

        if (groupped.find(v -> v.length() > 1).isEmpty()) {
            return this; // nothing to merge
        }

        Vector<PlayerAction<?>> actions = Vector.ofAll(
            groupped.map(v -> {
                Set<FeaturePointer> mergedOptions = v.map(a ->
                    a.getOptions()).reduce((o1, o2) -> o1.addAll(o2)
                );
                return new MeepleAction(v.get().getMeepleType(), mergedOptions);
            })
        );
        actions = actions.appendAll(
            this.actions.filter(Predicates.instanceOf(MeepleAction.class).negate())
        );
        return setActions(actions);
    }

    public Player getPlayer() {
        return player;
    }

    public Vector<PlayerAction<?>> getActions() {
        return actions;
    }

    public boolean isPassAllowed() {
        return passAllowed;
    }
}
