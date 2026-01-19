package fr.Nat0uille.NATWhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.command.CommandManager;
import fr.Nat0uille.NATWhitelist.Commands.WhitelistCommand;
import fr.Nat0uille.NATWhitelist.Listeners.PlayerListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.ConfigurationNode;

@Plugin(
        id = "natwhitelist",
        name = "NAT-Whitelist",
        version = "${version}",
        authors = {"Nat0uille"}
)
public class Main {

    private final ProxyServer server;
    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigurationNode config;
    private ConfigurationNode langConfig;
    private final Path configPath;
    private final Path langDirectory;

    private CheckVersion checkVersion;
    private DatabaseManager dbManager;
    private WhitelistManager whitelistManager;
    private WhitelistHandler whitelistHandler;
    private final Metrics.Factory metricsFactory;

    @Inject
    public Main(ProxyServer server, PluginContainer pluginContainer, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.server = server;
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            logger.error("Failed to create data directory", e);
        }
        this.configPath = dataDirectory.resolve("config.yml");
        this.langDirectory = dataDirectory.resolve("languages");
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        boolean firstRun = saveCommonResources();
        loadConfig();
        migrateConfig();
        loadLang();

        int pluginId = 28892;
        Metrics metrics = metricsFactory.make(this, pluginId);

        server.getConsoleCommandSource().sendMessage(Component.empty());
        server.getConsoleCommandSource().sendMessage(
                Component.text("          ___   NAT-Whitelist", NamedTextColor.RED)
        );
        server.getConsoleCommandSource().sendMessage(
                Component.text("|\\ |  /\\   |    ", NamedTextColor.RED)
                        .append(Component.text("Made by ", NamedTextColor.DARK_RED))
                        .append(Component.text("Nat0uille", NamedTextColor.RED))
        );
        server.getConsoleCommandSource().sendMessage(
                Component.text("| \\| /~~\\  |    ", NamedTextColor.RED)
                        .append(Component.text("Version ", NamedTextColor.DARK_RED))
                        .append(Component.text(getVersion(), NamedTextColor.RED))
        );
        server.getConsoleCommandSource().sendMessage(Component.empty());

        if (firstRun) {
            displayFirstRunMessage();
        }

        // Initialize database
        dbManager = new DatabaseManager();

        String type = config.node("database", "type").getString("H2");
        String host = config.node("database", "host").getString("localhost");
        int port = config.node("database", "port").getInt(3306);
        String dbName = config.node("database", "database").getString("natwhitelist");
        String username = config.node("database", "username").getString("root");
        String password = config.node("database", "password").getString("");

        boolean connected = false;
        if ("MySQL".equalsIgnoreCase(type)) {
            connected = dbManager.connectMySQL(host, port, dbName, username, password);
        } else if ("MariaDB".equalsIgnoreCase(type)) {
            connected = dbManager.connectMariaDB(host, port, dbName, username, password);
        } else if ("H2".equalsIgnoreCase(type)) {
            String path = dataDirectory.resolve("database").toAbsolutePath().toString();
            connected = dbManager.connectH2(path);
        } else {
            String path = dataDirectory.resolve("database").toAbsolutePath().toString();
            connected = dbManager.connectH2(path);
        }

        if (!connected) {
            logger.error("Unable to connect to the database!");
            return;
        }

        dbManager.execute("CREATE TABLE IF NOT EXISTS nat_whitelist (player_name VARCHAR(16) PRIMARY KEY, uuid VARCHAR(36))");

        // Initialize managers and handler
        whitelistManager = new WhitelistManager(dbManager, playerName ->
            server.getPlayer(playerName).isPresent()
        );
        whitelistHandler = new WhitelistHandler(this, whitelistManager, server);

        // Register listeners
        server.getEventManager().register(this, new PlayerListener(this, whitelistManager, whitelistHandler, server));

        // Register commands
        CommandManager commandManager = server.getCommandManager();
        commandManager.register(
            commandManager.metaBuilder("whitelist")
                .aliases("wl", "natwhitelist")
                .build(),
            new WhitelistCommand(this, whitelistManager, whitelistHandler, server)
        );

        // Start version checker
        checkVersion = new CheckVersion();
        CheckVersion.startVersionCheck(this, checkVersion, server, logger);

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (dbManager != null) {
            dbManager.disconnect();
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public CheckVersion getCheckVersion() {
        return checkVersion;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    public ConfigurationNode getConfig() {
        return config;
    }

    public String getVersion() {
        return pluginContainer.getDescription().getVersion().orElse("Unknown");
    }

    public void reloadConfig() {
        loadConfig();
    }

    public void loadLang() {
        try {
            String lang = config.node("lang").getString("en-us");
            Path langFile = langDirectory.resolve(lang + ".yml");

            if (!Files.exists(langFile)) {
                saveResource("languages/" + lang + ".yml");
            }

            YamlConfigurationLoader langLoader = YamlConfigurationLoader.builder()
                    .path(langFile)
                    .build();
            langConfig = langLoader.load();
        } catch (IOException e) {
            logger.error("Failed to load language file", e);
        }
    }

    public String getLangMessage(String key) {
        ConfigurationNode node = langConfig.node((Object[]) key.split("\\."));
        String message = node.getString();

        if (message == null) {
            String lang = config.node("lang").getString("en-us");
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
        logger.info("");
        logger.info("");
        logger.info("");
        logger.info("Thank you very much for installing NAT-Whitelist!");
        logger.info("");
        logger.info("IMPORTANT: Be sure to configure your database! ");
        logger.info("");
        logger.info("Thank you for using NAT-Whitelist on your server, I'm very grateful and hope you enjoy it!");
        logger.info("- Nat0uille");
        logger.info("");
        logger.info("If you need help with this plugin, come to my Discord: https://nat0uille.com/discord");
        logger.info("");
        logger.info("");
        logger.info("");
    }

    private void migrateConfig() {
        ConfigMigration.migrateToV2(
                defaultValue -> {
                    ConfigurationNode node = config.node("config-version");
                    return node.isNull() ? defaultValue : node.getString(defaultValue);
                },
                key -> !config.node((Object[]) key.split("\\.")).isNull(),
                key -> config.node((Object[]) key.split("\\.")).getBoolean(true),
                (key, value) -> {
                    try {
                        config.node((Object[]) key.split("\\.")).set(value);
                    } catch (Exception e) {
                        logger.error("Failed to migrate config", e);
                    }
                },
                this::saveConfig
        );
    }

    public void saveConfig() {
        try {
            YamlConfigurationLoader loader = createLoader();
            loader.save(config);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    private YamlConfigurationLoader createLoader() {
        return YamlConfigurationLoader.builder()
                .path(configPath)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .build();
    }

    private void loadConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            YamlConfigurationLoader loader = createLoader();
            if (!Files.exists(configPath)) {
                config = loader.createNode();
                loader.save(config);
            }
            config = loader.load();
        } catch (IOException e) {
            logger.error("Failed to load config", e);
            config = null;
        }
    }

    private boolean saveCommonResources() {
        boolean isFirstRun = !Files.exists(configPath);

        if (isFirstRun) {
            saveResource("config.yml");
        }

        try {
            Files.createDirectories(langDirectory);
        } catch (IOException e) {
            logger.error("Failed to create languages directory", e);
        }

        String[] commonLangs = {"en-us.yml", "fr-fr.yml", "es-es.yml"};
        for (String langFile : commonLangs) {
            Path outFile = langDirectory.resolve(langFile);
            if (!Files.exists(outFile)) {
                saveResource("languages/" + langFile);
            }
        }

        return isFirstRun;
    }

    private void saveResource(String resourcePath) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                Path outPath = dataDirectory.resolve(resourcePath);
                Files.createDirectories(outPath.getParent());
                Files.copy(in, outPath);
            } else {
                logger.warn("Resource not found: " + resourcePath);
            }
        } catch (IOException e) {
            logger.error("Failed to save resource: " + resourcePath, e);
        }
    }
}