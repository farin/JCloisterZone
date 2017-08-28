package com.jcloisterzone.wsio;

/**
 * Legacy RMI interface, all calls are translated to RMI command
 */
@Deprecated //TO DEL USED JUST AS REFERENCE
public interface RmiProxy {

    //public void pass();
    //public void placeTile(Rotation rotation, Position position);

    //public void deployMeeple(FeaturePointer fp, Class<? extends Meeple> meepleType);
    //public void undeployMeeple(MeeplePointer mp);
    //public void moveNeutralFigure(BoardPointer ptr, Class<? extends NeutralFigure> figureType);

    //TODO replace with generic  placeToken (use for gold, little building, maybe bridge and castle)
//    public void placeTowerPiece(Position pos);
//    public void placeTunnelPiece(FeaturePointer fp, boolean isSecondPiece);
//
//    public void takePrisoner(MeeplePointer mp);
//    public void payRansom(Integer playerIndexToPay, Class<? extends Follower> meepleType);
//
//    public void deployBridge(Position pos, Location loc); //TODO use FeaturePointer
//    public void deployCastle(Position pos, Location loc); //TODO use FeaturePointer
//
//    public void bazaarBid(Integer supplyIndex, Integer price);
//    public void bazaarBuyOrSell(boolean buy);
//
//    public void cornCiclesRemoveOrDeploy(boolean remove);
//    public void placeLittleBuilding(LittleBuilding lbType);
//    public void placeGoldPiece(Position pos);
}
