package com.jcloisterzone.ai;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;

public class PositionRanking {

	private double rank;

	private Position position;
	private Rotation rotation;

	private PlayerAction action;
	private Position actionPosition;
	private Location actionLocation;

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

	public PlayerAction getAction() {
		return action;
	}

	public void setAction(PlayerAction action) {
		this.action = action;
	}

	public Position getActionPosition() {
		return actionPosition;
	}

	public void setActionPosition(Position actionPosition) {
		this.actionPosition = actionPosition;
	}

	public Location getActionLocation() {
		return actionLocation;
	}

	public void setActionLocation(Location actionLocation) {
		this.actionLocation = actionLocation;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(rank);
		sb.append(" Pos: ").append(position);
		sb.append(" Rot: ").append(rotation);
		if (action instanceof MeepleAction) {
			sb.append(" Meeple: ").append(((MeepleAction)action).getMeepleType().getSimpleName());
			sb.append(" APos:").append(actionPosition);
			sb.append(" ALoc:").append(actionLocation);
		}
		return sb.toString();
	}



}

