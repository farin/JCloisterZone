package com.jcloisterzone.feature;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;

public class Watchtower extends TileFeature implements Structure {

	public enum WatchtowerType {

		MONASTERY("monastery",3),
		CITY("city",1),
		ROAD("road",1),
		MEEPLE("meeple",2),
		PENNANT("pennant",2);

	    private String type;
	    private int points;
	    
	    /**
	     * Instantiates a new {@code WatchtowerType}.
	     *
	     * @param type of watchtower
	     * @param points of watchtower bonus
	     */
	    WatchtowerType(String type, int points) {
	        this.type = type;
	        this.points = points;
	    }

	    /**
	     * Gets the {@code type} of the instance.
	     * @return the {@code type} of the instance
	     */
	    public String getType() {
	        return type;
	    }

	    /**
	     * Gets the {@code points} of the instance.
	     * @return the {@code points} of the instance
	     */
	    public int getPoints() {
	        return points;
	    }

	    /**
	     * Gets the instance with the given {@code type}.
	     * @param type the type to search
	     * @return the instance with the given {@code type}
	     * @throws IllegalArgumentException if {@code type} does not match any instance
	     */
	    public static WatchtowerType forType(String type) {
	        for (WatchtowerType e : values()) {
	            if (e.type.equals(type)) return e;
	        }
	        throw new IllegalArgumentException("Invalid WatchtowerType type " + type);
	    }
	}

    private static final List<FeaturePointer> INITIAL_PLACE = List.of(new FeaturePointer(Position.ZERO, Location.TOWER));
    private final WatchtowerType type;

    public Watchtower(WatchtowerType type) {
        this(INITIAL_PLACE, type);
    }

    public Watchtower(List<FeaturePointer> places, WatchtowerType type) {
        super(places);
        this.type = type;
    }

    @Override
    public Watchtower placeOnBoard(Position pos, Rotation rot) {
        return new Watchtower(placeOnBoardPlaces(pos, rot), type);
    }

    public WatchtowerType getType() {
    	return type;
    }

    public static String name() {
        return "Watchtower";
    }
}
