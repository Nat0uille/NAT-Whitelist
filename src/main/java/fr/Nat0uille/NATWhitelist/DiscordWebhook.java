package fr.Nat0uille.NATWhitelist;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    /**
     * Sends an embed message to Discord via webhook
     * @param title The title of the embed
     * @param description The description of the embed
     * @return true if the webhook was sent successfully, false otherwise
     */
    public boolean sendEmbed(String title, String description) {
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equalsIgnoreCase("none")) {
            return false;
        }

        try {
            String jsonPayload = "{"
                    + "\"embeds\":[{"
                    + "\"title\":\"" + escapeJson(title) + "\","
                    + "\"description\":\"" + escapeJson(description) + "\","
                    + "\"color\":13107200,"
                    + "\"footer\":{"
                    +     "\"text\":\"NAT-Whitelist\","
                    +     "\"icon_url\":\"https://i.imgur.com/qxAdLlM.jpeg\""
                    + "},"
                    + "\"timestamp\":\"" + java.time.Instant.now().toString() + "\""
                    + "}]"
                    + "}";

            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            return responseCode == 204 || responseCode == 200;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}

