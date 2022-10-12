package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Position;
import io.vavr.collection.Vector;

public class BlackDragonCapabilityModel {

    private final Vector<Position> visited;
    private final int moves;

    public BlackDragonCapabilityModel(Vector<Position> visited, int moves) {
        this.visited = visited;
        this.moves = moves;
    }

    public Vector<Position> getVisited() {
        return visited;
    }

    public BlackDragonCapabilityModel setVisited(Vector<Position> visited) {
        return new BlackDragonCapabilityModel(visited, moves);
    }

    public int getMoves() {
        return moves;
    }

    public BlackDragonCapabilityModel setMoves(int moves) {
        return new BlackDragonCapabilityModel(visited, moves);
    }
}
