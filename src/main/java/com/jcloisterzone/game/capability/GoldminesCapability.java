package com.jcloisterzone.game.capability;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class GoldminesCapability  extends Capability {

    private final Map<Position, Integer> boardGold = new HashMap<>();
    private final Map<Player, Integer> playerGold = new HashMap<>();

    public GoldminesCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        Object[] a = new Object[2];
        a[0] = new HashMap<>(boardGold);
        a[1] = new HashMap<>(playerGold);
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(Object data) {
        Object[] a = (Object[]) data;
        Map<Position, Integer> boardBackup = (Map<Position, Integer>) a[0];
        boardGold.clear();
        boardGold.putAll(boardBackup);
        Map<Player, Integer> playerBackup = (Map<Player, Integer>) a[1];
        playerGold.clear();
        playerGold.putAll(playerBackup);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("goldmine").getLength() > 0) {
            tile.setTrigger(TileTrigger.GOLDMINE);
        }
    }


    public int addGold(Position pos) {
        Integer prev = boardGold.get(pos);
        Integer curr = prev == null ? 1 : prev + 1;
        boardGold.put(pos, curr);
        return curr;
    }
}
