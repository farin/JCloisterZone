package com.jcloisterzone.ui.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.PlayersConfig;
import com.jcloisterzone.game.PlayerSlot.SlotType;

public class NameProvider {

    private static class ReservedName {
        String name;
        Integer slot;

        public ReservedName(String name, Integer slot) {
            this.name = name;
            this.slot = slot;
        }
    }

    private Map<SlotType, List<ReservedName>> namesMap = new HashMap<>();


    public NameProvider(Config config) {
        PlayersConfig playersConfig = config.getPlayers();
        initNames(SlotType.PLAYER, playersConfig == null ? null : playersConfig.getNames());
        initNames(SlotType.AI, playersConfig == null ? null : playersConfig.getAi_names());
    }

    private void initNames(SlotType type, List<String> names) {
        List<ReservedName> rn = new ArrayList<>();
        namesMap.put(type, rn);
        if (names != null) {
            for (String name: names) {
                rn.add(new ReservedName(name, null));
            }
        }
    }

    synchronized
    public String reserveName(SlotType type, int slot) {
        for (ReservedName rn : namesMap.get(type)) {
            if (rn.slot == null) {
                rn.slot = slot;
                return rn.name;
            }
        }
        return "";
    }

    synchronized
    public void releaseName(SlotType type, int slot) {
        for (ReservedName rn : namesMap.get(type)) {
            if (rn.slot != null && rn.slot == slot) { //autoboxing, must check for null
                rn.slot = null;
                return;
            }
        }
    }

}
