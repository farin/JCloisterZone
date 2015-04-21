package com.jcloisterzone.ui.panel;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.PlayersConfig;

public class NameProvider {

    private static class ReservedName {
        String name;
        Integer slot;

        public ReservedName(String name, Integer slot) {
            this.name = name;
            this.slot = slot;
        }
    }

    private List<ReservedName> aiNames = new ArrayList<>();
    private List<ReservedName> playerNames = new ArrayList<>();


    public NameProvider(Config config) {
        PlayersConfig playersConfig = config.getPlayers();
        initNames(playerNames, playersConfig == null ? null : playersConfig.getNames());
        initNames(aiNames, playersConfig == null ? null : playersConfig.getAi_names());
    }

    private void initNames(List<ReservedName> lrn, List<String> names) {
        if (names != null) {
            for (String name: names) {
                lrn.add(new ReservedName(name, null));
            }
        }
    }

    synchronized
    public String reserveName(boolean ai, int slot) {
        List<ReservedName> lrn = ai ? aiNames : playerNames;
        for (ReservedName rn : lrn) {
            if (rn.slot == null) {
                rn.slot = slot;
                return rn.name;
            }
        }
        return "";
    }

    synchronized
    public void releaseName(boolean ai, int slot) {
        List<ReservedName> lrn = ai ? aiNames : playerNames;
        for (ReservedName rn : lrn) {
            if (rn.slot != null && rn.slot == slot) { //autoboxing, must check for null
                rn.slot = null;
                return;
            }
        }
    }

}
