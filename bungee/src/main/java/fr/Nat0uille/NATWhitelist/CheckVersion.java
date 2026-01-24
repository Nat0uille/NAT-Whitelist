package fr.Nat0uille.NATWhitelist;

import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class CheckVersion {
    private static final String REMOTE_URL = "https://raw.githubusercontent.com/Nat0uille/NAT-Whitelist/refs/heads/master/VERSION";
    private static final long PERIOD_SECONDS = 6 * 60 * 60L; // 6 hours in seconds

    public boolean outdated;
    public String remoteVersion;
    public String localVersion;

    public static void startVersionCheck(Main plugin, CheckVersion checkVersion) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            try {
                checkVersion.remoteVersion = fetchRemoteVersion();
                checkVersion.localVersion = fetchLocalVersion(plugin);
                if (!checkVersion.remoteVersion.equals(checkVersion.localVersion)) {
                    plugin.getLogger().warning("[NAT-Whitelist] The plugin is not up to date. Local version: " + checkVersion.localVersion + ", latest version: " + checkVersion.remoteVersion + "\nPlease download the new version on : modrinth.com/plugin/nat-whitelist");
                    checkVersion.outdated = true;
                }
            } catch (Exception e) {
                plugin.getLogger().severe("[NAT-Whitelist] Error while checking version: " + e.getMessage());
            }
        }, 0L, PERIOD_SECONDS, TimeUnit.SECONDS);
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

    private static String fetchLocalVersion(Main plugin) {
        return plugin.getDescription().getVersion();
    }
}
