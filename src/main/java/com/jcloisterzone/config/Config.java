package com.jcloisterzone.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private transient File origin;

    private String update;
    private Integer port;
    private String locale;

    private Integer score_display_duration;
    private Integer ai_place_tile_delay;

    private Boolean beep_alert;

    private List<String> plugins;
    private ConfirmConfig confirm;
    private PlayersConfig players;
    private DebugConfig debug;
    private Map<String, ProfileConfig> profiles;

    public static class ProfileConfig {
        private List<String> expansions;
        private List<String> rules;

        public List<String> getExpansions() {
            return expansions == null ? Collections.<String>emptyList() : expansions;
        }
        public void setExpansions(List<String> expansions) {
            this.expansions = expansions;
        }
        public List<String> getRules() {
            return rules;
        }
        public void setRules(List<String> rules) {
            this.rules = rules;
        }
    }

    public static class AutostartConfig {

        private String profile;
        private List<String> players;

        public String getProfile() {
            return profile;
        }
        public void setProfile(String profile) {
            this.profile = profile;
        }
        public List<String> getPlayers() {
            return players;
        }
        public void setPlayers(List<String> players) {
            this.players = players;
        }
    }

    public static class DebugConfig {
        private String save_format;
        private String autosave;
        private AutostartConfig autostart;
        private Map<String, String> tile_definitions;
        private List<String> draw;
        private List<String> off_capabilities;
        private String area_highlight;

        public boolean isAutostartEnabled() {
            return autostart != null && autostart.getProfile() != null;
        }

        public String getSave_format() {
            return save_format;
        }
        public void setSave_format(String save_format) {
            this.save_format = save_format;
        }
        public String getAutosave() {
            return autosave;
        }
        public void setAutosave(String autosave) {
            this.autosave = autosave;
        }
        public AutostartConfig getAutostart() {
            return autostart;
        }
        public void setAutostart(AutostartConfig autostart) {
            this.autostart = autostart;
        }
        public Map<String, String> getTile_definitions() {
            return tile_definitions;
        }
        public void setTile_definitions(Map<String, String> tile_definitions) {
            this.tile_definitions = tile_definitions;
        }
        public List<String> getDraw() {
            return draw;
        }
        public void setDraw(List<String> draw) {
            this.draw = draw;
        }
        public List<String> getOff_capabilities() {
            return off_capabilities;
        }
        public void setOff_capabilities(List<String> off_capabilities) {
            this.off_capabilities = off_capabilities;
        }
        public String getArea_highlight() {
            return area_highlight;
        }
        public void setArea_highlight(String area_highlight) {
            this.area_highlight = area_highlight;
        }
    }

    public static class ConfirmConfig {
        private Boolean farm_place;
        private Boolean tower_place;
        private Boolean game_close;
        private Boolean ransom_payment;

        public Boolean getFarm_place() {
            return farm_place == null ? Boolean.FALSE : farm_place;
        }
        public void setFarm_place(Boolean farm_place) {
            this.farm_place = farm_place;
        }
        public Boolean getTower_place() {
            return tower_place == null ? Boolean.FALSE : tower_place;
        }
        public void setTower_place(Boolean tower_place) {
            this.tower_place = tower_place;
        }
        public Boolean getGame_close() {
            return game_close == null ? Boolean.FALSE : game_close;
        }
        public void setGame_close(Boolean game_close) {
            this.game_close = game_close;
        }
        public Boolean getRansom_payment() {
            return ransom_payment == null ? Boolean.FALSE : ransom_payment;
        }
        public void setRansom_payment(Boolean ransom_payment) {
            this.ransom_payment = ransom_payment;
        }
    }

    public static class PlayersConfig {
        private List<String> colors;
        private List<String> names;
        private List<String> ai_names;

        public List<String> getColors() {
            return colors == null ? Collections.<String>emptyList() : colors;
        }
        public void setColors(List<String> colors) {
            this.colors = colors;
        }
        public List<String> getNames() {
            return names;
        }
        public void setNames(List<String> names) {
            this.names = names;
        }
        public List<String> getAi_names() {
            return ai_names;
        }
        public void setAi_names(List<String> ai_names) {
            this.ai_names = ai_names;
        }
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Integer getScore_display_duration() {
        return score_display_duration;
    }

    public void setScore_display_duration(Integer score_display_duration) {
        this.score_display_duration = score_display_duration;
    }

    public Integer getAi_place_tile_delay() {
        return ai_place_tile_delay;
    }

    public void setAi_place_tile_delay(Integer ai_place_tile_delay) {
        this.ai_place_tile_delay = ai_place_tile_delay;
    }

    public Boolean getBeep_alert() {
        return beep_alert == null ? Boolean.FALSE : beep_alert;
    }

    public void setBeep_alert(Boolean beep_alert) {
        this.beep_alert = beep_alert;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public PlayersConfig getPlayers() {
        if (players == null) {
            players = new PlayersConfig();
        }
        return players;
    }

    public void setPlayers(PlayersConfig players) {
        this.players = players;
    }

    public DebugConfig getDebug() {
        return debug;
    }

    public void setDebug(DebugConfig debug) {
        this.debug = debug;
    }

    public ConfirmConfig getConfirm() {
        if (confirm == null) {
            confirm = new ConfirmConfig();
        }
        return confirm;
    }

    public void setConfirm(ConfirmConfig confirm) {
        this.confirm = confirm;
    }

    public Map<String, ProfileConfig> getProfiles() {
        if (profiles == null) {
            profiles = new HashMap<>();
        }
        return profiles;
    }

    public void setProfiles(Map<String, ProfileConfig> profiles) {
        this.profiles = profiles;
    }

    public File getOrigin() {
        return origin;
    }

    public void setOrigin(File origin) {
        this.origin = origin;
    }

}
