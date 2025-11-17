package fr.Nat0uille.NATWhitelist;

import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.URL;
import java.net.HttpURLConnection;
import com.google.gson.JsonObject;

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

    public boolean add(UUID uuid, String playerName) throws SQLException {
        String type = main.getConfig().getString("database.type");
        String sql = "MySQL".equalsIgnoreCase(type)
            ? "INSERT IGNORE INTO nat_whitelist (player_name, uuid) VALUES (?, ?)"
            : "INSERT OR IGNORE INTO nat_whitelist (player_name, uuid) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setString(2, uuid.toString());
            return stmt.executeUpdate() > 0;
        }
    }


    public boolean remove(UUID uuid) throws SQLException {
        String sql = "DELETE FROM nat_whitelist WHERE uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isWhitelisted(UUID uuid) throws SQLException {
        String sql = "SELECT 1 FROM nat_whitelist WHERE uuid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
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
        return String.join(", ", players);
    }

    public void setEnabled(boolean enabled) {
        main.getConfig().set("enabled", enabled);
        main.saveConfig();
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
                if (!isWhitelisted(player.getUniqueId()) || !player.hasPermission("natwhitelist.bypass")) {
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
                    remove(uuid);
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
}