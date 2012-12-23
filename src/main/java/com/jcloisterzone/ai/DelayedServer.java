package com.jcloisterzone.ai;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
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

    public void updateSlot(PlayerSlot slot,
            EnumSet<Expansion> supportedExpansions) {
        server.updateSlot(slot, supportedExpansions);
    }

    public void selectTiles(int tilesCount, int drawCount) {
        server.selectTiles(tilesCount, drawCount);
    }

    public void updateExpansion(Expansion expansion, Boolean enabled) {
        server.updateExpansion(expansion, enabled);
    }

    public void updateCustomRule(CustomRule rule, Boolean enabled) {
        server.updateCustomRule(rule, enabled);
    }

    public void startGame() {
        server.startGame();
    }

    public void pass() {
        server.pass();
    }

    public void placeTile(Rotation rotation, Position position) {
        try {
            Thread.sleep(placeTileDelay);
        } catch (InterruptedException e) {}
        server.placeTile(rotation, position);
    }

    public void deployMeeple(Position pos, Location loc,
            Class<? extends Meeple> meepleType) {
        server.deployMeeple(pos, loc, meepleType);
    }

    public void undeployMeeple(Position pos, Location loc) {
        server.undeployMeeple(pos, loc);
    }

    public void placeTowerPiece(Position pos) {
        server.placeTowerPiece(pos);
    }

    public void takePrisoner(Position pos, Location loc) {
        server.takePrisoner(pos, loc);
    }

    public void placeTunnelPiece(Position pos, Location loc,
            boolean isSecondPiece) {
        server.placeTunnelPiece(pos, loc, isSecondPiece);
    }

    public void moveFairy(Position pos) {
        server.moveFairy(pos);
    }

    public void moveDragon(Position pos) {
        server.moveDragon(pos);
    }

    public void payRansom(Integer playerIndexToPay,
            Class<? extends Follower> meepleType) {
        server.payRansom(playerIndexToPay, meepleType);
    }

    public void deployBridge(Position pos, Location loc) {
        server.deployBridge(pos, loc);
    }

    public void deployCastle(Position pos, Location loc) {
        server.deployCastle(pos, loc);
    }

    public void bazaarBid(Integer supplyIndex, Integer price) {
        server.bazaarBid(supplyIndex, price);
    }

    public void bazaarBuyOrSell(boolean buy) {
        server.bazaarBuyOrSell(buy);
    }

    @Override
    public void cornCiclesRemoveOrDeploy(boolean remove) {
        server.cornCiclesRemoveOrDeploy(remove);
    }

}