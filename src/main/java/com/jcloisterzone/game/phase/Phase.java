package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.rmi.ClientIF;


public abstract class Phase implements ClientIF {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Game game;

    private boolean entered;
    private Phase defaultNext;

    public Phase(Game game) {
        this.game = game;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public Phase getDefaultNext() {
        return defaultNext;
    }

    public void setDefaultNext(Phase defaultNext) {
        this.defaultNext = defaultNext;
    }

    public void next() {
        game.setPhase(defaultNext);
    }

    public void next(Class<? extends Phase> phaseClass) {
        game.setPhase(game.getPhases().get(phaseClass));
    }

    public void enter() { }

    /**
     * Method is invoked on active phase when user buy back inprisoned follower
     */
    public void notifyRansomPaid() {
        //do nothing by default
    }

    public boolean isActive() {
        return true;
    }

    //shortcuts

    protected TilePack getTilePack() {
        return game.getTilePack();
    }
    protected Board getBoard() {
        return game.getBoard();
    }

    protected Tile getTile() {
        return game.getCurrentTile();
    }

    public Player getActivePlayer() {
        return game.getTurnPlayer();
    }


    protected void notifyUI(List<PlayerAction> actions, boolean canPass) {
        game.getUserInterface().selectAction(actions, canPass);
    }

    protected void notifyUI(PlayerAction action, boolean canPass) {
        game.getUserInterface().selectAction(Collections.singletonList(action), canPass);
    }

    /** handler called after game is load if this phase is active */
    public void loadGame(Snapshot snapshot) {
        //do nothing by default
    }

    //adapter methods

    @Override
    public void startGame() {
        logger.error(Application.ILLEGAL_STATE_MSG, "startGame");
    }

    @Override
    public void pass() {
        logger.error(Application.ILLEGAL_STATE_MSG, "pass");
    }

    @Override
    public void placeTile(Rotation  rotation, Position position) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeTile");
    }

    @Override
    public void deployMeeple(Position p,  Location loc, Class<? extends Meeple> meepleType) {
        logger.error(Application.ILLEGAL_STATE_MSG, "deployMeeple");
    }

    @Override
    public void moveFairy(Position p) {
        logger.error(Application.ILLEGAL_STATE_MSG, "moveFairy");
    }

    @Override
    public void placeTowerPiece(Position p) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeTowerPiece");
    }

    @Override
    public void placeTunnelPiece(Position p, Location loc, boolean isSecondPiece) {
        logger.error(Application.ILLEGAL_STATE_MSG, "placeTunnelPiece");
    }

    @Override
    public void undeployMeeple(Position p, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        logger.error(Application.ILLEGAL_STATE_MSG, "undeployMeeple");
    }

    @Override
    public void moveDragon(Position p) {
        logger.error(Application.ILLEGAL_STATE_MSG, "moveDragon");
    }

    @Override
    public final void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType) {
        //pay ransom is valid any time
        TowerCapability towerCap = game.getCapability(TowerCapability.class);
        if (towerCap == null) {
            logger.error(Application.ILLEGAL_STATE_MSG, "payRansom");
            return;
        }
        towerCap.payRansom(playerIndexToPay, meepleType);
    }

    @Override
    public void updateCustomRule(CustomRule rule, Boolean enabled) {
        logger.error(Application.ILLEGAL_STATE_MSG, "updateCustomRule");
    }
    @Override
    public void updateExpansion(Expansion expansion, Boolean enabled) {
        logger.error(Application.ILLEGAL_STATE_MSG, "updateExpansion");

    }
    @Override
    public void updateSlot(PlayerSlot slot) {
        logger.error(Application.ILLEGAL_STATE_MSG, "updateSlot");
    }

    @Override
    public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
        logger.error(Application.ILLEGAL_STATE_MSG, "updateSupportedExpansions");
    }


    @Override
    public void drawTiles(int[] tileIndex) {
        logger.error(Application.ILLEGAL_STATE_MSG, "drawTiles");
    }

    @Override
    public void takePrisoner(Position p, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
         logger.error(Application.ILLEGAL_STATE_MSG, "takePrisoner");
    }

    @Override
    public void deployBridge(Position pos, Location loc) {
        logger.error(Application.ILLEGAL_STATE_MSG, "deployBridge");

    }

    @Override
    public void deployCastle(Position pos, Location loc) {
        logger.error(Application.ILLEGAL_STATE_MSG, "deployCastle");
    }

    @Override
    public void bazaarBid(Integer supplyIndex, Integer price) {
        logger.error(Application.ILLEGAL_STATE_MSG, "bazaarBid");
    }

    @Override
    public void bazaarBuyOrSell(boolean buy) {
        logger.error(Application.ILLEGAL_STATE_MSG, "bazaarBuyOrSell");
    }

    @Override
    public void cornCiclesRemoveOrDeploy(boolean remove) {
        logger.error(Application.ILLEGAL_STATE_MSG, "cornCiclesRemoveOrDeploy");
    }

    @Override
    public void setFlierDistance(int distance) {
        logger.error(Application.ILLEGAL_STATE_MSG, "setFlierDistance");
    }

    @Override
    public final void chatMessage(Integer author, String message) {
        game.getUserInterface().chatMessage(game.getPlayer(author), message);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
