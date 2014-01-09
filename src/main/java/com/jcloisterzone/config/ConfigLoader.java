package com.jcloisterzone.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
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
import com.jcloisterzone.config.Config.ColorConfig;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.Config.PlayersConfig;
import com.jcloisterzone.config.Config.PresetConfig;
import com.jcloisterzone.ui.Client;


public class ConfigLoader {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_CONFIG = "config.yaml";

    private final Yaml yaml;
    private final Pattern indentPatter = Pattern.compile("^", Pattern.MULTILINE);

    public ConfigLoader() {
        yaml = new Yaml(new Constructor(Config.class));
    }

    private String getConfigFile() {
        String configFile = System.getProperty("config");
        if (configFile == null) {
            return DEFAULT_CONFIG;
        }
        return configFile;
    }

    public Config load() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        String configFile = getConfigFile();
        URL configResource = Client.class.getClassLoader().getResource(configFile);
        if (configResource == null && !configFile.equals(DEFAULT_CONFIG)) {
            logger.warn("Configuration file not found {}", configFile);
            configFile = DEFAULT_CONFIG;
            configResource = Client.class.getClassLoader().getResource(configFile);
        }

        Config config = null;
        boolean save = false;
        if (configResource == null) {
            logger.info("Default configuration file {} doesn't exist. Creating new one.", DEFAULT_CONFIG);
            config = createDefault();
            config.setOrigin(new File(DEFAULT_CONFIG));
            save = true;
        } else {
            logger.info("Loading configuration {}", configFile);
            File origin = new File(configResource.getFile());
            try {
                config = (Config) yaml.load(configResource.openStream());
            } catch (Exception ex) {
                logger.warn("Error reading configuration.", ex);
                if (ex instanceof ParserException && origin.isFile()) {
                    String name;
                    if (origin.getParent() == null) {
                        name = "~" + origin.getName();
                    } else {
                        name = origin.getParent() + File.separator + "~" + origin.getName();
                    }
                    File backup = new File(name);
                    if (!backup.isFile()) {
                        try {
                            Files.copy(origin.toPath(), backup.toPath());
                        } catch (IOException copyEx) {
                            logger.warn("Unable to backup invalid config.", copyEx);
                        }
                    }
                    save = true;
                }
                config = createDefault();
            }
            config.setOrigin(origin);
        }
        if (save) {
            save(config);
        }
        return config;
    }

    public void save(Config config) {
        File file = config.getOrigin();
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print(fillTemplate(config));
            writer.close();
            logger.info("Configuration saved {}", file);
        } catch (IOException e) {
            logger.warn("Unable to create configuration file {}", file);
        }
    }

    private Config createDefault() {
        Config config = new Config();
        config.setUpdate("http://jcloisterzone.com/version.xml");
        config.setPort(37447);
        config.setScore_display_duration(7);
        config.setAi_place_tile_delay(250);
        config.getConfirm().setTower_place(true);
        config.getConfirm().setRansom_payment(true);
        config.getConfirm().setGame_close(true);
        config.getPlayers().setColors(Lists.newArrayList(
            new ColorConfig("RED"),
            new ColorConfig("#008ffe"),
            new ColorConfig("#FFED00"),
            new ColorConfig("#009900"),
            new ColorConfig("BLACK"),
            new ColorConfig("#812EFF")
        ));
        config.getPlayers().setAi_names(Lists.newArrayList("Adda", "Ellen", "Caitlyn", "Riannon", "Tankred", "Rigatona"));
        config.setPlugins(Lists.newArrayList("plugins/classic.jar"));
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
        model.put("ai_place_tile_delay", config.getAi_place_tile_delay());
        model.put("beep_alert", config.getBeep_alert());

        if (config.getConfirm() != null) {
            model.put("confirm", indent(1, yaml.dumpAs(config.getConfirm(), Tag.MAP, FlowStyle.BLOCK)));
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

        if (config.getPlugins() != null && !config.getPlugins().isEmpty()) {
            model.put("plugins", indent(1, yaml.dumpAs(config.getPlugins(), Tag.SEQ, FlowStyle.BLOCK)));
        }
        if (config.getPresets() != null && !config.getPresets().isEmpty()) {
            model.put("presets", indent(1, yaml.dumpAs(config.getPresets(), Tag.MAP, FlowStyle.BLOCK)));
        }
        if (config.getConnection_history() != null && !config.getConnection_history().isEmpty()) {
            model.put("connection_history", yaml.dumpAs(config.getConnection_history(), Tag.SEQ, FlowStyle.FLOW).trim());
        }

        DebugConfig dc = config.getDebug();
        model.put("hasDebug", dc != null);
        if (dc != null) {
            model.put("save_format", dc.getSave_format());
            model.put("autosave", dc.getAutosave());
            if (dc.getAutostart() != null) {
                model.put("autostart", indent(2, yaml.dumpAs(dc.getAutostart(), Tag.MAP, FlowStyle.BLOCK)));
            }
            if (dc.getTile_definitions() != null) {
                model.put("tile_definitions", indent(2, yaml.dumpAs(dc.getTile_definitions(), Tag.MAP, FlowStyle.BLOCK)));
            }
            if (dc.getDraw() != null) {
                model.put("draw", indent(1, yaml.dumpAs(dc.getDraw(), Tag.SEQ, FlowStyle.BLOCK)));
            }
            if (dc.getOff_capabilities() != null) {
                model.put("off_capabilities", indent(2, yaml.dumpAs(dc.getOff_capabilities(), Tag.SEQ, FlowStyle.BLOCK)));
            }
            model.put("area_highlight", dc.getArea_highlight());
        }

        String result = engine.transform(template, model);
        result = result.replace(" !!"+PresetConfig.class.getName(), "");
        result = result.replace("\n", System.lineSeparator());
        return result;
    }

}
