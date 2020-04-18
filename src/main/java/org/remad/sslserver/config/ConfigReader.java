package org.remad.sslserver.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

/**
 * This class handles read a json file, deserialize JSON to {@link Config} and create the Server Socket Settings.
 */
public class ConfigReader {

    private static String FILE_NAME = "config.json";
    private String configJson;

    /**
     * @return Returns the server config.
     * @throws IOException In case of cannot process config file or cannot parse json.
     */
    public Config getConfig() throws IOException, RuntimeException {
        readConfigJson();

        return convertJSONToConfig();
    }

    private void readConfigJson() throws IOException {
        configJson = IOUtils.toString(new FileInputStream(FILE_NAME), StandardCharsets.UTF_8);
    }

    private Config convertJSONToConfig() throws IllegalStateException, JsonProcessingException {
        if (configJson == null || configJson.isEmpty()) {
            throw new IllegalStateException("Error: config.json is empty.");
        }

        return new ObjectMapper().readValue(configJson, Config.class);
    }

    /**
     * Gets the server socket settings.
     * @param config The config of this server.
     * @return The server socket settings as {@code Map<String, String>}
     */
    public static Map<String, String> getServerSocketSettings(Config config) {
        Map<String, String> serverSocketSettings = new HashMap<>();
        serverSocketSettings.put("ip", config.getServerSocketIP());
        serverSocketSettings.put("backlog", String.valueOf(config.getBacklog()));
        serverSocketSettings.put("port", String.valueOf(config.getListenerPort()));

        return serverSocketSettings;
    }
}
