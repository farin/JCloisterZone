package com.jcloisterzone.config;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.PlayerColors;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.GameSetupMessage;

/**
 * Snakeyaml not supporting mapping to camel-case properties.
 * So propeties must be underscored.
 *
 */
public class Config {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    private transient File origin;
    /** flag used to construct proper player colors */
    private transient boolean darkTheme;

    private String update;
    private Integer port;
    private String locale;

    private Integer score_display_duration;
    private Integer ai_place_tile_delay;
    private String theme;

    private Boolean beep_alert;
    private String client_name;
    private String client_id;
    private String secret;
    private String play_online_host;

    private List<String> plugins;
    private ConfirmConfig confirm;
    private PlayersConfig players;
    private ScreenshotsConfig screenshots;
    private SavedGamesConfig saved_games;
    private DebugConfig debug;
    private Map<String, PresetConfig> presets;
    private List<String> connection_history;

    public static class ScreenshotsConfig {
        private String folder;
        private Integer scale;

        public String getFolder() {
            return folder;
        }
        public void setFolder(String folder) {
            this.folder = folder;
        }
        public Integer getScale() {
            return scale;
        }
        public void setScale(Integer scale) {
            this.scale = scale;
        }
    }

    public static class SavedGamesConfig {
        private String folder;
        private String format;

        public String getFolder() {
            return folder;
        }
        public void setFolder(String folder) {
            this.folder = folder;
        }
        public String getFormat() {
            return format;
        }
        public void setFormat(String format) {
            this.format = format;
        }
    }

    public static class PresetConfig {
        private List<String> expansions;
        private Map<CustomRule, Object> rules;

        public List<String> getExpansions() {
            return expansions == null ? Collections.<String>emptyList() : expansions;
        }
        public void setExpansions(List<String> expansions) {
            this.expansions = expansions;
        }
        public Map<CustomRule, Object> getRules() {
            return rules;
        }
        public void setRules(Map<CustomRule, Object> rules) {
            this.rules = rules;
        }

        public void updateGameSetup(Connection conn, String gameId) {
            EnumSet<Expansion> expansionSet = EnumSet.noneOf(Expansion.class);
            expansionSet.add(Expansion.BASIC);
            if (expansions != null) {
                for (String expName : expansions) {
                    try {
                        expansionSet.add(Expansion.valueOf(expName));
                    } catch (IllegalArgumentException ex) {
                        LoggerFactory.getLogger(Config.class).error("Invalid expansion name {} in preset config", expName);
                    }
                }
            }
            GameSetupMessage msg = new GameSetupMessage(rules, expansionSet);
            msg.setGameId(gameId);
            conn.send(msg);
        }
    }

    public static class AutostartConfig {

        private String preset;
        private List<String> players;
        private Boolean online;

        public String getPreset() {
            return preset;
        }
        public void setPreset(String profile) {
            this.preset = profile;
        }
        public List<String> getPlayers() {
            return players;
        }
        public void setPlayers(List<String> players) {
            this.players = players;
        }
        public Boolean getOnline() {
            return online;
        }
        public void setOnline(Boolean online) {
            this.online = online;
        }
    }

    public static class DebugConfig {
        private String window_size;
        private String autosave;
        private AutostartConfig autostart;
        private Map<String, String> tile_definitions;
        private HashMap<String, Object> game_annotation;
        private List<String> off_capabilities;
        private String area_highlight;

        public boolean isAutostartEnabled() {
            return autostart != null && (autostart.getPreset() != null || Boolean.TRUE.equals(autostart.getOnline()));
        }

        public String getWindow_size() {
            return window_size;
        }
        public void setWindow_size(String window_size) {
            this.window_size = window_size;
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
        public HashMap<String, Object> getGame_annotation() {
            return game_annotation;
        }
        public void setGame_annotation(HashMap<String, Object> game_annotation) {
            this.game_annotation = game_annotation;
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
        private Boolean any_deployment;
        private Boolean farm_deployment;
        private Boolean on_tower_deployment;
        private Boolean ransom_payment;

        public Boolean getAny_deployment() {
            return any_deployment == null ? Boolean.FALSE : any_deployment;
        }
        public void setAny_deployment(Boolean any_deployment) {
            this.any_deployment = any_deployment;
        }
        public Boolean getFarm_deployment() {
            return farm_deployment == null ? Boolean.FALSE : farm_deployment;
        }
        public void setFarm_deployment(Boolean farm_deployment) {
            this.farm_deployment = farm_deployment;
        }
        public Boolean getOn_tower_deployment() {
            return on_tower_deployment == null ? Boolean.FALSE : on_tower_deployment;
        }
        public void setOn_tower_deployment(Boolean on_tower_deployment) {
            this.on_tower_deployment = on_tower_deployment;
        }
        public Boolean getRansom_payment() {
            return ransom_payment == null ? Boolean.FALSE : ransom_payment;
        }
        public void setRansom_payment(Boolean ransom_payment) {
            this.ransom_payment = ransom_payment;
        }
    }

    public static class ColorConfig {
        private String meeple;
        private String fontLight;
        private String fontDark;

        public ColorConfig() {
        }

        public ColorConfig(String meeple) {
            this.meeple = meeple;
        }

        public ColorConfig(String meeple, String fontLight, String fontDark) {
            this.meeple = meeple;
            this.fontLight = fontLight;
            this.fontDark = fontDark;
        }

        public String getMeeple() {
            return meeple;
        }

        public void setMeeple(String meeple) {
            this.meeple = meeple;
        }

        public String getFontDark() {
            return fontDark;
        }

        public void setFontDark(String fontDark) {
            this.fontDark = fontDark;
        }

        public String getFontLight() {
            return fontLight;
        }

        public void setFontLight(String fontLight) {
            this.fontLight = fontLight;
        }

    }

    public static class PlayersConfig {
        private List<ColorConfig> colors;
        private List<String> names;
        private List<String> ai_names;

        public List<ColorConfig> getColors() {
            return colors == null ? Collections.<ColorConfig>emptyList() : colors;
        }
        public void setColors(List<ColorConfig> colors) {
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

    public PlayerColors getPlayerColor(PlayerSlot slot) {
        return getPlayerColor(slot.getNumber());
    }

    public PlayerColors getPlayerColor(int slotNumber) {
        try {
            ColorConfig cfg = players.getColors().get(slotNumber);
            return new PlayerColors(cfg, darkTheme);
        } catch (IndexOutOfBoundsException ex) {
            logger.warn("Too few player colors defined in config");
            return new PlayerColors();
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

    public Locale getLocaleObject() {
        String language = getLocale();
        if (language == null) {
            return Locale.getDefault();
        }
        if (language.contains("_")) {
            String[] tokens = language.split("_", 2);
            return new Locale(tokens[0], tokens[1]);
        }
        return new Locale(language);
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Boolean getBeep_alert() {
        return beep_alert == null ? Boolean.FALSE : beep_alert;
    }

    public void setBeep_alert(Boolean beep_alert) {
        this.beep_alert = beep_alert;
    }

    public List<String> getPlugins() {
        return plugins == null ? Collections.<String>emptyList() : plugins;
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

    public Map<String, PresetConfig> getPresets() {
        if (presets == null) {
            presets = new HashMap<>();
        }
        return presets;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }


    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getPlay_online_host() {
        return play_online_host;
    }

    public void setPlay_online_host(String play_online_host) {
        this.play_online_host = play_online_host;
    }

    public void setPresets(Map<String, PresetConfig> presets) {
        this.presets = presets;
    }

    public List<String> getConnection_history() {
        return connection_history;
    }

    public void setConnection_history(List<String> connection_history) {
        this.connection_history = connection_history;
    }

    public File getOrigin() {
        return origin;
    }

    public void setOrigin(File origin) {
        this.origin = origin;
    }

    public ScreenshotsConfig getScreenshots() {
        if (screenshots == null) {
            screenshots = new ScreenshotsConfig();
        }
        return screenshots;
    }

    public void setScreenshots(ScreenshotsConfig screenshots) {
        this.screenshots = screenshots;
    }


    public SavedGamesConfig getSaved_games() {
        if (saved_games == null) {
            saved_games = new SavedGamesConfig();
        }
        return saved_games;
    }

    public void setSaved_games(SavedGamesConfig saved_games) {
        this.saved_games = saved_games;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
    }


}
