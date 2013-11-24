package com.jcloisterzone.ai;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.figure.Meeple;

public class PositionRanking {

    public static class SelectedAction {

        public SelectedAction(PlayerAction action) {
            this(action, null, null, null, null);
        }

        public SelectedAction(PlayerAction action, Position position, Location location) {
            this(action, position, location, null, null);
        }

        public SelectedAction(PlayerAction action, Position actionPosition, Location actionLocation, Class<? extends Meeple> meepleType, Player meepleOwner) {
            this.action = action;
            this.position = actionPosition;
            this.location = actionLocation;
            this.meepleType = meepleType;
            this.meepleOwner = meepleOwner;
        }

        public PlayerAction action;
        public Position position;
        public Location location;
        public Class<? extends Meeple> meepleType;
        public Player meepleOwner;
    }

    private double rank;

    private Position position;
    private Rotation rotation;

    private Deque<SelectedAction> selectedActions = new LinkedList<>();


    public PositionRanking(double rank) {
        this.rank = rank;
    }

    public PositionRanking(double rank, Position position, Rotation rotation) {
        this.rank = rank;
        this.position = position;
        this.rotation = rotation;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Deque<SelectedAction> getSelectedActions() {
        return selectedActions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rank);
        sb.append(" Pos: ").append(position);
        sb.append(" Rot: ").append(rotation);
        for (SelectedAction sa : selectedActions) {
            if (sa.action instanceof MeepleAction) {
                sb.append(" Meeple: ").append(((MeepleAction)sa.action).getMeepleType().getSimpleName());
                sb.append(" APos: ").append(sa.position);
                sb.append(" ALoc: ").append(sa.location);
            } else {
                sb.append(" Action: ").append(sa.action.getName());
            }
        }
        return sb.toString();
    }



}

