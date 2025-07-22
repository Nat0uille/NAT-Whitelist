package fr.Nat0uille.NATWhitelist;

import fr.Nat0uille.NATWhitelist.Commands.*;
import fr.Nat0uille.NATWhitelist.TabCompleter.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info(getDescription().getName() + " Par " + String.join(", ", getDescription().getAuthors()));
        getLogger().info("Version: " + getDescription().getVersion());

        getCommand("whitelist").setExecutor(new WhitelistCommand(this));
        getCommand("whitelist").setTabCompleter(new WhitelistTabCompleter());
        getCommand("wl").setExecutor(new WhitelistCommand(this));
        getCommand("wl").setTabCompleter(new WhitelistTabCompleter());
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " désactivé !");
    }


}
