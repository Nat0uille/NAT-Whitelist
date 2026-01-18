package fr.Nat0uille.NATWhitelist;

import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class CheckVersion {
    private static final String REMOTE_URL = "https://raw.githubusercontent.com/Nat0uille/NAT-Whitelist/refs/heads/master/VERSION";
    private static final long PERIOD_HOURS = 6;

    public boolean outdated;
    public String remoteVersion;
    public String localVersion;

    public static void startVersionCheck(Main plugin, CheckVersion checkVersion, ProxyServer server, Logger logger) {
        server.getScheduler()
            .buildTask(plugin, () -> {
                try {
                    checkVersion.remoteVersion = fetchRemoteVersion();
                    checkVersion.localVersion = plugin.getVersion();
                    if (!checkVersion.remoteVersion.equals(checkVersion.localVersion)) {
                        logger.warn("The plugin is not up to date. Local version: " +
                            checkVersion.localVersion + ", latest version: " + checkVersion.remoteVersion + "\\nPlease download the new version on : modrinth.com/plugin/nat-whitelist");
                        checkVersion.outdated = true;
                    }
                } catch (Exception e) {
                    logger.error("Error while checking version: " + e.getMessage());
                }
            })
            .repeat(PERIOD_HOURS, TimeUnit.HOURS)
            .schedule();
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
}
