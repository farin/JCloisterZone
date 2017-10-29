package com.jcloisterzone.figure;

import java.util.HashMap;

import com.jcloisterzone.Player;

public class MeepleIdProvider {

    private final Player player;
    private final HashMap<Class<? extends Meeple>, Integer> ids;

    public MeepleIdProvider(Player player) {
        this.player = player;
        this.ids = new HashMap<>();
    }

    public String generateId(Class<? extends Meeple> clazz) {
        Integer n = ids.get(clazz);
        if (n == null) {
            n = 1;
            ids.put(clazz, 1);
        } else {
            n++;
            ids.put(clazz, n);
        }
        String type = clazz.getSimpleName().toLowerCase().replace("follower", "");
        return String.format("%s.%s.%s", player.getIndex(), type, n);
    }

}
