package fr.Nat0uille.NATWhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.ConfigurationNode;

@Plugin(
        id = "natwhitelist",
        name = "NAT-Whitelist",
        version = "${version}",
        authors = {"Nat0uille"}
)
public class Main {

    private final ProxyServer server;
    private final PluginContainer pluginContainer;
    private ConfigurationNode config;
    private final Path configPath;

    @Inject
    public Main(ProxyServer server, PluginContainer pluginContainer, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.pluginContainer = pluginContainer;
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.configPath = dataDirectory.resolve("config.yml");
        loadConfig();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        server.getConsoleCommandSource().sendMessage(Component.empty());
        server.getConsoleCommandSource().sendMessage(
                Component.text("          ___   NAT-Whitelist", NamedTextColor.RED)
        );
        server.getConsoleCommandSource().sendMessage(
                Component.text("|\\ |  /\\   |    ", NamedTextColor.RED)
                        .append(Component.text("Made by ", NamedTextColor.DARK_RED))
                        .append(Component.text("Nat0uille", NamedTextColor.RED))
        );
        server.getConsoleCommandSource().sendMessage(
                Component.text("| \\| /~~\\  |    ", NamedTextColor.RED)
                        .append(Component.text("Version ", NamedTextColor.DARK_RED))
                        .append(Component.text(getVersion(), NamedTextColor.RED))
        );
        server.getConsoleCommandSource().sendMessage(Component.empty());
    }

    public ConfigurationNode getConfig() {
        return config;
    }

    public String getVersion() {
        return pluginContainer.getDescription().getVersion().orElse("Unknown");
    }

    public void reloadConfig() {
        loadConfig();
    }

    private YamlConfigurationLoader createLoader() {
        return YamlConfigurationLoader.builder()
                .path(configPath)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .build();
    }

    private void loadConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            YamlConfigurationLoader loader = createLoader();
            if (!Files.exists(configPath)) {
                try (var in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configPath);
                    } else {
                        config = loader.createNode();
                        loader.save(config);
                    }
                }
                config = loader.load();
            } else {
                config = loader.load();
            }
        } catch (IOException e) {
            e.printStackTrace();
            config = null;
        }
    }
}