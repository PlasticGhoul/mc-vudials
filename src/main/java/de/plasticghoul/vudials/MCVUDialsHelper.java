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

/**
 * Main class that manages all variables (get and set)
 */
public class MCVUDialsHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Writer buffer = new StringWriter();
    private static PrintWriter printwriter = new PrintWriter(buffer);
    
    /** Saves the current VU Server status */
    public static boolean serverAvailable = false;

    /** Has all currently available dial UIDs saved */
    public static String[] dialUids = null;

    // Dial Values
    /** Current health value in percent as an integer (0-100) */
    public static int currentHealthValuePercent = 0;

    /** Current food level value in percent as an integer (0-100) */
    public static int currentFoodLevelValuePercent = 0;

    /** Current armor in percent as an integer (0-100) */
    public static int currentArmorValuePercent = 0;

    /** Current air value in percent as an integer (0-100) */
    public static int currentAirValuePercent = 0;

    // Dial Colors
    /** Current health LED color values as HashMap */
    public static HashMap<String, Integer> currentHealthColors = new HashMap<String, Integer>();
    
    /** Current food level LED color values as HashMap */
    public static HashMap<String, Integer> currentFoodLevelColors = new HashMap<String, Integer>();

    /** Current armor LED color values as HashMap */
    public static HashMap<String, Integer> currentArmorColors = new HashMap<String, Integer>();

    /** Current air LED color values as HashMap */
    public static HashMap<String, Integer> currentAirColors = new HashMap<String, Integer>();

    /**
     * Class constructor.
     */
    public MCVUDialsHelper() {
    }

    // Util
    /**
     * This method tests if the VU Server is listening on the configured hostname and port.
     * 
     * @param   host    The hostname of the VU Server
     * @param   port    The port on which the VU Server should listen
     * @return          True if the server listens on the given hostname and port
     */
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

    /**
     * This method tests if the VU Server is answering to HTTP requests.
     * 
     * @param   apiBaseUrl  The base URL of the api server
     * @param   apiKey      The API key which is needed in order to make requests against the API
     * @return              True if the server answers on the given base url
     */
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

    /**
     * This method tests if the VU Server is listening and answering by calling isVUServerListening() and isVUServerAnswering().
     *
     * @return              True if the server listens and answers
     */
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

    /**
     * This method is getting a list of dial UIDs so it can be used later on.
     */
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
    /**
     * This method gets the currently set health value.
     * 
     * @return  The current health value as an integer
     */
    public static int getCurrentHealthValuePercent() {
        return currentHealthValuePercent;
    }

    /**
     * This method calculates the new health value and saves it in a variable.
     * 
     * @param   newHealth   The new health value to be set (before percentage calculation)
     * @param   maxHealth   The maximum possible health value
     */
    public static void setCurrentHealthValuePercent(float newHealth, float maxHealth) {
        currentHealthValuePercent = Math.round((newHealth * 100) / maxHealth);
    }

    /**
     * This method gets the currently set food level value.
     * 
     * @return  The current food level value as an integer
     */
    public static int getCurrentFoodLevelValuePercent() {
        return currentFoodLevelValuePercent;
    }

    /**
     * This method calculates the new food level value and saves it in a variable.
     * 
     * @param   newFoodLevel    The new food level value to be set (before percentage calculation)
     * @param   maxFoodLevel    The maximum possible food level value
     */
    public static void setCurrentFoodLevelValuePercent(int newFoodLevel, int maxFoodLevel) {

        currentFoodLevelValuePercent = Math.round((newFoodLevel * 100) / maxFoodLevel);
    }

    /**
     * This method gets the currently set armor value.
     * 
     * @return  The current armor value as an integer
     */
    public static int getCurrentArmorValuePercent() {
        return currentArmorValuePercent;
    }

    /**
     * This method calculates the new armor value and saves it in a variable.
     * 
     * @param   newArmor    The new armor value to be set (before percentage calculation)
     * @param   maxArmor    The maximum possible armor value
     */
    public static void setCurrentArmorValuePercent(int newArmor, int maxArmor) {

        currentArmorValuePercent = Math.round((newArmor * 100) / maxArmor);
    }

    /**
     * This method gets the currently set air value.
     * 
     * @return  The current air value as an integer
     */
    public static int getCurrentAirValuePercent() {
        return currentAirValuePercent;
    }

    /**
     * This method calculates the new air value and saves it in a variable.
     * 
     * @param   newAir    The new air value to be set (before percentage calculation)
     * @param   maxAir    The maximum possible air value
     */
    public static void setCurrentAirValuePercent(int newAir, int maxAir) {
        if (newAir < 0) {
            newAir = 0;
        }
        currentAirValuePercent = Math.round((newAir * 100) / maxAir);
    }

    // Dial Colors
    /**
     * This method gets the currently set health colors.
     * 
     * @return  The current health colors as a HashMap
     */
    public static HashMap<String, Integer> getCurrentHealthColors() {
        return currentHealthColors;
    }

    /**
     * This method calculates the new health color based on the given health value and saves it in a HashMap.
     * This is needed to later compare the new and the current colors in order to reduce API calls.
     * 
     * @param   newHealth   The new health value to be set (before percentage calculation)
     * @return              Returns a HashMap with possible new color values for comparison with the current set color values
     */
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

    /**
     * This method calculates the current health color based on the given health value and saves it in a HashMap.
     */
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

    /**
     * This method gets the currently set food level colors.
     * 
     * @return  The current food level colors as a HashMap
     */
    public static HashMap<String, Integer> getCurrentFoodLevelColors() {
        return currentFoodLevelColors;
    }

    /**
     * This method calculates the new food level color based on the given food level value and saves it in a HashMap.
     * This is needed to later compare the new and the current colors in order to reduce API calls.
     * 
     * @param   newFoodLevel    The new food level value to be set (before percentage calculation)
     * @return                  Returns a HashMap with possible new color values for comparison with the current set color values
     */
    public static HashMap<String, Integer> getNewFoodLevelColors(int newFoodLevel) {
        HashMap<String, Integer> newFoodLevelColors = new HashMap<String, Integer>();

        if (newFoodLevel > 50) {
            newFoodLevelColors.put("red", 0);
            newFoodLevelColors.put("green", 100);
            newFoodLevelColors.put("blue", 0);
        } else if (newFoodLevel > 30) {
            newFoodLevelColors.put("red", 100);
            newFoodLevelColors.put("green", 100);
            newFoodLevelColors.put("blue", 0);
        } else if (newFoodLevel <= 30) {
            newFoodLevelColors.put("red", 100);
            newFoodLevelColors.put("green", 0);
            newFoodLevelColors.put("blue", 0);
        }

        return newFoodLevelColors;
    }

    /**
     * This method calculates the current food level color based on the currently set food level value and saves it in a HashMap.
     */
    public static void setCurrentFoodLevelColors() {
        if (getCurrentFoodLevelValuePercent() > 50) {
            LOGGER.debug("Setting food dial color to green");

            currentFoodLevelColors.put("red", 0);
            currentFoodLevelColors.put("green", 100);
            currentFoodLevelColors.put("blue", 0);
        } else if (getCurrentFoodLevelValuePercent() > 30) {
            LOGGER.debug("Setting food dial color to yellow");

            currentFoodLevelColors.put("red", 100);
            currentFoodLevelColors.put("green", 100);
            currentFoodLevelColors.put("blue", 0);
        } else if (getCurrentFoodLevelValuePercent() <= 30) {
            LOGGER.debug("Setting food dial color to red");

            currentFoodLevelColors.put("red", 100);
            currentFoodLevelColors.put("green", 0);
            currentFoodLevelColors.put("blue", 0);
        }
    }

    /**
     * This method gets the currently set armor colors.
     * 
     * @return  The current armor colors as a HashMap
     */
    public static HashMap<String, Integer> getCurrentArmorColors() {
        return currentArmorColors;
    }

    /**
     * This method calculates the new armor color based on the given armor value and saves it in a HashMap.
     * This is needed to later compare the new and the current colors in order to reduce API calls.
     * 
     * @param   newArmor    The new armor value to be set (before percentage calculation)
     * @return              Returns a HashMap with possible new color values for comparison with the current set color values
     */
    public static HashMap<String, Integer> getNewArmorColors(int newArmor) {
        HashMap<String, Integer> newArmorColors = new HashMap<String, Integer>();

        if (newArmor > 50) {
            newArmorColors.put("red", 0);
            newArmorColors.put("green", 100);
            newArmorColors.put("blue", 0);
        } else if (newArmor > 30) {
            newArmorColors.put("red", 100);
            newArmorColors.put("green", 100);
            newArmorColors.put("blue", 0);
        } else if (newArmor <= 30) {
            newArmorColors.put("red", 100);
            newArmorColors.put("green", 0);
            newArmorColors.put("blue", 0);
        }

        return newArmorColors;
    }

    /**
     * This method calculates the current armor color based on the currently set armor value and saves it in a HashMap.
     */
    public static void setCurrentArmorColors() {
        if (getCurrentArmorValuePercent() > 50) {
            LOGGER.debug("Setting armor dial color to green");

            currentArmorColors.put("red", 0);
            currentArmorColors.put("green", 100);
            currentArmorColors.put("blue", 0);
        } else if (getCurrentArmorValuePercent() > 30) {
            LOGGER.debug("Setting armor dial color to yellow");

            currentArmorColors.put("red", 100);
            currentArmorColors.put("green", 100);
            currentArmorColors.put("blue", 0);
        } else if (getCurrentArmorValuePercent() <= 30) {
            LOGGER.debug("Setting armor dial color to red");

            currentArmorColors.put("red", 100);
            currentArmorColors.put("green", 0);
            currentArmorColors.put("blue", 0);
        }
    }

    /**
     * This method gets the currently set air colors.
     * 
     * @return  The current air colors as a HashMap
     */
    public static HashMap<String, Integer> getCurrentAirColors() {
        return currentAirColors;
    }

    /**
     * This method calculates the new air color based on the given air value and saves it in a HashMap.
     * This is needed to later compare the new and the current colors in order to reduce API calls.
     * 
     * @param   newAir  The new air value to be set (before percentage calculation)
     * @return          Returns a HashMap with possible new color values for comparison with the current set color values
     */
    public static HashMap<String, Integer> getNewAirColors(int newAir) {
        HashMap<String, Integer> newAirColors = new HashMap<String, Integer>();

        if (newAir > 50) {
            newAirColors.put("red", 0);
            newAirColors.put("green", 100);
            newAirColors.put("blue", 0);
        } else if (newAir > 30) {
            newAirColors.put("red", 100);
            newAirColors.put("green", 100);
            newAirColors.put("blue", 0);
        } else if (newAir <= 30) {
            newAirColors.put("red", 100);
            newAirColors.put("green", 0);
            newAirColors.put("blue", 0);
        }

        return newAirColors;
    }

    /**
     * This method calculates the current air color based on the currently set air value and saves it in a HashMap.
     */
    public static void setCurrentAirColors() {
        if (getCurrentAirValuePercent() > 50) {
            LOGGER.debug("Setting air dial color to green");

            currentAirColors.put("red", 0);
            currentAirColors.put("green", 100);
            currentAirColors.put("blue", 0);
        } else if (getCurrentAirValuePercent() > 30) {
            LOGGER.debug("Setting air dial color to yellow");

            currentAirColors.put("red", 100);
            currentAirColors.put("green", 100);
            currentAirColors.put("blue", 0);
        } else if (getCurrentAirValuePercent() <= 30) {
            LOGGER.debug("Setting air dial color to red");

            currentAirColors.put("red", 100);
            currentAirColors.put("green", 0);
            currentAirColors.put("blue", 0);
        }
    }

}
