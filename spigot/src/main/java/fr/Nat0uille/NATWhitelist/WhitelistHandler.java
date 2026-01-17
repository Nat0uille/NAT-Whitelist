package fr.Nat0uille.NATWhitelist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistHandler {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final DiscordWebhook discordWebhook;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private Component prefix;
    private Component noPermission;

    public WhitelistHandler(Main main, WhitelistManager whitelistManager) {
        this.main = main;
        this.whitelistManager = whitelistManager;

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
        Player onlinePlayer = Bukkit.getPlayer(playerName);
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
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("player-never-joined")
                            .replace("{player}", playerName))
            ));
            return;
        }

        // Vérifier si le joueur est déjà whitelisté
        if (whitelistManager.isWhitelisted(uuid)) {
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("already-on-whitelist")
                            .replace("{player}", finalName))
            ));
            return;
        }

        // Ajouter le joueur à la whitelist
        boolean success = whitelistManager.add(uuid, finalName);

        if (success) {
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("add-success")
                            .replace("{player}", finalName))
            ));

            // Send Discord webhook
            String webhookTitle = main.getLangMessage("add.title");
            String webhookDescription = main.getLangMessage("add.description").replace("{player}", finalName);
            discordWebhook.sendEmbed(webhookTitle, webhookDescription);
        } else {
            sender.sendMessage(prefix.append(
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
        Player onlinePlayer = Bukkit.getPlayer(playerName);
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
            // Sinon, le joueur n'existe pas (ni en ligne, ni premium)
        }

        // Si l'UUID est toujours null, le joueur n'existe pas
        if (uuid == null) {
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("player-never-joined")
                            .replace("{player}", playerName))
            ));
            return;
        }

        whitelistManager.remove(uuid);

        sender.sendMessage(prefix.append(
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
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("list") + main.getLangMessage("list-empty"))
            ));
        } else {
            sender.sendMessage(prefix.append(
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
                Player onlinePlayer = Bukkit.getPlayer(uuid);

                // Si le joueur n'est pas en ligne, le retirer
                if (onlinePlayer == null) {

                    whitelistManager.remove(uuid);
                    removedPlayers.add(playerName);
                }
            }

            // Envoyer le message de résultat
            if (removedPlayers.isEmpty()) {
                sender.sendMessage(prefix.append(
                        mm.deserialize(main.getLangMessage("removeoffline-empty"))
                ));
            } else {
                String removedList = String.join(", ", removedPlayers);
                sender.sendMessage(prefix.append(
                        mm.deserialize(main.getLangMessage("removeoffline-success")
                                .replace("{players}", removedList))
                ));
            }
        } catch (Exception e) {
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("error"))
            ));
            e.printStackTrace();
        }
    }

    public void setWhitelist(boolean enabled, CommandSender sender) {
        boolean ancientEnabled = main.getConfig().getBoolean("enabled");

        if (ancientEnabled == enabled) {
            if (enabled) {
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("already-enabled"))));
            }
            else {
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("already-disabled"))));
            }
        }
        else {
            if (enabled) {
                if (main.getConfig().getBoolean("kick-not-whitelisted-players")) {
                    kickNoWhitelistedPlayers();
                }
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("enabled"))));

                // Send Discord webhook for enable
                String webhookTitle = main.getLangMessage("enable.title");
                String webhookDescription = main.getLangMessage("enable.description");
                discordWebhook.sendEmbed(webhookTitle, webhookDescription);
            }
            else {
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("disabled"))));

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
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (!whitelistManager.isWhitelisted(player.getUniqueId()) && !player.hasPermission("natwhitelist.bypass")) {
                    player.kick(prefix.append(mm.deserialize(main.getLangMessage("kick-message"))));
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
        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("reload"))));
    }

    public void showHelp(CommandSender sender) {
        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("help"))));
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
}
