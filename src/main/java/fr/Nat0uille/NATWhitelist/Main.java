package fr.Nat0uille.NATWhitelist;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info(getDescription().getName() + " Par " + String.join(", ", getDescription().getAuthors()));
        getLogger().info("Version: " + getDescription().getVersion());

    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " désactivé !");
    }
}
