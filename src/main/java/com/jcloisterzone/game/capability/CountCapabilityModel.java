package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;

@Immutable
public class CountCapabilityModel {

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
