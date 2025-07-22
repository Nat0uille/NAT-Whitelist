package fr.Nat0uille.NATWhitelist;

 import fr.Nat0uille.NATWhitelist.Commands.*;
 import fr.Nat0uille.NATWhitelist.TabCompleter.*;
 import fr.Nat0uille.NATWhitelist.Listeners.WhitelistListener;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private WhitelistListener whitelistListener;
    private WhitelistTabCompleter tabCompleter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        whitelistListener = new WhitelistListener(this);
        tabCompleter = new WhitelistTabCompleter(whitelistListener);

        getCommand("whitelist").setExecutor(new WhitelistCommand(this));
        getCommand("whitelist").setTabCompleter(tabCompleter);
        getCommand("wl").setExecutor(new WhitelistCommand(this));
        getCommand("wl").setTabCompleter(tabCompleter);

        Bukkit.getScheduler().runTaskTimer(this, tabCompleter::updateCache, 0L, 20L);
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " désactivé !");
    }
}