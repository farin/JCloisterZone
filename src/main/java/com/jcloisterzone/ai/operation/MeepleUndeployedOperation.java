package com.jcloisterzone.ai.operation;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;

public class MeepleUndeployedOperation implements Operation {

    private final Meeple meeple;
    private Tile tile;
    private Location loc;

    public MeepleUndeployedOperation(Meeple meeple) {
        this.meeple = meeple;
        this.tile = meeple.getFeature() == null ? null : meeple.getFeature().getTile(); //prison undeploy
        this.loc = meeple.getLocation();
    }

    @Override
    public void undo(Game game) {
        meeple.setLocation(loc);
        if (tile != null) {
            Feature feature = meeple.getPieceForDeploy(tile, loc);
            feature.addMeeple(meeple);
            meeple.setPosition(tile.getPosition());
            meeple.setFeature(feature);
        }
    }

}
