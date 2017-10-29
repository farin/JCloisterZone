package com.jcloisterzone.game.state;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.neutral.Witch;

import io.vavr.collection.LinkedHashMap;

@Immutable
public class NeutralFiguresState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Dragon dragon;
    private final Fairy fairy;
    private final Mage mage;
    private final Witch witch;
    private final Count count;

    private final LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures;

    public NeutralFiguresState() {
        this(null, null, null, null, null, LinkedHashMap.empty());
    }

    public NeutralFiguresState(
        Dragon dragon, Fairy fairy, Mage mage, Witch witch, Count count,
        LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures
    ) {
        this.dragon = dragon;
        this.fairy = fairy;
        this.mage = mage;
        this.witch = witch;
        this.count = count;
        this.deployedNeutralFigures = deployedNeutralFigures;
    }

    public NeutralFiguresState setDragon(Dragon dragon) {
        return new NeutralFiguresState(dragon, fairy, mage, witch, count, deployedNeutralFigures);
    }

    public NeutralFiguresState setFairy(Fairy fairy) {
        return new NeutralFiguresState(dragon, fairy, mage, witch, count, deployedNeutralFigures);
    }

    public NeutralFiguresState setMage(Mage mage) {
        return new NeutralFiguresState(dragon, fairy, mage, witch, count, deployedNeutralFigures);
    }

    public NeutralFiguresState setWitch(Witch witch) {
        return new NeutralFiguresState(dragon, fairy, mage, witch, count, deployedNeutralFigures);
    }

    public NeutralFiguresState setCount(Count count) {
        return new NeutralFiguresState(dragon, fairy, mage, witch, count, deployedNeutralFigures);
    }

    public NeutralFiguresState setDeployedNeutralFigures(LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures) {
        return new NeutralFiguresState(dragon, fairy, mage, witch, count, deployedNeutralFigures);
    }

    public NeutralFigure<?> getById(String figureId) {
        if (dragon != null && figureId.equals(dragon.getId())) return dragon;
        if (fairy != null && figureId.equals(fairy.getId())) return fairy;
        if (mage != null && figureId.equals(mage.getId())) return mage;
        if (witch != null && figureId.equals(witch.getId())) return witch;
        if (count != null && figureId.equals(count.getId())) return count;
        return null;
    }

    public Dragon getDragon() {
        return dragon;
    }

    public Position getDragonDeployment() {
        return (Position) deployedNeutralFigures.get(dragon).getOrNull();
    }

    public Fairy getFairy() {
        return fairy;
    }

    public BoardPointer getFairyDeployment() {
        return deployedNeutralFigures.get(fairy).getOrNull();
    }

    public Mage getMage() {
        return mage;
    }

    public FeaturePointer getMageDeployment() {
        return (FeaturePointer) deployedNeutralFigures.get(mage).getOrNull();
    }

    public Witch getWitch() {
        return witch;
    }

    public FeaturePointer getWitchDeployment() {
        return (FeaturePointer) deployedNeutralFigures.get(witch).getOrNull();
    }

    public Count getCount() {
        return count;
    }

    public FeaturePointer getCountDeployment() {
        return (FeaturePointer) deployedNeutralFigures.get(count).getOrNull();
    }

    public LinkedHashMap<NeutralFigure<?>, BoardPointer> getDeployedNeutralFigures() {
        return deployedNeutralFigures;
    }

}
