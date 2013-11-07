package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XmlUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;


public final class ShrineCapability extends Capability {

    public ShrineCapability(Game game) {
        super(game);
    }

    @Override
    public void initFeature(Tile tile, Feature feature, Element xml) {
        if (feature instanceof Cloister) {
            ((Cloister)feature).setShrine(attributeBoolValue(xml, "shrine"));
        }
    }

    public void makeCloisterChallenged(Cloister cloister) {
        boolean first = true;
        for (Meeple m : cloister.getMeeples()) {
            if (first) {
                game.fireGameEvent().scored(cloister, 0, "0", m, false);
                first = false;
            }
            m.undeploy();
        }
    }


    public void resolveChallengedCloisters(Cloister cloister) {
        Position p = cloister.getTile().getPosition();
        for (Tile nt : game.getBoard().getAdjacentAndDiagonalTiles(p)) {
            if (nt.hasCloister()) {
                Cloister nextCloister = nt.getCloister();
                if (cloister.isShrine() ^ nextCloister.isShrine()) {
                    //opposite cloisters
                    if (nextCloister.isOpen()) {
                        makeCloisterChallenged(nextCloister);
                    }
                }
            }
        }
    }

    @Override
    public void scoreCompleted(CompletableScoreContext ctx) {
        if (ctx.getMasterFeature() instanceof Cloister) {
            resolveChallengedCloisters((Cloister) ctx.getMasterFeature());
        }
    }

    @Override
    public boolean isTilePlacementAllowed(Tile tile, Position p) {
        if (tile.hasCloister()) {
            int opositeCount = 0;
            int sameCount = 0;
            for (Tile nt: getBoard().getAdjacentAndDiagonalTiles(p)) {
                if (nt.hasCloister()) {
                    if  (tile.getCloister().isShrine() ^ nt.getCloister().isShrine()) {
                        opositeCount++;
                    } else {
                        sameCount++;
                    }
                }
            }
            if (opositeCount > 1 || (opositeCount == 1 && sameCount > 0)) {
                return false;
            }
        }
        return true;
    }

}
