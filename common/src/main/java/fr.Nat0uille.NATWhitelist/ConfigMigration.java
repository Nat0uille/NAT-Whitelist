package fr.Nat0uille.NATWhitelist;

import java.util.function.Function;


public class ConfigMigration {

    public static void migrateToV2(
            Function<String, String> getConfigVersion,
            Function<String, Boolean> containsKey,
            Function<String, Boolean> getBoolean,
            ConfigSetter setConfig,
            Runnable saveConfig) {

        String configVersion = getConfigVersion.apply("1");

        if (configVersion.equals("1") || !containsKey.apply("config-version")) {
            if (containsKey.apply("kicknowhitelisted")) {
                boolean oldValue = getBoolean.apply("kicknowhitelisted");

                setConfig.set("kick-not-whitelisted-players", oldValue);

                setConfig.set("kicknowhitelisted", null);
            }

            setConfig.set("config-version", "2.0");

            saveConfig.run();
        }
    }

    @FunctionalInterface
    public interface ConfigSetter {
        void set(String key, Object value);
    }
}
