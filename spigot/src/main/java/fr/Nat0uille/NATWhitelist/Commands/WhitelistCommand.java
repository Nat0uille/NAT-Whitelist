package fr.Nat0uille.NATWhitelist.Commands;

import fr.Nat0uille.NATWhitelist.MojangAPIManager;
import fr.Nat0uille.NATWhitelist.Whitelist;
import fr.Nat0uille.NATWhitelist.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WhitelistCommand implements CommandExecutor {

    private final Main main;
    private final Whitelist whitelist;

    MiniMessage mm = MiniMessage.miniMessage();
    private Component prefix;
    private Component noPermission;


    public WhitelistCommand(Main main, Whitelist whitelist) {
        this.main = main;
        this.whitelist = whitelist;

        // Component messages
        this.prefix = mm.deserialize(main.getLangMessage("prefix"));
        this.noPermission = mm.deserialize(main.getLangMessage("no-permission"));
    }

    private void addPlayerInWhitelist(CommandSender sender, String playerName) {

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
        if (main.getWhitelistManager().isWhitelisted(uuid)) {
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("already-on-whitelist")
                            .replace("{player}", finalName))
            ));
            return;
        }

        // Ajouter le joueur à la whitelist
        boolean success = main.getWhitelistManager().add(uuid, finalName);

        if (success) {
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("add-success")
                            .replace("{player}", finalName))
            ));
        } else {
            sender.sendMessage(prefix.append(
                    mm.deserialize(main.getLangMessage("add-error")
                            .replace("{player}", finalName))
            ));
        }

    }

    private void removePlayerInWhitelist(CommandSender sender, String playerName) {

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

        main.getWhitelistManager().remove(uuid);

        sender.sendMessage(prefix.append(
                mm.deserialize(main.getLangMessage("remove-success")
                        .replace("{player}", finalName))
        ));
    }

    private void listWhitelistedPlayersFormatted(CommandSender sender) {
        String formattedList = main.getWhitelistManager().getFormattedList();

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

    private void removeOfflinePlayerInWhitelist(CommandSender sender) {
        try {
            List<UUID> whitelistedUUIDs = main.getWhitelistManager().list();
            List<String> removedPlayers = new ArrayList<>();

            for (UUID uuid : whitelistedUUIDs) {
                Player onlinePlayer = Bukkit.getPlayer(uuid);

                // Si le joueur n'est pas en ligne, le retirer
                if (onlinePlayer == null) {
                    String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                    if (playerName == null) {
                        playerName = uuid.toString();
                    }

                    main.getWhitelistManager().remove(uuid);
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
            }
            else {
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("disabled"))));
            }

            main.getConfig().set("enabled", enabled);
            main.saveConfig();

        }
    }

    public void kickNoWhitelistedPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (!main.getWhitelistManager().isWhitelisted(player.getUniqueId()) && !player.hasPermission("natwhitelist.bypass")) {
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
        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("reload"))));
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("help"))));
            return true;
        }

        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")) {
            setWhitelist(true, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) {
            setWhitelist(false, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {

            if (!sender.hasPermission("natwhitelist.add")) {
                sender.sendMessage(prefix.append(noPermission));
                return true;
            }

            if (args.length == 1) {
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("add-usage"))));
                return true;
            }

            for (int i = 1; i < args.length; i++) {
                addPlayerInWhitelist(sender, args[i]);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {

            if (!sender.hasPermission("natwhitelist.remove")) {
                sender.sendMessage(prefix.append(noPermission));
                return true;
            }

            if (args.length == 1) {
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("remove-usage"))));
                return true;
            }

            for (int i = 1; i < args.length; i++) {
                removePlayerInWhitelist(sender, args[i]);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("natwhitelist.list")) {
                sender.sendMessage(prefix.append(noPermission));
                return true;
            }
            listWhitelistedPlayersFormatted(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("removeoffline")) {
            if (!sender.hasPermission("natwhitelist.removeoffline")) {
                sender.sendMessage(prefix.append(noPermission));
                return true;
            }
            removeOfflinePlayerInWhitelist(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("natwhitelist.reload")) {
                sender.sendMessage(prefix.append(noPermission));
                return true;
            }
            reloadConfig(sender);
            return true;
        }
        return false;
    }
}

