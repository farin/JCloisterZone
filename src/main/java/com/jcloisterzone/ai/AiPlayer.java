package com.jcloisterzone.ai;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TakePrisonerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.RmiProxy;

public abstract class AiPlayer {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Game game;
    protected GameController gc;

    private RmiProxy rmiProxy;
    private Player player;

    public void setGame(Game game) {
        this.game = game;
    }

    public RmiProxy getRmiProxy() {
        return rmiProxy;
    }

    public void setGameController(GameController gc) {
        this.gc = gc;
        Integer placeTileDelay = gc.getConfig().getAi_place_tile_delay();
        rmiProxy = new DelayedServer(gc.getRmiProxy(), placeTileDelay == null ? 0 : placeTileDelay);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    protected GameController getGameController() {
        return gc;
    }

    // dummy implementations

    protected final void selectDummyAction(List<? extends PlayerAction<?>> actions, boolean canPass) {
        for (PlayerAction<?> action : actions) {
            if (action instanceof TilePlacementAction) {
                if (selectDummyTilePlacement((TilePlacementAction) action)) return;
            }
            if (action instanceof AbbeyPlacementAction) {
                if (selectDummyAbbeyPlacement((AbbeyPlacementAction) action)) return;
            }
            if (action instanceof MeepleAction) {
                if (selectDummyMeepleAction((MeepleAction) action)) return;
            }
            if (action instanceof TakePrisonerAction) {
                if (selectDummyTowerCapture((TakePrisonerAction) action)) return;
            }
        }
        getRmiProxy().pass();
    }

    protected boolean selectDummyAbbeyPlacement(AbbeyPlacementAction action) {
        getRmiProxy().pass();
        return true;
    }

    protected boolean selectDummyTilePlacement(TilePlacementAction action) {
        TilePlacement nearest = null;
        Position p0 = new Position(0, 0);
        int min = Integer.MAX_VALUE;
        for (TilePlacement tp : action) {
            int dist = tp.getPosition().squareDistance(p0);
            if (dist < min) {
                min = dist;
                nearest = tp;
            }
        }
        getRmiProxy().placeTile(nearest.getRotation(), nearest.getPosition());
        return true;
    }

    protected boolean selectDummyMeepleAction(MeepleAction ma) {
        for (FeaturePointer fp : ma) {
            Feature f = game.getBoard().get(fp.getPosition()).getFeature(fp.getLocation());
            if (f instanceof City || f instanceof Road || f instanceof Cloister) {
                getRmiProxy().deployMeeple(fp.getPosition(), fp.getLocation(), ma.getMeepleType());
                return true;
            }
        }
        return false;
    }

    protected boolean selectDummyTowerCapture(TakePrisonerAction action) {
        MeeplePointer mp = action.iterator().next();
        getRmiProxy().takePrisoner(mp.getPosition(), mp.getLocation(), mp.getMeepleType(), mp.getMeepleOwner().getIndex());
        return true;
    }

    protected final void selectDummyDragonMove(Set<Position> positions, int movesLeft) {
        getRmiProxy().moveDragon(positions.iterator().next());
    }

    @Override
    public String toString() {
        return String.valueOf(player);
    }

}
