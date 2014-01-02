package com.jcloisterzone.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import com.floreysoft.jmte.Engine;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.Config.PlayersConfig;
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
        if (configResource == null) {
            logger.info("Default configuration file {} doesn't exist. Creating new one.", DEFAULT_CONFIG);
            config = createDefault();
            config.setOrigin(new File(DEFAULT_CONFIG));
            save(config);
        } else {
            logger.info("Loading configuration {}", configFile);
            try {
                config = (Config) yaml.load(configResource.openStream());
            } catch (IOException ex) {
                logger.warn("Error reading configuration.", ex);
                config = createDefault();
            }
            config.setOrigin(new File(configResource.getFile()));
        }
        return config;
    }

    public void save(Config config) {
        File file = config.getOrigin();
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print(fillTemplate(config));
            writer.close();
            logger.warn("Configuration saved {}", file);
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
        config.getPlayers().setColors(Lists.newArrayList("RED", "#008ffe", "YELLOW", "#009900", "BLACK", "#808000"));
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
                model.put("colors", yaml.dumpAs(pc.getColors(), Tag.SEQ, FlowStyle.FLOW).trim());
            }
            if (pc.getNames() != null && !pc.getNames().isEmpty()) {
                model.put("player_names", yaml.dumpAs(pc.getNames(), Tag.SEQ, FlowStyle.FLOW).trim());
            }
            if (pc.getAi_names() != null && !pc.getAi_names().isEmpty()) {
                model.put("ai_names", yaml.dumpAs(pc.getAi_names(), Tag.SEQ, FlowStyle.FLOW).trim());
            }
        }

        if (config.getPlugins() != null) {
            model.put("plugins", indent(1, yaml.dumpAs(config.getPlugins(), Tag.SEQ, FlowStyle.BLOCK)));
        }
        if (config.getProfiles() != null) {
            model.put("profiles", indent(1, yaml.dumpAs(config.getProfiles(), Tag.MAP, FlowStyle.BLOCK)));
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
        result = result.replace(" !!com.jcloisterzone.config.Config$ProfileConfig", "");
        return result;
    }

}
