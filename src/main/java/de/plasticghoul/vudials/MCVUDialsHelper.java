package de.plasticghoul.vudials;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.StringWriter;
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
    public static int currentFoodLevel = 0;

    public MCVUDialsHelper() {
    }

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

    public static int getCurrentFoodLevel() {
        return currentFoodLevel;
    }

    public static void setFoodLevel(int newFoodLevel) {
        currentFoodLevel = newFoodLevel;
    }

}
