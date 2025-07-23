package fr.Nat0uille.NATWhitelist.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class WhitelistListener {
    private final File file;
    private final YamlConfiguration config;
    private boolean enabled;

    public WhitelistListener(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "whitelist.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.enabled = config.getBoolean("enabled", false);
    }

    public boolean add(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = player.getUniqueId();
        String path = "players." + playerName;

        if (!config.contains(path)) {
            config.set(path, uuid.toString());
            save();
            return true;
        }
        return false;
    }

    public boolean remove(String playerName) {
        String path = "players." + playerName;
        if (config.contains(path)) {
            config.set(path, null);
            save();
            return true;
        }
        return false;
    }

    public boolean isWhitelisted(String playerName) {
        List<String> whitelistedPlayers = getWhitelistedPlayers();
        return whitelistedPlayers.contains(playerName);
    }

    public List<String> getWhitelistedPlayers() {
        YamlConfiguration freshConfig = YamlConfiguration.loadConfiguration(file);
        if (freshConfig.contains("players")) {
            return new ArrayList<>(freshConfig.getConfigurationSection("players").getKeys(false));
        }
        return new ArrayList<>();
    }

    public String listWhitelistedPlayers() {
        List<String> players = getWhitelistedPlayers();
        return String.join(", ", players);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        config.set("enabled", enabled);
        save();
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
                JSONParser parser = new JSONParser();
                JSONObject response = (JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()));
                return (String) response.get("name");
            }
        } catch (Exception ignored) {}
        return null;
    }
}