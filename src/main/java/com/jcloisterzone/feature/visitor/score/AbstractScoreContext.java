package com.jcloisterzone.feature.visitor.score;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.visitor.SelfReturningVisitor;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;

public abstract class AbstractScoreContext extends SelfReturningVisitor implements ScoreContext {

    protected final Game game;

    protected final LittleBuildingsCapability lbCap;
    //track already counted buildings (one feature can have two segments on one tile, each invoke one visit
    private final Set<Position> collectedBuildings;
    private final Map<LittleBuilding, Integer> littleBuildings;

    public AbstractScoreContext(Game game) {
        this.game = game;
        lbCap = game.getCapability(LittleBuildingsCapability.class);
        if (lbCap != null) {
            collectedBuildings = new HashSet<>();
            littleBuildings = new HashMap<>();
            for (LittleBuilding lb : LittleBuilding.values()) {
                littleBuildings.put(lb, 0);
            }
        } else {
            collectedBuildings = null;
            littleBuildings = null;
        }
    }

    protected void collectLittleBuildings(Position pos) {
        LittleBuilding lb = lbCap.getPlacedLittleBuilding(pos);
        if (lb != null && !collectedBuildings.contains(pos)) {
            collectedBuildings.add(pos);
            littleBuildings.put(lb, littleBuildings.get(lb) + 1);
        }
    }

    @Override
    public Map<LittleBuilding, Integer> getLittleBuildings() {
        return littleBuildings;
    }

    protected int getLittleBuildingPoints() {
        if (lbCap == null) return 0;
        int points = 0;
        for (Entry<LittleBuilding, Integer> entry : littleBuildings.entrySet()) {
            if (game.getBooleanValue(CustomRule.BULDINGS_DIFFERENT_VALUE)) {
                LittleBuilding lb = entry.getKey();
                switch (lb) {
                    case SHED: points += entry.getValue(); break;
                    case HOUSE: points += 2*entry.getValue(); break;
                    case TOWER: points += 3*entry.getValue(); break;
                }
            } else {
                points += entry.getValue();
            }
        }
        return points;
    }

}
