package fr.Nat0uille.NATWhitelist;

import fr.Nat0uille.NATWhitelist.API.*;
import fr.Nat0uille.NATWhitelist.Commands.*;
import fr.Nat0uille.NATWhitelist.TabCompleter.*;
import fr.Nat0uille.NATWhitelist.Listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Main extends JavaPlugin {
    private Whitelist whitelist;
    private Connection sqlConnection;
    private CheckVersion checkVersion;
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveAllLangResources();
        loadLang();

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage("");
        console.sendMessage("          §c___   "+ getDescription().getName());
        console.sendMessage("§c|\\ |  /\\   |    §4Made by §cNat0uille");
        console.sendMessage("§c| \\| /~~\\  |    §4Version §c" + getDescription().getVersion());
        console.sendMessage("");

        // Database connection setup
        String type = getConfig().getString("database.type");
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String dbName = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");

        try {
            if ("MySQL".equalsIgnoreCase(type)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                sqlConnection = DriverManager.getConnection(url, username, password);
                try (Statement stmt = sqlConnection.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nat_whitelist (player_name VARCHAR(16) PRIMARY KEY, uuid VARCHAR(36))");
                } catch (SQLException e) {
                    getLogger().severe("Error creating table nat_whitelist: " + e.getMessage());
                }
            } else {
                Class.forName("org.sqlite.JDBC");
                if (!getDataFolder().exists()) {
                    getDataFolder().mkdirs();
                }
                String url = "jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/database.db";
                sqlConnection = DriverManager.getConnection(url);

                try (Statement stmt = sqlConnection.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS nat_whitelist (player_name TEXT PRIMARY KEY, uuid TEXT)");
                } catch (SQLException e) {
                    getLogger().severe("Error creating table nat_whitelist: " + e.getMessage());
                }
            }
            whitelist = new Whitelist(this, sqlConnection);
        } catch (Exception e) {
            getLogger().severe("Unable to connect to the SQL database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Commands and TabCompleter
        WhitelistTabCompleter tabCompleter = new WhitelistTabCompleter(whitelist);

        getCommand("whitelist").setExecutor(new WhitelistCommand(this, whitelist));
        getCommand("whitelist").setTabCompleter(tabCompleter);

        Bukkit.getScheduler().runTaskTimer(this, tabCompleter::updateCache, 0L, 20L);

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(whitelist, this), this);

        // Check version
        checkVersion = new CheckVersion();
        CheckVersion.startVersionCheck(this, checkVersion);

        // API
        NATWhitelistAPI api = new NATWhitelistImpl(this);
        getServer().getServicesManager().register(NATWhitelistAPI.class, api, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(this);
    }

    public CheckVersion getCheckVersion() {
        return checkVersion;
    }
    
    public Whitelist getWhitelistListener() {
    return whitelist;
}

    public void loadLang() {
        String lang = getConfig().getString("lang");
        File langFile = new File(getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            saveResource("lang/" + lang + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLangMessage(String key) {
        return langConfig.getString(key, "Message not found, please check your language file!");
    }

    private void saveAllLangResources() {
        String[] langs = {"en-us.yml", "fr-fr.yml"};
        File langDir = new File(getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();
        for (String langFile : langs) {
            File outFile = new File(langDir, langFile);
            if (!outFile.exists()) {
                saveResource("lang/" + langFile, false);
            }
        }
    }
}