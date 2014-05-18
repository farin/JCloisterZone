package com.jcloisterzone.ai.step;

import com.jcloisterzone.ai.SavePoint;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.rmi.ServerIF;

public class PassStep extends Step {

    public PassStep(Step previous, SavePoint savePoint) {
        super(previous, savePoint);
    }

    @Override
    public void performLocal(Game game) {
        game.getPhase().pass();
    }

    @Override
    public void performOnServer(ServerIF server) {
        server.pass();
    }

    @Override
    public String toString() {
        return "pass";
    }
}