package com.jcloisterzone.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.parser.ParserException;

import com.jcloisterzone.ui.Client;


public class PreparedGameConfigLoader {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_PREPARED_CONFIG = "prepared.yaml";

    private final Yaml yaml;

    public PreparedGameConfigLoader() {
        yaml = new Yaml(new Constructor(PreparedGameConfig.class));
    }

    private String getConfigFile() {
        String configFile = System.getProperty("preparedConfig");
        if (configFile == null) {
            return DEFAULT_PREPARED_CONFIG;
        }
        return configFile;
    }

    public PreparedGameConfig load() {
        Yaml yaml = new Yaml(new Constructor(PreparedGameConfig.class));
        String configFile = getConfigFile();
        URL configResource = Client.class.getClassLoader().getResource(configFile);
        if (configResource == null && !configFile.equals(DEFAULT_PREPARED_CONFIG)) {
            logger.warn("Configuration file not found {}", configFile);
            configFile = DEFAULT_PREPARED_CONFIG;
            configResource = Client.class.getClassLoader().getResource(configFile);
        }

        PreparedGameConfig config = null;
        if (configResource == null && (new File(configFile).exists())){
        	try{
        		configResource = (new File(configFile).toURI().toURL());
        	}catch (Exception e){
        		
        	}
        }
        if (configResource == null) {
            logger.info("Default configuration file {} doesn't exist. Creating new one.", DEFAULT_PREPARED_CONFIG);
            config = createDefault();
        } else {
            logger.info("Loading configuration {}", configFile);
            File origin = new File(configResource.getFile());
            try {
                config = (PreparedGameConfig) yaml.load(configResource.openStream());
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
                }
                config = createDefault();
            }
        }
        return config;
    }

    private PreparedGameConfig createDefault() {
        PreparedGameConfig config = new PreparedGameConfig();
        return config;
    }
}
