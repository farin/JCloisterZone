package com.jcloisterzone.ai;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.wsio.RmiProxy;

public class DelayedServer implements RmiProxy {

    private final RmiProxy server;
    private final int placeTileDelay;

    public DelayedServer(RmiProxy server, int placeTileDelay) {
        this.server = server;
        this.placeTileDelay = placeTileDelay;
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

    @Override
    public void placeLittleBuilding(LittleBuilding lbType) {
        server.placeLittleBuilding(lbType);
    }
}