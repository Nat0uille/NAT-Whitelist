package fr.Nat0uille.NATWhitelist.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class WhitelistListener {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private boolean enabled;

    public WhitelistListener(JavaPlugin plugin) {
        this.plugin = plugin;
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
        return config.contains("players." + playerName);
    }

    public List<String> getWhitelistedPlayers() {
        return new ArrayList<>(config.getConfigurationSection("players").getKeys(false));
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

    public void toggleEnabled() {
        setEnabled(!enabled);
    }
}