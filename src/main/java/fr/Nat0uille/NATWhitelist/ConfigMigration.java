package fr.Nat0uille.NATWhitelist;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ConfigMigration {

    // Keys renamed across old versions, applied before the generic merge so the value under
    // the old key isn't silently dropped just because the new template no longer has it.
    private static void applyLegacyRenames(YamlConfiguration currentConfig) {
        if (currentConfig.contains("kicknowhitelisted") && !currentConfig.contains("kick-not-whitelisted-players")) {
            currentConfig.set("kick-not-whitelisted-players", currentConfig.getBoolean("kicknowhitelisted"));
        }
    }

    /**
     * Rebuilds config.yml from the version bundled in the plugin jar (fresh structure, comments,
     * and any new setting), while carrying over every value the user already had configured.
     *
     * @return true if a setting present in the bundled template was missing from the user's config
     * (i.e. this update introduced a new setting).
     */
    public static boolean mergeWithLatestDefaults(JavaPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
        applyLegacyRenames(currentConfig);

        return mergeResourceWithLatestDefaults(plugin, "config.yml", configFile, currentConfig, Set.of("config-version"));
    }

    /**
     * Rebuilds a languages/{lang}.yml file from the version bundled in the plugin jar (fresh
     * structure and any new message), while carrying over every message the user already customized.
     *
     * @return true if a message present in the bundled template was missing from the user's file
     * (i.e. this update introduced a new message).
     */
    public static boolean mergeLangWithLatestDefaults(JavaPlugin plugin, String lang) {
        String resourcePath = "languages/" + lang + ".yml";
        File langFile = new File(plugin.getDataFolder(), resourcePath);
        YamlConfiguration currentLang = YamlConfiguration.loadConfiguration(langFile);

        return mergeResourceWithLatestDefaults(plugin, resourcePath, langFile, currentLang, Set.of());
    }

    private static boolean mergeResourceWithLatestDefaults(JavaPlugin plugin, String resourcePath, File targetFile,
                                                             YamlConfiguration currentConfig, Set<String> excludedKeys) {
        YamlConfiguration latestTemplate;
        try (InputStream defaultStream = plugin.getResource(resourcePath)) {
            if (defaultStream == null) {
                plugin.getLogger().severe("Unable to find the bundled " + resourcePath + ", skipping migration!");
                return false;
            }
            latestTemplate = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to read the bundled " + resourcePath + ", skipping migration!");
            return false;
        }

        boolean addedNewKey = false;
        for (String key : latestTemplate.getKeys(true)) {
            if (latestTemplate.isConfigurationSection(key) || excludedKeys.contains(key)) {
                continue;
            }

            if (currentConfig.contains(key)) {
                latestTemplate.set(key, currentConfig.get(key));
            } else {
                addedNewKey = true;
            }
        }

        try {
            latestTemplate.save(targetFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save the migrated " + resourcePath + "!");
            return false;
        }

        return addedNewKey;
    }
}
