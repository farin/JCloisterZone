package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;

public class MeepleAction extends SelectFeatureAction {

    private static final long serialVersionUID = 1L;

    private final String meepleId;
    private final Class<? extends Meeple> meepleType;
    private boolean cityOfCarcassoneMove;

    public MeepleAction(Meeple meeple, Set<FeaturePointer> options) {
        this(meeple.getId(), meeple.getClass(), options);
    }

    public MeepleAction(String meepleId, Class<? extends Meeple> meepleType, Set<FeaturePointer> options) {
        super(options);
        this.meepleId = meepleId;
        this.meepleType = meepleType;
    }

    public String getMeepleId() {
        return meepleId;
    }

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    public boolean isCityOfCarcassoneMove() {
        return cityOfCarcassoneMove;
    }

    public void setCityOfCarcassoneMove(boolean cityOfCarcassoneMove) {
        this.cityOfCarcassoneMove = cityOfCarcassoneMove;
    }

    @Override
    public WsInGameMessage select(FeaturePointer fp) {
        return new DeployMeepleMessage(fp, meepleId);
    }

    @Override
    public String toString() {
        return "place " + meepleType.getSimpleName();
    }
}
