package com.jcloisterzone.ui.theme;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Tower;

public class FeatureDescriptor {

    public static final String EVERY = "*";

    private final String tileId;
    private final Class<? extends Feature> featureType;
    private final Location location;

    public FeatureDescriptor(String tileId, Class<? extends Feature> featureType, Location location) {
        this.tileId = tileId;
        this.featureType = featureType;
        this.location = location;
    }

    public FeatureDescriptor(Tile tile, Class<? extends Feature> featureType, Location location) {
        this(tile.getId(), featureType, location);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tileId).append(' ');
        sb.append(featureType.getSimpleName().toUpperCase()).append(' ');
        sb.append(location.toString());
        return sb.toString();
    }

    public static FeatureDescriptor valueOf(String descriptor) {
        String[] tokens = descriptor.split(" ");
        return FeatureDescriptor.valueOf(tokens[0], tokens[1], tokens[2]);
    }

    public static FeatureDescriptor valueOf(String tileId, String featureName, String locationName) {
        Class<? extends Feature> featureType;
        switch (featureName) {
        case "ROAD": featureType = Road.class; break;
        case "CITY": featureType = City.class; break;
        case "FARM": featureType = Farm.class; break;
        case "CLOISTER": featureType = Cloister.class; break;
        case "TOWER": featureType = Tower.class; break;
        case "CASTLE": featureType = Castle.class; break;
        case "BRIDGE": featureType = Bridge.class; break;
        default: throw new IllegalArgumentException("Unsupported feature "+featureName);
        }
        Location location = Location.valueOf(locationName);
        assert location.isFarmLocation() ^ !featureType.equals(Farm.class) : "improper location "+locationName;
        return new FeatureDescriptor(tileId, featureType, location);
    }

    public String getTileId() {
        return tileId;
    }

    public Class<? extends Feature> getFeatureType() {
        return featureType;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + featureType.toString().hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + tileId.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FeatureDescriptor))
            return false;
        FeatureDescriptor other = (FeatureDescriptor) obj;
        if (featureType == null) {
            if (other.featureType != null)
                return false;
        } else if (!featureType.equals(other.featureType))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (tileId == null) {
            if (other.tileId != null)
                return false;
        } else if (!tileId.equals(other.tileId))
            return false;
        return true;
    }





}
