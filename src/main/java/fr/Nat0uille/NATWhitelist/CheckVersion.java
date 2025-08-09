package fr.Nat0uille.NATWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class CheckVersion {
    private static final String REMOTE_URL = "https://raw.githubusercontent.com/Nat0uille/NAT-Whitelist/refs/heads/master/VERSION";
    private static final String BUILD_GRADLE_PATH = "build.gradle";
    private static final long PERIOD_TICKS = 3 * 60 * 60 * 20L;
    public static boolean isUpdateAvailable = false;

    public static void startVersionCheck(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String remoteVersion = fetchRemoteVersion();
                    String localVersion = fetchLocalVersion();
                    if (!remoteVersion.equals(localVersion)) {
                        Bukkit.getLogger().warning("[NATWhitelist] Le plugin n'est pas à jour. Version locale: " + localVersion + " | Dernière version: " + remoteVersion);
                        isUpdateAvailable = true;
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[NATWhitelist] Erreur lors de la vérification de la version : " + e.getMessage());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, PERIOD_TICKS);
    }

    private static String fetchRemoteVersion() throws IOException {
        URL url = new URL(REMOTE_URL);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return in.readLine().trim();
        }
    }

    private static String fetchLocalVersion() throws IOException {
        for (String line : Files.readAllLines(Paths.get(BUILD_GRADLE_PATH))) {
            if (line.trim().startsWith("version")) {
                return line.split("'")[1].trim();
            }
        }
        return "";
    }
}