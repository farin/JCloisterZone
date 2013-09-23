package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.GameExtension;

public class PigCapability extends GameExtension {

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Pig(game, player));
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        if (getTile().getTrigger() == TileTrigger.VOLCANO &&
            getGame().hasRule(CustomRule.CANNOT_PLACE_BUILDER_ON_VOLCANO)) return;

        Player player = game.getActivePlayer();
        Position pos = getTile().getPosition();
        if (player.hasSpecialMeeple(Pig.class)) {
            MeepleAction meepleAction = new MeepleAction(Pig.class);
            Set<Location> dirs = getTile().getPlayerFeatures(player, Farm.class);
            if (! dirs.isEmpty()) meepleAction.getOrCreate(pos).addAll(dirs);
            if (! meepleAction.getSites().isEmpty()) actions.add(meepleAction);
        }
    }

}
