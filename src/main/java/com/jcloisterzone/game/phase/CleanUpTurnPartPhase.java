package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.config.Config.ConfirmConfig;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.RequestConfirmEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.EndTurnMessage;

/**
 *  end of turn part. For double turn, second part starts otherways proceed to real end of turn
 */
public class CleanUpTurnPartPhase extends ServerAwarePhase {

    private final BuilderCapability builderCap;

    public CleanUpTurnPartPhase(Game game, GameController gc) {
        super(game, gc);
        builderCap = game.getCapability(BuilderCapability.class);
    }

    @Override
    public void enter() {
        if (isLocalPlayer(getActivePlayer())) {
            boolean needsConfirm = false;
            if (game.getLastUndoable() instanceof MeepleEvent) {
                ConfirmConfig cfg =  getConfig().getConfirm();
                MeepleEvent ev = (MeepleEvent) game.getLastUndoable();
                if (cfg.getAny_deployment()) {
                    needsConfirm = true;
                } else if (cfg.getFarm_deployment() && ev.getTo().getLocation().isFarmLocation()) {
                    needsConfirm = true;
                } else if (cfg.getOn_tower_deployment() && ev.getTo().getLocation() == Location.TOWER) {
                    needsConfirm = true;
                }
            }
            if (needsConfirm) {
                game.post(new RequestConfirmEvent(getActivePlayer()));
            } else {
                getConnection().send(new EndTurnMessage(game.getGameId()));
            }
        }
    }

    @WsSubscribe
    public void handleEndTurn(EndTurnMessage msg) {
        game.updateRandomSeed(msg.getCurrentTime());
        boolean builderTakeAnotherTurn = builderCap != null && builderCap.hasPlayerAnotherTurn();
        if (getTile() != null) { //after last turn, abbeys can be placed, then cycling through players and tile can be null. Do not delegate on capabilities in such case
            game.turnPartCleanUp();
            game.setCurrentTile(null);
        }
        if (builderTakeAnotherTurn) {
            next(game.hasCapability(AbbeyCapability.class) ? AbbeyPhase.class : DrawPhase.class);
        } else {
            next();
        }
    }

}
