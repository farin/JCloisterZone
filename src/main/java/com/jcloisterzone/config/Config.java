package com.jcloisterzone.config;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.ui.PlayerColor;

/**
 * Snakeyaml not supporting mapping to camel-case properties.
 * So propeties must be underscored.
 *
 */
public class Config {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

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
    private Map<String, PresetConfig> presets;
    private List<String> connection_history;

    public static class PresetConfig {
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

        public void updateGameSetup(ServerIF server) {
            EnumSet<Expansion> expansionSet = EnumSet.noneOf(Expansion.class);
            expansionSet.add(Expansion.BASIC);
            for (String expName : expansions) {
                expansionSet.add(Expansion.valueOf(expName));
            }

            EnumSet<CustomRule> ruleSet = EnumSet.noneOf(CustomRule.class);
            for (String ruleName : rules) {
                ruleSet.add(CustomRule.valueOf(ruleName));
            }
            server.updateGameSetup(expansionSet.toArray(new Expansion[expansionSet.size()]), ruleSet.toArray(new CustomRule[ruleSet.size()]));
        }
    }

    public static class AutostartConfig {

        private String preset;
        private List<String> players;

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
            return autostart != null && autostart.getPreset() != null;
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

    public static class ColorConfig {
        private String meeple;
        private String font;

        public ColorConfig() {
        }

        public ColorConfig(String meeple) {
            this.meeple = meeple;
        }

        public ColorConfig(String meeple, String font) {
            this.meeple = meeple;
            this.font = font;
        }
        public String getMeeple() {
            return meeple;
        }
        public void setMeeple(String meeple) {
            this.meeple = meeple;
        }
        public String getFont() {
            return font;
        }
        public void setFont(String font) {
            this.font = font;
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

    public PlayerColor getPlayerColor(PlayerSlot slot) {
        try {
            ColorConfig cfg = players.getColors().get(slot.getNumber());
            return new PlayerColor(cfg);
        } catch (IndexOutOfBoundsException ex) {
            logger.warn("Too few player colors defined in config");
            return new PlayerColor();
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

    public Map<String, PresetConfig> getPresets() {
        if (presets == null) {
            presets = new HashMap<>();
        }
        return presets;
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



}
