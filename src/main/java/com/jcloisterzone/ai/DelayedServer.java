package com.jcloisterzone.ai;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.rmi.ServerIF;

public class DelayedServer implements ServerIF {

    private final ServerIF server;
    private final int placeTileDelay;

    public DelayedServer(ServerIF server, int placeTileDelay) {
        this.server = server;
        this.placeTileDelay = placeTileDelay;
    }

    @Override
    public void updateSlot(PlayerSlot slot,
            EnumSet<Expansion> supportedExpansions) {
        server.updateSlot(slot, supportedExpansions);
    }

    @Override
    public void selectTiles(int tilesCount, int drawCount) {
        server.selectTiles(tilesCount, drawCount);
    }

    @Override
    public void rollFlierDice() {
        server.rollFlierDice();
    }

    @Override
    public void updateExpansion(Expansion expansion, Boolean enabled) {
        server.updateExpansion(expansion, enabled);
    }

    @Override
    public void updateCustomRule(CustomRule rule, Boolean enabled) {
        server.updateCustomRule(rule, enabled);
    }

    @Override
    public void startGame() {
        server.startGame();
    }

    @Override
    public void pass() {
        server.pass();
    }

    @Override
    public void placeTile(Rotation rotation, Position position) {
        try {
            Thread.sleep(placeTileDelay);
        } catch (InterruptedException e) {}
        server.placeTile(rotation, position);
    }

    @Override
    public void deployMeeple(Position pos, Location loc,
            Class<? extends Meeple> meepleType) {
        server.deployMeeple(pos, loc, meepleType);
    }

    @Override
    public void undeployMeeple(Position pos, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        server.undeployMeeple(pos, loc, meepleType, meepleOwner);
    }

    @Override
    public void placeTowerPiece(Position pos) {
        server.placeTowerPiece(pos);
    }

    @Override
    public void takePrisoner(Position pos, Location loc, Class<? extends Meeple> meepleType, Integer meepleOwner) {
        server.takePrisoner(pos, loc, meepleType, meepleOwner);
    }

    @Override
    public void placeTunnelPiece(Position pos, Location loc,
            boolean isSecondPiece) {
        server.placeTunnelPiece(pos, loc, isSecondPiece);
    }

    @Override
    public void moveFairy(Position pos) {
        server.moveFairy(pos);
    }

    @Override
    public void moveDragon(Position pos) {
        try {
            Thread.sleep(placeTileDelay / 2);
        } catch (InterruptedException e) {}
        server.moveDragon(pos);
    }

    @Override
    public void payRansom(Integer playerIndexToPay,
            Class<? extends Follower> meepleType) {
        server.payRansom(playerIndexToPay, meepleType);
    }

    @Override
    public void deployBridge(Position pos, Location loc) {
        server.deployBridge(pos, loc);
    }

    @Override
    public void deployCastle(Position pos, Location loc) {
        server.deployCastle(pos, loc);
    }

    @Override
    public void bazaarBid(Integer supplyIndex, Integer price) {
        server.bazaarBid(supplyIndex, price);
    }

    @Override
    public void bazaarBuyOrSell(boolean buy) {
        server.bazaarBuyOrSell(buy);
    }

    @Override
    public void cornCiclesRemoveOrDeploy(boolean remove) {
        server.cornCiclesRemoveOrDeploy(remove);
    }

    public void chatMessage(Integer player, String message) {
        server.chatMessage(player, message);
    }

}