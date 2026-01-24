package fr.Nat0uille.NATWhitelist;

import fr.Nat0uille.NATWhitelist.Commands.WhitelistCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Main extends Plugin {

    private CheckVersion checkVersion;
    private Configuration config;
    private Configuration langConfig;
    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        boolean firstRun = !new File(getDataFolder(), "config.yml").exists();
        loadConfig();
        migrateConfig();
        loadLang();

        getLogger().info("");
        getLogger().info("          " + ChatColor.RED + "___   " + getDescription().getName());
        getLogger().info(ChatColor.RED + "|\\ |  /\\   |    " + ChatColor.DARK_RED + "Made by " + ChatColor.RED + "Nat0uille");
        getLogger().info(ChatColor.RED + "| \\| /~~\\  |    " + ChatColor.DARK_RED + "Version " + ChatColor.RED + getDescription().getVersion());
        getLogger().info("");

        if (firstRun) {
            displayFirstRunMessage();
        }

        dbManager = new DatabaseManager();

        String type = config.getString("database.type");
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String dbName = config.getString("database.database");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

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
            return;
        }

        dbManager.execute("CREATE TABLE IF NOT EXISTS nat_whitelist (player_name VARCHAR(16) PRIMARY KEY, uuid VARCHAR(36))");

        WhitelistManager whitelistManager = new WhitelistManager(dbManager, playerName -> ProxyServer.getInstance().getPlayer(playerName) != null);
        WhitelistHandler whitelistHandler = new WhitelistHandler(this, whitelistManager);

        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerListener(this, whitelistManager, whitelistHandler));

        WhitelistTabCompleter tabCompleter = new WhitelistTabCompleter(whitelistManager);

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhitelistCommand(this, whitelistManager, whitelistHandler, tabCompleter, "whitelistbungee", "natwhitelistbungee", "wlb"));

        ProxyServer.getInstance().getScheduler().schedule(this, tabCompleter::updateCache, 0L, 1L, TimeUnit.SECONDS);

        checkVersion = new CheckVersion();
        CheckVersion.startVersionCheck(this, checkVersion);
    }

    @Override
    public void onDisable() {
        if (dbManager != null) {
            dbManager.disconnect();
        }
    }

    public CheckVersion getCheckVersion() {
        return checkVersion;
    }

    public void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                try (InputStream in = getResourceAsStream("config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath());
                    }
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public void loadLang() {
        try {
            String lang = config.getString("lang");
            File langDir = new File(getDataFolder(), "languages");
            if (!langDir.exists()) {
                langDir.mkdirs();
            }

            File langFile = new File(langDir, lang + ".yml");
            if (!langFile.exists()) {
                try (InputStream in = getResourceAsStream("languages/" + lang + ".yml")) {
                    if (in != null) {
                        Files.copy(in, langFile.toPath());
                    }
                }
            }
            langConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(langFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLangMessage(String key) {
        String message = langConfig.getString(key);
        if (message == null) {
            String lang = config.getString("lang", "en-us");
            String notFound = notFoundMessages.getOrDefault(lang, notFoundMessages.get("en-us"));
            return notFound.replace("{key}", key);
        }
        return message;
    }

    private final Map<String, String> notFoundMessages = Map.of(
            "en-us", "Message not found, please check {key} in your language file! (en-us.yml)",
            "fr-fr", "Message introuvable, vérifiez {key} dans votre fichier de langue ! (fr-fr.yml)",
            "es-es", "Mensaje no encontrado, compruebe {key} en su archivo de idioma (es-es.yml)"
    );

    private void displayFirstRunMessage() {
        getLogger().info("");
        getLogger().info("");
        getLogger().info("");
        getLogger().info("Thank you very much for installing NAT-Whitelist!");
        getLogger().info("");
        getLogger().info("IMPORTANT: Be sure to configure your database! ");
        getLogger().info("");
        getLogger().info("Thank you for using NAT-Whitelist on your server, I'm very grateful and hope you enjoy it!");
        getLogger().info("- Nat0uille");
        getLogger().info("");
        getLogger().info("If you need help with this plugin, come to my Discord: https://nat0uille.com/discord");
        getLogger().info("");
        getLogger().info("");
        getLogger().info("");
    }

    private void migrateConfig() {
        ConfigMigration.migrateToV2(
                defaultValue -> config.getString("config-version", defaultValue),
                config::contains,
                key -> config.getBoolean(key, true),
                config::set,
                this::saveConfig
        );
    }
}
