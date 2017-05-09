package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.game.state.GameState;

public class NeutralFigure<T extends BoardPointer> extends Figure<T> {

    private static final long serialVersionUID = 1L;

    public NeutralFigure(String id) {
        super(id);
    }

    @SuppressWarnings("unchecked")
    public T getDeployment(GameState state) {
        return (T) state.getNeutralFigures().getDeployedNeutralFigures().get(this).getOrNull();
    }

    @Override
    public boolean at(GameState state, Feature feature) {
        BoardPointer ptr = getDeployment(state);
        if (ptr == null) {
            return false;
        }
        FeaturePointer fp = ptr.asFeaturePointer();
        return feature.getPlaces().contains(fp);
    }


//    @Override
//    public void deploy(T at) {
//        T origin = getDeployment();
//        game.replaceState(state -> {
//            LinkedHashMap<NeutralFigure<?>, BoardPointer> deployedNeutralFigures = state.getDeployedNeutralFigures();
//            return state.setDeployedNeutralFigures(deployedNeutralFigures.put(this, at));
//        });
//        game.post(new NeutralFigureMoveEvent(game.getActivePlayer(), this, origin, at));
//    }

}
