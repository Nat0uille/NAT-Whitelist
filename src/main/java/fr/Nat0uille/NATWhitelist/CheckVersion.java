package fr.Nat0uille.NATWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class CheckVersion {
    private static final String REMOTE_URL = "https://raw.githubusercontent.com/Nat0uille/NAT-Whitelist/refs/heads/master/VERSION";
    private static final long PERIOD_TICKS = 6 * 60 * 60 * 20L;

    public boolean outdated;
    public String remoteVersion;
    public String localVersion;

    public static void startVersionCheck(Plugin plugin, CheckVersion checkVersion) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    checkVersion.remoteVersion = fetchRemoteVersion();
                    checkVersion.localVersion = fetchLocalVersion(plugin);
                    if (!checkVersion.remoteVersion.equals(checkVersion.localVersion)) {
                        Bukkit.getLogger().warning("[NAT-Whitelist] The plugin is not up to date. Local version: " + checkVersion.localVersion + ", latest version: " + checkVersion.remoteVersion + "\\nPlease download the new version on : modrinth.com/plugin/nat-whitelist");
                        checkVersion.outdated = true;
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[NAT-Whitelist] Error while checking version: " + e.getMessage());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, PERIOD_TICKS);
    }

    public boolean outdated() {
        return outdated;
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }

    public String getLocalVersion() {
        return localVersion;
    }

    private static String fetchRemoteVersion() throws Exception {
        URL url = new URL(REMOTE_URL);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return in.readLine().trim();
        }
    }

    private static String fetchLocalVersion(Plugin plugin) {
        return plugin.getDescription().getVersion();
    }
}