package de.plasticghoul.vudials;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Main class that makes all API calls
 */
public class MCVUDialsControl {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Writer buffer = new StringWriter();
    private static PrintWriter printwriter = new PrintWriter(buffer);

    /**
     * Class constructor.
     */
    public MCVUDialsControl() {
    }

    /**
     * Sets the value on the given dial.
     * The value has to be an integer between 0 and 100.
     * 
     * @param   dialUid     The UID of the dial that will get the new value
     * @param   dialValue   The new value that will be set on the dial
     */
    public static void setDialValue(String dialUid, int dialValue) {
        HttpURLConnection connection = null;

        try {

            String apiUrl = MCVUDialsConfig.vuServerApiBaseUrl + "/api/v0/dial/" + dialUid + "/set?key="
                    + MCVUDialsConfig.vuServerApiKey + "&value=" + dialValue;

            URL url = new URL(apiUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                LOGGER.debug("Response: " + response.toString());
            } else {
                LOGGER.error("HTTP GET to API server didn't work: " + responseCode);
            }
        } catch (IOException exception) {
            LOGGER.error("Error setting value on Dial!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Sets the color on the given dial.
     * Since the dials can only increase or decrese the LED of either red green or blue,
     * the color has to be specified with a percantage value between 0-100
     * 
     * @param   dialUid     The UID of the dial that will get the new value
     * @param   red         The brightness of the red LED
     * @param   green       The brightness of the green LED
     * @param   blue        The brightness of the blue LED
     */
    public static void setDialColor(String dialUid, int red, int green, int blue) {
        HttpURLConnection connection = null;

        try {

            String apiUrl = MCVUDialsConfig.vuServerApiBaseUrl + "/api/v0/dial/" + dialUid + "/backlight?key="
                    + MCVUDialsConfig.vuServerApiKey + "&red=" + red + "&blue=" + blue + "&green=" + green;

            URL url = new URL(apiUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                LOGGER.debug("Response: " + response.toString());
            } else {
                LOGGER.error("HTTP GET to API server didn't work: " + responseCode);
            }
        } catch (IOException exception) {
            LOGGER.error("Error setting color on Dial!");
            exception.printStackTrace(printwriter);
            LOGGER.error(buffer.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Sets the image on the given dial.
     * The given image is pulled from within the jar file an therefore must be places inside the resources directory.
     * This method will then perform a multipart/form-data upload.
     * 
     * @param   dialUid     The UID of the dial that will get the new value
     * @param   dialImage   The new image that will be set on the dial
     */
    public static void setDialImage(String dialUid, String dialImage) {
        try {

            String apiUrl = MCVUDialsConfig.vuServerApiBaseUrl + "/api/v0/dial/" + dialUid + "/image/set?key="
                    + MCVUDialsConfig.vuServerApiKey + "&imgfile=" + dialImage;

            String boundary = "===" + System.currentTimeMillis() + "===";

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setDoOutput(true);

            InputStream fileStream = MCVUDials.class.getResourceAsStream("/" + dialImage);

            try (OutputStream outputStream = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"imgfile\"; filename=\"").append(dialImage)
                        .append("\"\r\n");
                writer.append("Content-Type: ").append("multipart/form-data").append("\r\n");
                writer.append("\r\n").flush();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();

                writer.append("\r\n").flush();
                writer.append("--").append(boundary).append("--\r\n").flush();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            connection.disconnect();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
