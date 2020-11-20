package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import io.vavr.collection.Set;

import java.io.Serializable;

@Immutable
public class CountCapabilityModel implements Serializable {

	private static final long serialVersionUID = 1L;

    /** position of CO.7 tile */
    private final Position quarterPosition;

    /** players who passed their turn */
    private final Set<Player> finalScoringPass;

    public CountCapabilityModel(Position quarterPosition, Set<Player> finalScoringPass) {
        this.quarterPosition = quarterPosition;
        this.finalScoringPass = finalScoringPass;
    }

    public Position getQuarterPosition() {
        return quarterPosition;
    }

    public Set<Player> getFinalScoringPass() {
        return finalScoringPass;
    }

    public CountCapabilityModel setFinalScoringPass(Set<Player> value) {
        return new CountCapabilityModel(quarterPosition, value);
    }
}
