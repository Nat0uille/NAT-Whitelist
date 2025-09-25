package fr.Nat0uille.NATWhitelist;

import fr.Nat0uille.NATWhitelist.API.*;
import fr.Nat0uille.NATWhitelist.Commands.*;
import fr.Nat0uille.NATWhitelist.TabCompleter.*;
import fr.Nat0uille.NATWhitelist.Listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Main extends JavaPlugin {
    private WhitelistListener whitelistListener;
    private WhitelistTabCompleter tabCompleter;
    private Connection sqlConnection;
    private CheckVersion checkVersion;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
            whitelistListener = new WhitelistListener(this, sqlConnection);
        } catch (Exception e) {
            getLogger().severe("Unable to connect to the SQL database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Commands and TabCompleter
        tabCompleter = new WhitelistTabCompleter(whitelistListener);

        getCommand("whitelist").setExecutor(new WhitelistCommand(this, whitelistListener));
        getCommand("whitelist").setTabCompleter(tabCompleter);

        Bukkit.getScheduler().runTaskTimer(this, tabCompleter::updateCache, 0L, 20L);

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(whitelistListener, this), this);

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
        getLogger().info(getDescription().getName() + " désactivé !");
    }

    public Connection getSqlConnection() {
        return sqlConnection;
    }

    public CheckVersion getCheckVersion() {
        return checkVersion;
    }
    
    public WhitelistListener getWhitelistListener() {
    return whitelistListener;
}

}