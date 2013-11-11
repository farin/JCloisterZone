package com.jcloisterzone.ai;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TakePrisonerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.rmi.mina.ClientStub;

public abstract class AiPlayer implements UserInterface {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Game game;

    private ServerIF server;
    private ClientStub clientStub;
    private Player player;

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public ServerIF getServer() {
        return server;
    }

    public void setServer(ServerIF server) {
        Integer placeTileDelay = game.getConfig().get("players", "ai_place_tile_delay", Integer.class);
        this.server = new DelayedServer(server, placeTileDelay == null ? 0 : placeTileDelay);
        this.clientStub = (ClientStub) Proxy.getInvocationHandler(server);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    protected Board getBoard() {
        return game.getBoard();
    }

    protected TilePack getTilePack() {
        return game.getTilePack();
    }

    protected ClientStub getClientStub() {
        return clientStub;
    }

    protected boolean isMe(Player p) {
        return player.equals(p);
    }

    public boolean isAiPlayerActive() {
        if (server == null) return false;
        return player.equals(game.getActivePlayer());
    }

    @Override
    public void showWarning(String title, String message) {
        //do nothing
    }

    protected void handleRuntimeError(Exception e) {
        logger.error("AI player exception", e);
    }

    // dummy implementations

    protected final void selectDummyAction(List<PlayerAction> actions, boolean canPass) {
        for (PlayerAction action : actions) {
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
        getServer().pass();
    }

    protected boolean selectDummyAbbeyPlacement(AbbeyPlacementAction action) {
        getServer().pass();
        return true;
    }

    protected boolean selectDummyTilePlacement(TilePlacementAction action) {
        Position nearest = null, p0 = new Position(0, 0);
        int min = Integer.MAX_VALUE;
        for (Position pos : action.getAvailablePlacements().keySet()) {
            int dist = pos.squareDistance(p0);
            if (dist < min) {
                min = dist;
                nearest = pos;
            }
        }
        getServer().placeTile(action.getAvailablePlacements().get(nearest).iterator().next(), nearest);
        return true;
    }

    protected boolean selectDummyMeepleAction(MeepleAction ma) {
        Position p = ma.getLocationsMap().keySet().iterator().next();
        for (Location loc : ma.getLocationsMap().get(p)) {
            Feature f = getBoard().get(p).getFeature(loc);
            if (f instanceof City || f instanceof Road || f instanceof Cloister) {
                getServer().deployMeeple(p, loc, ma.getMeepleType());
                return true;
            }
        }
        return false;
    }

    protected boolean selectDummyTowerCapture(TakePrisonerAction action) {
        Position p = action.getLocationsMap().keySet().iterator().next();
        Location loc = action.getLocationsMap().get(p).iterator().next();
        Meeple m = getBoard().get(p).getFeature(loc).getMeeples().get(0);
        getServer().takePrisoner(p, loc, m.getClass(), m.getPlayer().getIndex());
        return true;
    }

    protected final void selectDummyDragonMove(Set<Position> positions, int movesLeft) {
        getServer().moveDragon(positions.iterator().next());
    }

    @Override
    public void chatMessage(Player player, String message) {
    }

    @Override
    public String toString() {
        return String.valueOf(player);
    }

}
