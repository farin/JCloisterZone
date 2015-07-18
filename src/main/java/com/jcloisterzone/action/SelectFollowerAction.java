package com.jcloisterzone.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.layer.FollowerAreaLayer;


public abstract class SelectFollowerAction extends PlayerAction<MeeplePointer> {

    public SelectFollowerAction(String name) {
        super(name);
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return FollowerAreaLayer.class;
    }

    //temporary legacy, TODO direct meeple selection on client

    public Map<Position, List<MeeplePointer>> groupByPosition() {
        Map<Position, List<MeeplePointer>> map = new HashMap<>();
        for (MeeplePointer mp: options) {
            List<MeeplePointer> pointers = map.get(mp.getPosition());
            if (pointers == null) {
                pointers = new ArrayList<>();
                map.put(mp.getPosition(), pointers);
            }
            pointers.add(mp);
        }
        return map;
    }

    //TODO direct implementation
    public List<MeeplePointer> getMeeplePointers(Position p) {
        List<MeeplePointer> pointers = groupByPosition().get(p);
        if (pointers == null) return Collections.emptyList();
        return pointers;
    }

}
