package com.jcloisterzone.wsio;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.NeutralFigure;

/**
 * Legacy RMI interface, all calls are translated to RMI command
 */
public interface RmiProxy {

    public void pass();
    public void placeTile(Rotation rotation, Position position);

    public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType);
    public void undeployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType, Integer meepleOwner);
    public void moveNeutralFigure(FeaturePointer fp, Class<? extends NeutralFigure> figureType);

    public void placeTowerPiece(Position pos);
    public void takePrisoner(FeaturePointer fp, Class<? extends Meeple> meepleType, Integer meepleOwner);
    public void placeTunnelPiece(FeaturePointer fp, boolean isSecondPiece);


    public void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType);

    public void deployBridge(Position pos, Location loc); //TODO use FeaturePointer
    public void deployCastle(Position pos, Location loc); //TODO use FeaturePointer

    public void bazaarBid(Integer supplyIndex, Integer price);
    public void bazaarBuyOrSell(boolean buy);

    public void cornCiclesRemoveOrDeploy(boolean remove);
    public void placeLittleBuilding(LittleBuilding lbType);
    public void placeGoldPiece(Position pos);
}
