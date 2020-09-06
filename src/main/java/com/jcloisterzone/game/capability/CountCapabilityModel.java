package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

import java.io.Serializable;

@Immutable
public class CountCapabilityModel implements Serializable {

	private static final long serialVersionUID = 1L;

    /** position of CO.7 tile */
    private final Position quarterPosition;

    /** player how make last meeple deployment */
    private final Player finalScoringLastMeepleDeployPlayer;

    public CountCapabilityModel(Position quarterPosition, Player finalScoringLastMeepleDeployPlayer) {
        this.quarterPosition = quarterPosition;
        this.finalScoringLastMeepleDeployPlayer = finalScoringLastMeepleDeployPlayer;
    }

    public Position getQuarterPosition() {
        return quarterPosition;
    }

    public Player getFinalScoringLastMeepleDeployPlayer() {
        return finalScoringLastMeepleDeployPlayer;
    }

    public CountCapabilityModel setFinalScoringLastMeepleDeployPlayer(Player player) {
        return new CountCapabilityModel(quarterPosition, player);
    }
}
