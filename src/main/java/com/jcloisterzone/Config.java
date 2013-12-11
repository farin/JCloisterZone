package com.jcloisterzone;

import java.util.List;
import java.util.Map;

public class Config {

    private String update;
    private Integer port;
    private String locale;

    private Integer score_display_duration;
    private Integer ai_place_tile_delay;

    private Boolean beep_alert;

    private List<String> plugins;
    private ConfirmConfig confirm; //TODO class
    private PlayersConfig players;
    private DebugConfig debug;
    private Map<String, Object> profiles;

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
            return farm_place;
        }
        public void setFarm_place(Boolean farm_place) {
            this.farm_place = farm_place;
        }
        public Boolean getTower_place() {
            return tower_place;
        }
        public void setTower_place(Boolean tower_place) {
            this.tower_place = tower_place;
        }
        public Boolean getGame_close() {
            return game_close;
        }
        public void setGame_close(Boolean game_close) {
            this.game_close = game_close;
        }
        public Boolean getRansom_payment() {
            return ransom_payment;
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
            return colors;
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
        return beep_alert;
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
        return confirm;
    }

    public void setConfirm(ConfirmConfig confirm) {
        this.confirm = confirm;
    }

    public Map<String, Object> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, Object> profiles) {
        this.profiles = profiles;
    }








//
//
//    class Confirmations
//
//    confirm:
//      farm_place: false
//      tower_place: true
//      game_close: true
//      ransom_payment: true
//
//    players:
//      colors: # Colors as Java awt.Color constant or in hex-value. (third-party themes can ignore these colors)
//        - RED
//        - "#008ffe"
//        - YELLOW
//        - "#009900"
//        - BLACK
//        - "#808000"
//
//      names: [] # You can declare default player names
//      ai_names: [ Adda, Ellen, Caitlyn, Riannon, Tankred, Rigatona ]
//
//    plugins:
//      - plugins/classic.jar # Graphics from original board game
//      #- plugins/rgg_siege.jar # RGG's Siege tiles instead of original The Cathars tiles
//
//    profiles:
//      default:
//        expansions:
//          #- WINTER
//          #- INNS_AND_CATHEDRALS
//          #- TRADERS_AND_BUILDERS
//          #- PRINCESS_AND_DRAGON
//          #- TOWER
//          #- ABBEY_AND_MAYOR
//          #- BRIDGES_CASTLES_AND_BAZAARS
//
//          #- CATAPULT
//          #- KING_AND_SCOUT
//          #- RIVER
//          #- RIVER_II
//          #- CATHARS= false
//          #- COUNT= false
//          #- GQ11= false
//          #- CULT
//          #- TUNNEL
//          #- CORN_CIRCLES
//          #- PLAGUE
//          #- FESTIVAL
//          #- PHANTOM
//          #- WIND_ROSE
//
//          #- FLIER
//          #- CORN_CIRCLES_II
//        rules:
//          #- TINY_CITY_2_POINTS
//          #- FARM_CITY_SCORED_ONCE
//          #- CANNOT_PLACE_BUILDER_ON_VOLCANO
//          #- PRINCESS_MUST_REMOVE_KNIGHT
//          - PIG_HERD_ON_GQ_FARM
//          #- MULTI_BARN_ALLOWEDD
//          - TUNNELIZE_ALL_EXPANSIONS
//          #- BAZAAR_NO_AUCTION



}
