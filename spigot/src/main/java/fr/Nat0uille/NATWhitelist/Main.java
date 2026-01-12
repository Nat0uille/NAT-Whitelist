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
import java.util.Map;

public final class Main extends JavaPlugin {
    private Whitelist whitelist;
    private Connection sqlConnection;
    private DatabaseManager dbManager;
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

        dbManager = new DatabaseManager();

        String type = getConfig().getString("database.type");
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String dbName = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");

        boolean connected = false;
        if ("MySQL".equalsIgnoreCase(type)) {
            connected = dbManager.connectMySQL(host, port, dbName, username, password);
        } else if ("MariaDB".equalsIgnoreCase(type)) {
            connected = dbManager.connectMariaDB(host, port, dbName, username, password);
        } else if ("H2".equalsIgnoreCase(type)) {
            String path = getDataFolder().getAbsolutePath() + "/database";
            connected = dbManager.connectH2(path);
        } else {
            String path = getDataFolder().getAbsolutePath() + "/database";
            connected = dbManager.connectH2(path);
        }

        if (!connected) {
            getLogger().severe("Unable to connect to the database!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dbManager.execute("CREATE TABLE IF NOT EXISTS nat_whitelist (player_name VARCHAR(16) PRIMARY KEY, uuid VARCHAR(36))");

        try {
            sqlConnection = dbManager.getConnection();
            whitelist = new Whitelist(this, sqlConnection);
        } catch (Exception e) {
            getLogger().severe("Unable to connect to the SQL database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        WhitelistTabCompleter tabCompleter = new WhitelistTabCompleter(whitelist);

        getCommand("whitelist").setExecutor(new WhitelistCommand(this, whitelist));
        getCommand("whitelist").setTabCompleter(tabCompleter);

        Bukkit.getScheduler().runTaskTimer(this, tabCompleter::updateCache, 0L, 20L);

        getServer().getPluginManager().registerEvents(new PlayerListener(whitelist, this), this);

        checkVersion = new CheckVersion();
        CheckVersion.startVersionCheck(this, checkVersion);

        NATWhitelistAPI api = new NATWhitelistImpl(this);
        getServer().getServicesManager().register(NATWhitelistAPI.class, api, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        if (dbManager != null) {
            dbManager.disconnect();
        }
        getServer().getServicesManager().unregister(this);
    }

    public CheckVersion getCheckVersion() {
        return checkVersion;
    }
    
    public Whitelist getWhitelistListener() {
        return whitelist;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
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
        String message = langConfig.getString(key);
        if (message == null) {
            String lang = getConfig().getString("lang", "en-us");
            String notFound = notFoundMessages.getOrDefault(lang, notFoundMessages.get("en-us"));
            return notFound.replace("{key}", key);
        }
        return message;
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

    private final Map<String, String> notFoundMessages = Map.of(
            "en-us", "Message not found, please check {key} in your language file! (en-us.yml)",
            "fr-fr", "Message introuvable, vérifiez {key} dans votre fichier de langue ! (fr-fr.yml)"
    );
}
