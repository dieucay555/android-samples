package com.urbanairship.richpush.sample.test;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class to send push notifications
 *
 */
public class PushSender {
    private static final String TAG = "RichPushSampleUiTests";
    private static final String RICH_PUSH_BROADCAST_URL = "https://go.urbanairship.com/api/airmail/send/broadcast/";
    private static final String RICH_PUSH_URL = "https://go.urbanairship.com/api/airmail/send/";

    private final String masterSecret;
    private final String appKey;

    public enum SendPushType {
        BROADCAST,
        RICH_PUSH_USER,
        ALIAS,
        TAG
    }

    /**
     * Constructor for PushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     */
    public PushSender(String masterSecret, String appKey) {
        this.masterSecret = masterSecret;
        this.appKey = appKey;
    }

    /**
     * Sends a rich push message
     * @throws Exception
     */
    public void sendRichPushMessage() throws Exception {
        sendRichPushMessage("");
    }

    /**
     * Sends a rich push message to an activity
     * @param activity The specified activity to send the rich push message to
     * @throws Exception
     */
    public void sendRichPushMessage(String activity) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"push\": {\"android\": { \"alert\": \"Rich Push Alert\", \"extra\": { \"activity\": \"" + activity + "\" } } },");
        builder.append("\"title\": \"Rich Push Title\",");
        builder.append("\"message\": \"Rich Push Message\",");
        builder.append("\"content-type\": \"text/html\"}");

        String json = builder.toString();
        String basicAuthString =  "Basic "+Base64.encodeToString(String.format("%s:%s", appKey, masterSecret).getBytes(), Base64.NO_WRAP);
        URL url = new URL(RICH_PUSH_BROADCAST_URL);
        try {
            sendMessage(url, json, basicAuthString);
        } catch (Exception ex) {
            // Try again if we fail for whatever reason
            Thread.sleep(3000);
            sendMessage(url, json, basicAuthString);
        }
    }

    /**
     * Sends a rich push message via a specific push type
     * @param type The specified push type
     * @param string The string related to the type of push
     * @throws Exception
     */
    public void sendRichPushMessage(SendPushType type, String pushString) throws Exception {
        URL url = new URL(RICH_PUSH_URL);
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"push\": {\"android\": { \"alert\": \"Rich Push Alert\", \"extra\": { \"activity\": \"\" } } },");

        switch (type) {
        case RICH_PUSH_USER:
            builder.append("\"users\": [\"" + pushString + "\"],");
            break;
        case ALIAS:
            builder.append("\"aliases\": [\"" + pushString + "\", \"anotherAlias\"],");
            break;
        case TAG:
            builder.append("\"tags\": [\"" + pushString + "\"],");
            break;
        case BROADCAST:
        default:
            url = new URL(RICH_PUSH_BROADCAST_URL);
            break;
        }
        builder.append("\"title\": \"Rich Push Title\",");
        builder.append("\"message\": \"Rich Push Message\",");
        builder.append("\"content-type\": \"text/html\"}");

        String json = builder.toString();
        String basicAuthString =  "Basic "+Base64.encodeToString(String.format("%s:%s", appKey, masterSecret).getBytes(), Base64.NO_WRAP);

        try {
            sendMessage(url, json, basicAuthString);
        } catch (Exception ex) {
            // Try again if we fail for whatever reason
            Thread.sleep(3000);
            sendMessage(url, json, basicAuthString);
        }
    }

    /**
     * Actually sends the rich push message
     * @param url The specified url the message is sent to
     * @param message The json formatted message to be sent
     * @param basicAuthString The basicAuthString to be sent
     * @throws IOException
     */
    private void sendMessage(URL url, String message, String basicAuthString) throws IOException {
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Content-Type",
                    "application/json");

            conn.setRequestProperty("Authorization", basicAuthString);

            // Create the form content
            OutputStream out = conn.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(message);
            writer.close();
            out.close();

            if (conn.getResponseCode() != 200) {
                Log.e(TAG, "Sending rich push failed with: " + conn.getResponseCode() + " " + conn.getResponseMessage() + " Message: " + message);
                throw new IOException(conn.getResponseMessage());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
