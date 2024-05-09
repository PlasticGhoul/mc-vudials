package de.plasticghoul.vudials;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.StringWriter;
import java.util.HashMap;
import java.net.Socket;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MCVUDialsHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Writer buffer = new StringWriter();
    private static PrintWriter printwriter = new PrintWriter(buffer);
    public static boolean serverAvailable = false;
    public static String[] dialUids = null;
    
    // Dial Values
    public static int currentHealthValuePercent = 0;
    public static int currentFoodLevelValuePercent = 0;
    public static int currentArmorValuePercent = 0;
    public static int currentAirValuePercent = 0;

    // Dial Colors
    public static HashMap<String, Integer> currentHealthColors = new HashMap<String, Integer>();
    public static HashMap<String, Integer> currentFoodLevelColors = new HashMap<String, Integer>();
    public static HashMap<String, Integer> currentArmorColors = new HashMap<String, Integer>();
    public static HashMap<String, Integer> currentAirColors = new HashMap<String, Integer>();

    public MCVUDialsHelper() {
    }

    // Util
    public static boolean isVUServerListening(String host, int port) {
        LOGGER.debug("Trying " + host + ":" + port + "...");

        Socket socket = null;
        try {
            socket = new Socket(host, port);
            LOGGER.debug("Found running VU Server instance.");
            return true;
        } catch (Exception exception) {
            LOGGER.error("No running VU Server instance found!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception exception) {
                }
            }
        }
    }

    public static boolean isVUServerAnswering(String apiBaseUrl, String apiKey) {
        LOGGER.debug("Trying base URL " + MCVUDialsConfig.vuServerApiBaseUrl);

        int httpStatusCode = 0;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(apiBaseUrl + "/api/v0/dial/list?key=" + apiKey)
                    .openConnection();
            httpStatusCode = conn.getResponseCode();
        } catch (IOException exception) {
            LOGGER.error("Error creating connection to API server!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
            return false;
        }

        if (httpStatusCode == 200) {
            LOGGER.debug("API server answered successful.");
            return true;
        } else {
            return false;
        }
    }

    public static boolean isVUServerAvailable() {
        LOGGER.info("Try to reach API server...");
        if (isVUServerListening(MCVUDialsConfig.vuServerHostname, MCVUDialsConfig.vuServerPort)
                && isVUServerAnswering(MCVUDialsConfig.vuServerApiBaseUrl, MCVUDialsConfig.vuServerApiKey)) {
            LOGGER.info("API server listening and answering.");
            serverAvailable = true;
            return serverAvailable;
        } else {
            LOGGER.error("API server either not listening or answeing!");
            serverAvailable = false;
            return serverAvailable;
        }
    }

    public static void getDialUids() {
        JSONObject json = null;

        LOGGER.info("Getting dial UIDs...");

        try {
            json = new JSONObject(IOUtils.toString(new URL(
                    MCVUDialsConfig.vuServerApiBaseUrl + "/api/v0/dial/list?key=" + MCVUDialsConfig.vuServerApiKey),
                    Charset.forName("UTF-8")));
        } catch (JSONException | IOException exception) {
            LOGGER.error("Error getting dials stats!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        }

        if (json != null) {
            JSONArray jsonArray = json.getJSONArray("data");
            int dialsCount = jsonArray.length();
            dialUids = new String[dialsCount];

            for (int i = 0; i < dialsCount; i++) {
                dialUids[i] = jsonArray.getJSONObject(i).getString("uid");
                LOGGER.debug("Dial " + i + 1 + " UID: " + dialUids[i]);
            }
        }
    }

    // Dial Values
    public static int getCurrentHealthValuePercent() {
        return currentHealthValuePercent;
    }

    public static void setCurrentHealthValuePercent(float newHealth, float maxHealth) {
        currentHealthValuePercent = Math.round((newHealth * 100)/maxHealth);
    }

    public static int getCurrentFoodLevelValuePercent() {
        return currentFoodLevelValuePercent;
    }

    public static void setCurrentFoodLevelValuePercent(int newFoodLevel, int maxFoodLevel) {

        currentFoodLevelValuePercent = Math.round((newFoodLevel * 100) / maxFoodLevel);
    }

    // Dial Colors
    public static HashMap<String, Integer> getCurrentHealthColors() {
        return currentHealthColors;
    }

    public static HashMap<String, Integer> getNewHealthColors(int newHealth) {
        HashMap<String, Integer> newHealthColors = new HashMap<String, Integer>();

        if (newHealth > 50) {
            newHealthColors.put("red", 0);
            newHealthColors.put("green", 100);
            newHealthColors.put("blue", 0);
        } else if (newHealth > 20) {
            newHealthColors.put("red", 100);
            newHealthColors.put("green", 100);
            newHealthColors.put("blue", 0);
        } else if (newHealth <= 20) {
            newHealthColors.put("red", 100);
            newHealthColors.put("green", 0);
            newHealthColors.put("blue", 0);
        }

        return newHealthColors;
    }

    public static void setCurrentHealthColors() {
        if (getCurrentHealthValuePercent() > 50) {
            LOGGER.debug("Setting health dial color to green");

            currentHealthColors.put("red", 0);
            currentHealthColors.put("green", 100);
            currentHealthColors.put("blue", 0);
        } else if (getCurrentHealthValuePercent() > 20) {
            LOGGER.debug("Setting health dial color to yellow");

            currentHealthColors.put("red", 100);
            currentHealthColors.put("green", 100);
            currentHealthColors.put("blue", 0);
        } else if (getCurrentHealthValuePercent() <= 20) {
            LOGGER.debug("Setting health dial color to red");

            currentHealthColors.put("red", 100);
            currentHealthColors.put("green", 0);
            currentHealthColors.put("blue", 0);
        }        
    }

    public static HashMap<String, Integer> getCurrentFoodLevelColors() {
        return currentFoodLevelColors;
    }

    public static HashMap<String, Integer> getNewFoodLevelColors(int newFoodLevel) {
        HashMap<String, Integer> newHFoodLevelColors = new HashMap<String, Integer>();

        if (newFoodLevel > 50) {
            newHFoodLevelColors.put("red", 0);
            newHFoodLevelColors.put("green", 100);
            newHFoodLevelColors.put("blue", 0);
        } else if (newFoodLevel > 30) {
            newHFoodLevelColors.put("red", 100);
            newHFoodLevelColors.put("green", 100);
            newHFoodLevelColors.put("blue", 0);
        } else if (newFoodLevel <= 30) {
            newHFoodLevelColors.put("red", 100);
            newHFoodLevelColors.put("green", 0);
            newHFoodLevelColors.put("blue", 0);
        }

        return newHFoodLevelColors;
    }

    public static void setCurrentFoodLevelColors() {
        if (getCurrentFoodLevelValuePercent() > 50) {
            LOGGER.debug("Setting health dial color to green");

            currentFoodLevelColors.put("red", 0);
            currentFoodLevelColors.put("green", 100);
            currentFoodLevelColors.put("blue", 0);
        } else if (getCurrentFoodLevelValuePercent() > 30) {
            LOGGER.debug("Setting health dial color to yellow");

            currentFoodLevelColors.put("red", 100);
            currentFoodLevelColors.put("green", 100);
            currentFoodLevelColors.put("blue", 0);
        } else if (getCurrentFoodLevelValuePercent() <= 30) {
            LOGGER.debug("Setting health dial color to red");

            currentFoodLevelColors.put("red", 100);
            currentFoodLevelColors.put("green", 0);
            currentFoodLevelColors.put("blue", 0);
        }        
    }

}
