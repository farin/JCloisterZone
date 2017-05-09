package com.jcloisterzone.event.play;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.game.state.GameState;

/**
 * Ancestor for all in-game event.
 */
@Immutable
public abstract class PlayEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PlayEventMeta metadata;


    public PlayEvent(PlayEventMeta metadata) {
        this.metadata = metadata;
    }

    public PlayEventMeta getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Immutable
    public static class PlayEventMeta implements Serializable {

        private final long time;
        private final Integer triggeringPlayerIndex;

        public PlayEventMeta(long time, Integer triggeringPlayerIndex) {
            this.time = time;
            this.triggeringPlayerIndex = triggeringPlayerIndex;
        }

        public static PlayEventMeta createWithActivePlayer(GameState state) {
            Player p = state.getActivePlayer();
            return PlayEventMeta.createWithPlayer(p);
        }

        public static PlayEventMeta createWithoutPlayer() {
            return PlayEventMeta.createWithPlayer(null);
        }

        public static PlayEventMeta createWithPlayer(Player p) {
            return new PlayEventMeta(
                System.currentTimeMillis(),
                p == null ? null : p.getIndex()
            );
        }

        public long getTime() {
            return time;
        }

        public Integer getTriggeringPlayerIndex() {
            return triggeringPlayerIndex;
        }
    }
}
