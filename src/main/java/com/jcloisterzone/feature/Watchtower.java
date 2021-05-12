package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class Watchtower extends TileFeature implements Structure {

    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.TOWER));
    Map<String, Integer> modifiers;
    
    public Watchtower(Map<String, Integer> modifiers) {
        this(INITIAL_PLACE, modifiers);
    }

    public Watchtower(List<FeaturePointer> places, Map<String, Integer> modifiers) {
        super(places);
        this.modifiers = modifiers;
    }

    public Map<String, Integer> getModifiers() {
        return modifiers;
    }

    public Watchtower setModifier(String type, int count) {
    	if (modifiers.containsKey(type) && modifiers.get(type).equals(count)) return this;
    	return new Watchtower(places, modifiers.put(type,count));
    }
    
    @Override
    public Watchtower placeOnBoard(Position pos, Rotation rot) {
        return new Watchtower(placeOnBoardPlaces(pos, rot), modifiers);
    }

    public static String name() {
        return "Watchtower";
    }
}
