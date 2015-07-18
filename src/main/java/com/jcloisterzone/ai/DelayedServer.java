package com.jcloisterzone.ai;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.NeutralFigure;
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
    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType) {
        server.deployMeeple(fp, meepleType);
    }

    @Override
    public void undeployMeeple(MeeplePointer mp) {
        server.undeployMeeple(mp);
    }

    @Override
    public void placeTowerPiece(Position pos) {
        server.placeTowerPiece(pos);
    }

    @Override
    public void takePrisoner(MeeplePointer mp) {
        server.takePrisoner(mp);
    }

    @Override
    public void placeTunnelPiece(FeaturePointer fp, boolean isSecondPiece) {
        server.placeTunnelPiece(fp, isSecondPiece);
    }

    @Override
    public void moveNeutralFigure(BoardPointer ptr, Class<? extends NeutralFigure> figureType) {
        if (Dragon.class.equals(figureType)) {
            try {
                Thread.sleep(placeTileDelay / 2);
            } catch (InterruptedException e) {}
        }
        server.moveNeutralFigure(ptr, figureType);
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

    @Override
    public void placeGoldPiece(Position pos) {
        server.placeGoldPiece(pos);
    }
}