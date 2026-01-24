package fr.Nat0uille.NATWhitelist;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistHandler {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final DiscordWebhook discordWebhook;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final BungeeAudiences audiences;
    private Component prefix;
    private Component noPermission;

    public WhitelistHandler(Main main, WhitelistManager whitelistManager) {
        this.main = main;
        this.whitelistManager = whitelistManager;
        this.audiences = BungeeAudiences.create(main);

        // Initialize Discord webhook
        String webhookUrl = main.getConfig().getString("discord-webhook-url", "none");
        this.discordWebhook = new DiscordWebhook(webhookUrl);

        // Component messages
        this.prefix = mm.deserialize(main.getLangMessage("prefix"));
        this.noPermission = mm.deserialize(main.getLangMessage("no-permission"));
    }

    public void addPlayerInWhitelist(CommandSender sender, String playerName) {
        UUID uuid = null;
        String finalName = playerName;

        // 1. Vérifier si le joueur est connecté
        ProxiedPlayer onlinePlayer = ProxyServer.getInstance().getPlayer(playerName);
        if (onlinePlayer != null) {
            // Joueur premium ou cracké connecté
            finalName = onlinePlayer.getName();
            uuid = onlinePlayer.getUniqueId();
        } else {
            // 2. Joueur non connecté - vérifier si premium via Mojang API
            String correctNameFromMojang = MojangAPIManager.getCorrectUsernameFromMojang(playerName);

            if (correctNameFromMojang != null) {
                // Joueur premium non connecté
                finalName = correctNameFromMojang;
                uuid = MojangAPIManager.getUUIDFromUsername(finalName);
            }
        }

        // Si l'UUID est toujours null, le joueur n'existe pas
        if (uuid == null) {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("player-never-joined")
                            .replace("{player}", playerName))
            ));
            return;
        }

        // Vérifier si le joueur est déjà whitelisté
        if (whitelistManager.isWhitelisted(uuid)) {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("already-on-whitelist")
                            .replace("{player}", finalName))
            ));
            return;
        }

        // Ajouter le joueur à la whitelist
        boolean success = whitelistManager.add(uuid, finalName);

        if (success) {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("add-success")
                            .replace("{player}", finalName))
            ));

            // Send Discord webhook
            String webhookTitle = main.getLangMessage("add.title");
            String webhookDescription = main.getLangMessage("add.description").replace("{player}", finalName);
            discordWebhook.sendEmbed(webhookTitle, webhookDescription);
        } else {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("add-error")
                            .replace("{player}", finalName))
            ));
        }
    }

    public void removePlayerInWhitelist(CommandSender sender, String playerName) {
        UUID uuid = null;
        String finalName = playerName;

        kickNoWhitelistedPlayers();

        // 1. Vérifier si le joueur est connecté
        ProxiedPlayer onlinePlayer = ProxyServer.getInstance().getPlayer(playerName);
        if (onlinePlayer != null) {
            // Joueur premium ou cracké connecté
            finalName = onlinePlayer.getName();
            uuid = onlinePlayer.getUniqueId();
        } else {
            // 2. Joueur non connecté - vérifier si premium via Mojang API
            String correctNameFromMojang = MojangAPIManager.getCorrectUsernameFromMojang(playerName);

            if (correctNameFromMojang != null) {
                // Joueur premium non connecté
                finalName = correctNameFromMojang;
                uuid = MojangAPIManager.getUUIDFromUsername(finalName);
            }
        }

        // Si l'UUID est toujours null, le joueur n'existe pas
        if (uuid == null) {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("player-never-joined")
                            .replace("{player}", playerName))
            ));
            return;
        }

        whitelistManager.remove(uuid);

        audiences.sender(sender).sendMessage(prefix.append(
                mm.deserialize(main.getLangMessage("remove-success")
                        .replace("{player}", finalName))
        ));

        // Send Discord webhook
        String webhookTitle = main.getLangMessage("remove.title");
        String webhookDescription = main.getLangMessage("remove.description").replace("{player}", finalName);
        discordWebhook.sendEmbed(webhookTitle, webhookDescription);
    }

    public void listWhitelistedPlayersFormatted(CommandSender sender) {
        String formattedList = whitelistManager.getFormattedList();

        if (formattedList.isEmpty()) {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("list") + main.getLangMessage("list-empty"))
            ));
        } else {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("list") + formattedList)
            ));
        }
    }

    public void removeOfflinePlayerInWhitelist(CommandSender sender) {
        try {
            Map<UUID, String> whitelistedPlayers = whitelistManager.listWithNames();
            List<String> removedPlayers = new ArrayList<>();

            for (Map.Entry<UUID, String> entry : whitelistedPlayers.entrySet()) {
                UUID uuid = entry.getKey();
                String playerName = entry.getValue();
                ProxiedPlayer onlinePlayer = ProxyServer.getInstance().getPlayer(uuid);

                // Si le joueur n'est pas en ligne, le retirer
                if (onlinePlayer == null) {
                    whitelistManager.remove(uuid);
                    removedPlayers.add(playerName);
                }
            }

            // Envoyer le message de résultat
            if (removedPlayers.isEmpty()) {
                audiences.sender(sender).sendMessage(prefix.append(
                        mm.deserialize(main.getLangMessage("removeoffline-empty"))
                ));
            } else {
                String removedList = String.join(", ", removedPlayers);
                audiences.sender(sender).sendMessage(prefix.append(
                        mm.deserialize(main.getLangMessage("removeoffline-success")
                                .replace("{players}", removedList))
                ));
            }
        } catch (Exception e) {
            audiences.sender(sender).sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("error"))
            ));
            e.printStackTrace();
        }
    }

    public void setWhitelist(boolean enabled, CommandSender sender) {
        boolean ancientEnabled = main.getConfig().getBoolean("enabled");

        if (ancientEnabled == enabled) {
            if (enabled) {
                audiences.sender(sender).sendMessage(prefix.append(mm.deserialize(main.getLangMessage("already-enabled"))));
            } else {
                audiences.sender(sender).sendMessage(prefix.append(mm.deserialize(main.getLangMessage("already-disabled"))));
            }
        } else {
            if (enabled) {
                if (main.getConfig().getBoolean("kick-not-whitelisted-players")) {
                    kickNoWhitelistedPlayers();
                }
                audiences.sender(sender).sendMessage(prefix.append(mm.deserialize(main.getLangMessage("enabled"))));

                // Send Discord webhook for enable
                String webhookTitle = main.getLangMessage("enable.title");
                String webhookDescription = main.getLangMessage("enable.description");
                discordWebhook.sendEmbed(webhookTitle, webhookDescription);
            } else {
                audiences.sender(sender).sendMessage(prefix.append(mm.deserialize(main.getLangMessage("disabled"))));

                // Send Discord webhook for disable
                String webhookTitle = main.getLangMessage("disable.title");
                String webhookDescription = main.getLangMessage("disable.description");
                discordWebhook.sendEmbed(webhookTitle, webhookDescription);
            }

            main.getConfig().set("enabled", enabled);
            main.saveConfig();
        }
    }

    public void kickNoWhitelistedPlayers() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            try {
                if (!whitelistManager.isWhitelisted(player.getUniqueId()) && !player.hasPermission("natwhitelist.bypass")) {
                    player.disconnect(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                                    main.getLangMessage("kick-message"))
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadConfig(CommandSender sender) {
        main.reloadConfig();
        main.loadLang();
        prefix = mm.deserialize(main.getLangMessage("prefix"));
        noPermission = mm.deserialize(main.getLangMessage("no-permission"));
        audiences.sender(sender).sendMessage(prefix.append(mm.deserialize(main.getLangMessage("reload"))));
    }

    public void showHelp(CommandSender sender) {
        audiences.sender(sender).sendMessage(prefix.append(mm.deserialize(main.getLangMessage("help"))));
    }

    public Component getPrefix() {
        return prefix;
    }

    public Component getNoPermission() {
        return noPermission;
    }

    public boolean isEnabled() {
        return main.getConfig().getBoolean("enabled");
    }

    public boolean isWhitelisted(UUID uuid) {
        return whitelistManager.isWhitelisted(uuid);
    }

    public void addAllOnlinePlayersToWhitelist(CommandSender sender) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            whitelistManager.add(player.getUniqueId(), player.getName());
            audiences.sender(sender).sendMessage(prefix.append(mm.deserialize(main.getLangMessage("add-success")
                    .replace("{player}", player.getName()))));
        }
    }
}
