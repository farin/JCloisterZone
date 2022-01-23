package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class SheepCapabilityModel {

    private final Map<FeaturePointer, List<SheepToken>> placedTokens;
    private final List<MeeplePointer> unresolvedFlocks;

    public SheepCapabilityModel(Map<FeaturePointer, List<SheepToken>> placedTokens, List<MeeplePointer> unresolvedFlocks) {
        this.placedTokens = placedTokens;
        this.unresolvedFlocks = unresolvedFlocks;
    }

    public Map<FeaturePointer, List<SheepToken>> getPlacedTokens() {
        return placedTokens;
    }

    public SheepCapabilityModel setPlacedTokens(Map<FeaturePointer, List<SheepToken>> placedTokens) {
        return new SheepCapabilityModel(placedTokens, this.unresolvedFlocks);
    }

    public List<MeeplePointer> getUnresolvedFlocks() {
        return unresolvedFlocks;
    }

    public SheepCapabilityModel setUnresolvedFlocks(List<MeeplePointer> unresolvedFlocks) {
        return new SheepCapabilityModel(this.placedTokens, unresolvedFlocks);
    }
}
