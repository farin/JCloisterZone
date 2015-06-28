package com.jcloisterzone.board.pointer;

import java.io.Serializable;

import com.jcloisterzone.board.Position;


public interface BoardPointer extends Serializable {

    public Position getPosition();
    public FeaturePointer asFeaturePointer();
}
