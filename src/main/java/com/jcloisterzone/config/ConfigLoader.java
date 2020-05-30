package com.jcloisterzone.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.parser.ParserException;

import com.floreysoft.jmte.Engine;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.jcloisterzone.KeyUtils;
import com.jcloisterzone.ai.player.LegacyAiPlayer;
import com.jcloisterzone.config.Config.ColorConfig;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.Config.PlayersConfig;
import com.jcloisterzone.config.Config.PresetConfig;
import com.jcloisterzone.ui.Client;


public class ConfigLoader {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_UPDATE = "http://jcloisterzone.com/version.xml";
    public static final int DEFAULT_PORT = 37447;
    public static final int DEFAULT_SCORE_DISPLAY_DURATION = 9;
    public static final int DEFAULT_AI_PLACE_TILE_DELAY = 250;
    public static final String DEFAULT_AI_CLASS_NAME = LegacyAiPlayer.class.getName();
    public static final String DEFAULT_THEME = "light";
    public static final int DEFAULT_SCREENSHOT_SCALE = 120;
    public static final String DEFAULT_PLAY_ONLINE_HOST = "play.jcloisterzone.com";
    public static final String DEFAULT_SAVED_GAMES_FORMAT = "compact";

    private final Path dataDirectory;
    private final Yaml yaml;
    private final Pattern indentPatter = Pattern.compile("^", Pattern.MULTILINE);

    public ConfigLoader(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        yaml = new Yaml(new Constructor(Config.class));
    }

    private File getConfigFile() {
        String configFile = System.getProperty("config");
        if (configFile != null) {
            File file = Paths.get(configFile).toFile();
            if (!file.exists()) { //for dev purposes try to load also from classpath
                URL resource = Client.class.getClassLoader().getResource(configFile);
                if (resource != null) {
                    file = new File(resource.getFile());
                }
            }
            if (file.exists()) {
                return file;
            } else {
                logger.warn("Custom configuration file not found {}. Using default.", file.toString());
            }
        }
        return dataDirectory.resolve("config.yaml").toFile();
    }

    public Config load() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        File configFile = getConfigFile();

        Config config = null;
        boolean save = false;
        if (!configFile.exists()) {
            logger.info("Default configuration file {} doesn't exist. Creating new one.", configFile);
            config = createDefault();
            config.setOrigin(configFile);
            save = true;
        } else {
            logger.info("Loading configuration {}", configFile);
            try {
                config = (Config) yaml.load(new FileInputStream(configFile));
            } catch (Exception ex) {
                logger.warn("Error reading configuration.", ex);
                if (ex instanceof ParserException) {
                    String name;
                    if (configFile.getParent() == null) {
                        name = "~" + configFile.getName();
                    } else {
                        name = configFile.getParent() + File.separator + "~" + configFile.getName();
                    }
                    File backup = new File(name);
                    if (!backup.isFile()) {
                        try {
                            Files.copy(configFile.toPath(), backup.toPath());
                        } catch (IOException copyEx) {
                            logger.warn("Unable to backup invalid config.", copyEx);
                        }
                    }
                    save = true;
                }
                config = createDefault();
            }
            config.setOrigin(configFile);
        }
        if (config.getClient_id() == null) {
            config.setClient_id(KeyUtils.createRandomId());
            save = true;
        }
        if (config.getSecret() == null) {
            config.setSecret(KeyUtils.createRandomId());
            save = true;
        }
        if (save) {
            save(config);
        }
        return config;
    }

    public void save(Config config) {
        File file = config.getOrigin();
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(fillTemplate(config));
            logger.info("Configuration saved {}", file);
        } catch (IOException e) {
            logger.warn("Unable to create configuration file {}", file);
        }
    }

    public Config createDefault() {
        Config config = new Config();
        config.setUpdate(DEFAULT_UPDATE);
        config.setPort(DEFAULT_PORT);
        config.setScore_display_duration(DEFAULT_SCORE_DISPLAY_DURATION);
        config.getAi().setPlace_tile_delay(DEFAULT_AI_PLACE_TILE_DELAY);
        config.getAi().setClass_name(DEFAULT_AI_CLASS_NAME);
        config.setTheme(DEFAULT_THEME);
        config.setClient_name("");
        config.setTile_rotation(Config.TileRotationControls.TAB_RCLICK);
        config.setPlay_online_host(DEFAULT_PLAY_ONLINE_HOST);
        config.setClient_id(KeyUtils.createRandomId());
        config.setSecret(KeyUtils.createRandomId());
        config.getConfirm().setFarm_deployment(true);
        config.getConfirm().setOn_tower_deployment(true);
        config.getConfirm().setRansom_payment(true);
        config.getPlayers().setColors(Lists.newArrayList(
            new ColorConfig("RED"),
            new ColorConfig("#008ffe", null, "#5bb7fe"),
            new ColorConfig("#FFED00"),
            new ColorConfig("#009900", null, "#37a800"),
            new ColorConfig("BLACK", null, "WHITE"),
            new ColorConfig("#812EFF", null, "#ba92f8")
        ));
        config.getPlayers().setAi_names(Lists.newArrayList("Adda", "Ellen", "Caitlyn", "Riannon", "Tankred", "Rigatona"));
        config.getPlugins().setLookup_folders(Lists.newArrayList("plugins"));
        config.getPlugins().setEnabled_plugins(Lists.newArrayList("classic.jar"));
        config.getScreenshots().setScale(DEFAULT_SCREENSHOT_SCALE);
        config.getSaved_games().setFormat(DEFAULT_SAVED_GAMES_FORMAT);
        return config;
    }

    private String indent(int level, String fragment) {
        fragment = fragment.trim();
        if (fragment.length() == 0) return "\n";
        String spaces = String.format("%"+(level*2)+"s", "");
        return ("\n" + indentPatter.matcher(fragment).replaceAll(spaces));
    }

    public String fillTemplate(Config config) throws IOException {
        String template = Resources.toString(Client.class.getClassLoader().getResource("config.tpl"), Charsets.UTF_8);
        Engine engine = new Engine();
        Map<String, Object> model = new HashMap<>();
        model.put("update", config.getUpdate());
        model.put("port", config.getPort());
        model.put("locale", config.getLocale());
        model.put("score_display_duration", config.getScore_display_duration());
        model.put("theme", config.getTheme());
        model.put("beep_alert", config.getBeep_alert());
        model.put("tile_rotation", config.getTile_rotation());
        model.put("client_name", config.getClient_name());
        model.put("play_online_host", config.getPlay_online_host());
        model.put("client_id", config.getClient_id());
        model.put("secret", config.getSecret());
        model.put("screenshot_folder", config.getScreenshots().getFolder());
        model.put("screenshot_scale", config.getScreenshots().getScale());
        model.put("saved_games_folder", config.getSaved_games().getFolder());
        model.put("saved_games_format", config.getSaved_games().getFormat());

        if (config.getConfirm() != null) {
            model.put("confirm", indent(1, yaml.dumpAs(config.getConfirm(), Tag.MAP, FlowStyle.BLOCK)));
        }

        if (config.getAi() != null) {
            model.put("ai_place_tile_delay", config.getAi().getPlace_tile_delay());
            model.put("ai_class_name", config.getAi().getClass_name());
        }

        PlayersConfig pc = config.getPlayers();
        if (pc != null) {
            if (pc.getColors() != null && !pc.getColors().isEmpty()) {
                StringBuilder colors = new StringBuilder();
                for (ColorConfig cfg : pc.getColors()) {
                    colors.append("\n  - ");
                    colors.append(yaml.dumpAs(cfg, Tag.MAP, FlowStyle.FLOW).trim());
                }
                model.put("colors", colors.toString());
            }
            if (pc.getNames() != null && !pc.getNames().isEmpty()) {
                model.put("player_names", yaml.dumpAs(pc.getNames(), Tag.SEQ, FlowStyle.FLOW).trim());
            }
            if (pc.getAi_names() != null && !pc.getAi_names().isEmpty()) {
                model.put("ai_names", yaml.dumpAs(pc.getAi_names(), Tag.SEQ, FlowStyle.FLOW).trim());
            }
        }

        model.put("plugins_lookup_folders", indent(2, yaml.dumpAs(config.getPlugins().getLookup_folders(), Tag.SEQ, FlowStyle.BLOCK)));
        model.put("plugins_enabled_plugins", indent(2, yaml.dumpAs(config.getPlugins().getEnabled_plugins(), Tag.SEQ, FlowStyle.BLOCK)));

        if (config.getPresets() != null && !config.getPresets().isEmpty()) {
            model.put("presets", indent(1, yaml.dumpAs(config.getPresets(), Tag.MAP, FlowStyle.BLOCK)));
        }
        if (config.getConnection_history() != null && !config.getConnection_history().isEmpty()) {
            model.put("connection_history", yaml.dumpAs(config.getConnection_history(), Tag.SEQ, FlowStyle.FLOW).trim());
        }

        DebugConfig dc = config.getDebug();
        model.put("hasDebug", dc != null);
        if (dc != null) {
            model.put("window_size", dc.getWindow_size());
            model.put("autosave", dc.getAutosave());
            if (dc.getAutostart() != null) {
                model.put("autostart", indent(2, yaml.dumpAs(dc.getAutostart(), Tag.MAP, FlowStyle.BLOCK)));
            }
            if (dc.getTile_definitions() != null && !dc.getTile_definitions().isEmpty()) {
                model.put("tile_definitions", indent(2, yaml.dumpAs(dc.getTile_definitions(), Tag.MAP, FlowStyle.BLOCK)));
            }
            if (dc.getGame_annotation() != null && !dc.getGame_annotation().isEmpty()) {
                model.put("game_annotation", indent(2, yaml.dumpAs(dc.getGame_annotation(), Tag.MAP, FlowStyle.BLOCK)));
            }
            model.put("area_highlight", dc.getArea_highlight());
        }

        String result = engine.transform(template, model);
        result = result.replace(" !!"+PresetConfig.class.getName(), "");
        result = result.replace("\n", System.lineSeparator());
        return result;
    }

}
