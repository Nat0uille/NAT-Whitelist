package fr.Nat0uille.NATWhitelist;

import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class Whitelist {
    private final Connection conn;
    private Main main;
    private final List<String> removedPlayers = new ArrayList<>();

    public Whitelist(Main main, Connection conn) {
        this.main = main;
        this.conn = conn;
    }

    public boolean add(UUID uuid) throws SQLException {
        String type = main.getConfig().getString("database.type");
        String sql = "MySQL".equalsIgnoreCase(type)
                ? "INSERT IGNORE INTO nat_whitelist (player_name, uuid) VALUES (?, ?)"
                : "INSERT OR IGNORE INTO nat_whitelist (player_name, uuid) VALUES (?, ?)";
        String playerName = null;
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            playerName = online.getName();
        } else {
            playerName = Bukkit.getOfflinePlayer(uuid).getName();
        }
        if (playerName == null) {
            playerName = uuid.toString();
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setString(2, uuid.toString());
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                String title = main.getLangMessage("webhook-add-title");
                String desc = main.getLangMessage("webhook-add-desc")
                    .replace("{player}", playerName);
                SendDiscordWebhook(title, desc);
            }
            return result;
        }
    }

    public boolean remove(UUID uuid) throws SQLException {
        String type = main.getConfig().getString("database.type");
        String sql = "DELETE FROM nat_whitelist WHERE uuid = ?";
        String playerName = getPlayerNameByUUID(uuid);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                String title = main.getLangMessage("webhook-remove-title");
                String desc = main.getLangMessage("webhook-remove-desc")
                    .replace("{player}", playerName);
                SendDiscordWebhook(title, desc);
            }
            return result;
        }
    }

    public boolean removeOffline(UUID uuid) throws SQLException {
        String type = main.getConfig().getString("database.type");
        String sql = "DELETE FROM nat_whitelist WHERE uuid = ?";
        String playerName = getPlayerNameByUUID(uuid);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                String title = main.getLangMessage("webhook-removeoffline-title");
                String desc = main.getLangMessage("webhook-removeoffline-desc")
                    .replace("{player}", playerName);
                SendDiscordWebhook(title, desc);
            }
            return result;
        }
    }

    public boolean isWhitelisted(UUID uuid) {
        String sql = "SELECT 1 FROM nat_whitelist WHERE uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getWhitelistedPlayers() throws SQLException {
        List<String> players = new ArrayList<>();
        String sql = "SELECT player_name FROM nat_whitelist";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                players.add(rs.getString("player_name"));
            }
        }
        return players;
    }

    public String listWhitelistedPlayers() throws SQLException {
        List<String> players = getWhitelistedPlayers();
        List<String> coloredPlayers = new ArrayList<>();
        for (String playerName : players) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                coloredPlayers.add("<#63c74d>" + playerName);
            } else {
                coloredPlayers.add("<#951919>" + playerName);
            }
        }
        return String.join(", ", coloredPlayers);
    }

    public void setEnabled(boolean enabled) {
        main.getConfig().set("enabled", enabled);
        main.saveConfig();
        String title, desc;
        if (enabled) {
            title = main.getLangMessage("webhook-on-title");
            desc = main.getLangMessage("webhook-on-desc");
        } else {
            title = main.getLangMessage("webhook-off-title");
            desc = main.getLangMessage("webhook-off-desc");
        }
        SendDiscordWebhook(title, desc);
    }

    public boolean isEnabled() {
        return main.getConfig().getBoolean("enabled", false);
    }

    public static String getCorrectUsernameFromMojang(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            if (connection.getResponseCode() == 200) {
                JsonObject response = new JsonParser()
                    .parse(new java.io.InputStreamReader(connection.getInputStream()))
                    .getAsJsonObject();
                return response.get("name").getAsString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void kickNoWhitelistedPlayers(Main main) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component prefix = mm.deserialize(main.getLangMessage("prefix"));
        Component kickmessage = mm.deserialize(main.getLangMessage("kickmessage"));
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (!isWhitelisted(player.getUniqueId()) && !player.hasPermission("natwhitelist.bypass")) {
                    player.kick(prefix.append(kickmessage));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeNoWhitelistedPlayers(Main main) throws SQLException {
        removedPlayers.clear();
        for (String playerName : getWhitelistedPlayers()) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null || !player.isOnline()) {
                String uuidStr = null;
                String sql = "SELECT uuid FROM nat_whitelist WHERE player_name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, playerName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            uuidStr = rs.getString("uuid");
                        }
                    }
                }
                if (uuidStr != null) {
                    UUID uuid = UUID.fromString(uuidStr);
                    removeOffline(uuid);
                    removedPlayers.add(playerName);
                }
            }
        }
    }

    public List<String> getRemovedPlayers() {
        return removedPlayers;
    }

    public String getPlayerNameByUUID(UUID uuid) throws SQLException {
        String sql = "SELECT player_name FROM nat_whitelist WHERE uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("player_name");
                }
            }
        }
        return null;
    }

    public void updatePlayerName(UUID uuid, String newName) throws SQLException {
        String sql = "UPDATE nat_whitelist SET player_name = ? WHERE uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        }
    }

    public void SendDiscordWebhook(String title, String description) {
        String url = main.getConfig().getString("discord-webhook-url");
        if (url == null) return;
        try {
            String jsonPayload = "{"
                    + "\"embeds\":[{"
                    + "\"title\":\"" + title.replace("\"", "\\\"") + "\","
                    + "\"description\":\"" + description.replace("\"", "\\\"") + "\","
                    + "\"color\":13107200,"
                    + "\"footer\":{"
                    +     "\"text\":\"NAT-Whitelist\","
                    +     "\"icon_url\":\"https://i.imgur.com/qxAdLlM.jpeg\""
                    + "},"
                    + "\"timestamp\":\"" + java.time.Instant.now().toString() + "\""
                    + "}]"
                    + "}";
            java.net.URL webhookUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) webhookUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode != 204 && main != null) {
                main.getLogger().warning("Discord webhook send failed: code " + responseCode);
            }
        } catch (IOException e) {
            if (main != null) {
                main.getLogger().warning("Error sending Discord webhook: " + e.getMessage());
            }
        }
    }
}