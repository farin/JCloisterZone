package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.pointer.FeaturePointer;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class BigTopCapabilityModel {

    private final Map<FeaturePointer, List<BigTopToken>> placedTokens;

    public BigTopCapabilityModel(Map<FeaturePointer, List<BigTopToken>> placedTokens) {
        this.placedTokens = placedTokens;
    }

    public Map<FeaturePointer, List<BigTopToken>> getPlacedTokens() {
        return placedTokens;
    }

    public BigTopCapabilityModel setPlacedTokens(Map<FeaturePointer, List<BigTopToken>> placedTokens) {
        return new BigTopCapabilityModel(placedTokens);
    }

}
