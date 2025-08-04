package fr.Nat0uille.NATWhitelist.Listeners;

import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import fr.Nat0uille.NATWhitelist.Main;
import java.net.URL;
import java.net.HttpURLConnection;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class WhitelistListener {
    private final Connection conn;
    private Main main;
    private boolean enabled = false;

    public WhitelistListener(Main main, Connection conn) {
        this.main = main;
        this.conn = conn;
    }

    public boolean add(String playerName) throws SQLException {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = player.getUniqueId();
        String type = main.getConfig().getString("database.type");
        String sql;
        if ("MySQL".equalsIgnoreCase(type)) {
            sql = "INSERT IGNORE INTO nat_whitelist (player_name, uuid) VALUES (?, ?)";
        } else {
            sql = "INSERT OR IGNORE INTO nat_whitelist (player_name, uuid) VALUES (?, ?)";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setString(2, uuid.toString());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean remove(String playerName) throws SQLException {
        String sql = "DELETE FROM nat_whitelist WHERE player_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isWhitelisted(String playerName) throws SQLException {
        String sql = "SELECT 1 FROM nat_whitelist WHERE player_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
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
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
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

    public void kickNonWhitelistedPlayers(Main main) throws SQLException {
        MiniMessage mm = MiniMessage.miniMessage();
        Component prefix = mm.deserialize(main.getConfig().getString("prefix"));
        Component kickmessage = mm.deserialize(main.getConfig().getString("kickmessage"));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isWhitelisted(player.getName())) {
                player.kick(prefix.append(kickmessage));
            }
        }
    }
}