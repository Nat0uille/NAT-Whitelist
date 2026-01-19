package fr.Nat0uille.NATWhitelist;

import fr.Nat0uille.NATWhitelist.Commands.*;
import fr.Nat0uille.NATWhitelist.TabCompleter.*;
import fr.Nat0uille.NATWhitelist.Listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public final class Main extends JavaPlugin {

    private CheckVersion checkVersion;
    private FileConfiguration langConfig;

    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        boolean firstRun = saveCommonResources();
        migrateConfig();
        loadLang();

        int pluginId = 28891;
        new Metrics(this, pluginId);

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage("");
        console.sendMessage("          §c___   "+ getDescription().getName());
        console.sendMessage("§c|\\ |  /\\   |    §4Made by §cNat0uille");
        console.sendMessage("§c| \\| /~~\\  |    §4Version §c" + getDescription().getVersion());
        console.sendMessage("");

        if (firstRun) {
            displayFirstRunMessage(console);
        }

        dbManager = new DatabaseManager();

        String type = getConfig().getString("database.type");
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String dbName = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");

        boolean connected;
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

        WhitelistManager whitelistManager = new WhitelistManager(dbManager, playerName -> Bukkit.getPlayer(playerName) != null);
        WhitelistHandler whitelistHandler = new WhitelistHandler(this, whitelistManager);

        getServer().getPluginManager().registerEvents(new PlayerListener(this, whitelistManager, whitelistHandler), this);

        WhitelistTabCompleter tabCompleter = new WhitelistTabCompleter(whitelistManager);

        getCommand("whitelist").setExecutor(new WhitelistCommand(this, whitelistManager, whitelistHandler));
        getCommand("whitelist").setTabCompleter(tabCompleter);

        Bukkit.getScheduler().runTaskTimer(this, tabCompleter::updateCache, 0L, 20L);


        checkVersion = new CheckVersion();
        CheckVersion.startVersionCheck(this, checkVersion);

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


    public void loadLang() {
        String lang = getConfig().getString("lang");
        File langFile = new File(getDataFolder(), "languages/" + lang + ".yml");
        if (!langFile.exists()) {
            saveResource("languages/" + lang + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLangMessage(String key) {
        String message = langConfig.getString(key);
        if (message == null) {
            String lang = getConfig().getString("languages", "en-us");
            String notFound = notFoundMessages.getOrDefault(lang, notFoundMessages.get("en-us"));
            return notFound.replace("{key}", key);
        }
        return message;
    }

    private final Map<String, String> notFoundMessages = Map.of(
            "en-us", "Message not found, please check {key} in your language file! (en-us.yml)",
            "fr-fr", "Message introuvable, vérifiez {key} dans votre fichier de langue ! (fr-fr.yml)"
    );




    private void displayFirstRunMessage(ConsoleCommandSender console) {
        console.sendMessage("");
        console.sendMessage("");
        console.sendMessage("");
        console.sendMessage("Thank you very much for installing NAT-Whitelist!");
        console.sendMessage("");
        console.sendMessage("IMPORTANT: Be sure to configure your database! ");
        console.sendMessage("");
        console.sendMessage("Thank you for using NAT-Whitelist on your server, I'm very grateful and hope you enjoy it!");
        console.sendMessage("- Nat0uille");
        console.sendMessage("");
        console.sendMessage("If you need help with this plugin, come to my Discord: https://nat0uille.com/discord");
        console.sendMessage("");
        console.sendMessage("");
        console.sendMessage("");
    }

    private void migrateConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();

        ConfigMigration.migrateToV2(
                defaultValue -> config.getString("config-version", defaultValue),
                config::contains,
                key -> config.getBoolean(key, true),
                config::set,
                this::saveConfig
        );
    }


    public boolean saveCommonResources() {
        // Sauvegarder config.yml depuis common
        File configFile = new File(getDataFolder(), "config.yml");
        boolean isFirstRun = !configFile.exists();

        if (isFirstRun) {
            saveResource("config.yml", false);
        }

        // Sauvegarder les fichiers de langues depuis common
        File langDir = new File(getDataFolder(), "languages");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        String[] commonLangs = {"en-us.yml", "fr-fr.yml"};
        for (String langFile : commonLangs) {
            File outFile = new File(langDir, langFile);
            if (!outFile.exists()) {
                saveResource("languages/" + langFile, false);
            }
        }

        return isFirstRun;
    }
}
