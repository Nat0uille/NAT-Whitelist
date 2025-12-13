package fr.Nat0uille.NATWhitelist.Commands;

import fr.Nat0uille.NATWhitelist.Whitelist;
import fr.Nat0uille.NATWhitelist.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class WhitelistCommand implements CommandExecutor {
    private final Main main;
    private final Whitelist whitelist;

    public WhitelistCommand(Main main, Whitelist whitelist) {
        this.main = main;
        this.whitelist = whitelist;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component prefix = mm.deserialize(main.getLangMessage("prefix"));
        Component noPermission = mm.deserialize(main.getLangMessage("nopermission"));

        if (args.length == 0) {
            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("help"))));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("natwhitelist.list")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                try {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("list") + whitelist.listWhitelistedPlayers())));
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (!sender.hasPermission("natwhitelist.add")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>/whitelist add <player>")));
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("natwhitelist.remove")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>/whitelist remove <player>")));
                return true;
            }
            if (args[0].equalsIgnoreCase("on")) {
                if (!sender.hasPermission("natwhitelist.on")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                if (whitelist.isEnabled()) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("whitelistalreadyon"))));
                } else {
                    if (main.getConfig().getBoolean("kicknowhitelisted")) {
                        whitelist.kickNoWhitelistedPlayers(main);
                    }
                    whitelist.setEnabled(true);
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("whiteliston"))));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("off")) {
                if (!sender.hasPermission("natwhitelist.off")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                if (!whitelist.isEnabled()) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("whitelistalreadyoff"))));
                } else {
                    whitelist.setEnabled(false);
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("whitelistoff"))));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("natwhitelist.reload")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                main.reloadConfig();
                main.loadLang();
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("reload"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("removeoffilne")) {
                if (!sender.hasPermission("natwhitelist.removeoffline")) {
                    sender.sendMessage(prefix.append(noPermission));
                }
                try {
                     whitelist.removeNoWhitelistedPlayers(main);
                    List<String> removed = whitelist.getRemovedPlayers();
                    String removedList = removed.isEmpty() ? "No players retired." : String.join(", ", removed);
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("removeoffline").replace("{players}", removedList))));
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
        }

        if (args.length == 2) {
            String playerName = args[1];
            if (playerName.length() > 16) {
                sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("playertoolong"))));
                return true;
            }
            UUID uuid;
            String correctName = Whitelist.getCorrectUsernameFromMojang(playerName);
            if (correctName != null) {
                playerName = correctName;
                uuid = Bukkit.getOfflinePlayer(correctName).getUniqueId();
            } else {
                Player onlinePlayer = Bukkit.getPlayer(playerName);
                if (onlinePlayer != null) {
                    uuid = onlinePlayer.getUniqueId();
                    playerName = onlinePlayer.getName();
                } else {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("crackedneverconnected").replace("{player}", playerName))));
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (!sender.hasPermission("natwhitelist.add")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                try {
                    if (whitelist.isWhitelisted(uuid)) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("alreadyinwhitelist").replace("{player}", playerName))));
                        return true;
                    }
                    boolean success = whitelist.add(uuid);
                    if (success) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("addinwhitelist").replace("{player}", playerName))));
                    } else {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("erroraddingwhitelist").replace("{player}", playerName))));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("natwhitelist.remove")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                try {
                    if (!whitelist.isWhitelisted(uuid)) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("notinwhitelist").replace("{player}", playerName))));
                        return true;
                    }
                    boolean success = whitelist.remove(uuid);
                    if (success) {
                        whitelist.kickNoWhitelistedPlayers(main);
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("removeinwhoitelist").replace("{player}", playerName))));
                    } else {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("errorremovingwhitelist").replace("{player}", playerName))));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
        }

        if (args.length >= 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            for (int i = 1; i < args.length; i++) {
                String playerName = args[i];
                if (playerName.length() > 16) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("playertoolong"))));
                    continue;
                }
                UUID uuid;
                String correctName = Whitelist.getCorrectUsernameFromMojang(playerName);
                if (correctName != null) {
                    playerName = correctName;
                    uuid = Bukkit.getOfflinePlayer(correctName).getUniqueId();
                } else {
                    Player onlinePlayer = Bukkit.getPlayer(playerName);
                    if (onlinePlayer != null) {
                        uuid = onlinePlayer.getUniqueId();
                        playerName = onlinePlayer.getName();
                    } else {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("crackedneverconnected").replace("{player}", playerName))));
                        continue;
                    }
                }
                if (args[0].equalsIgnoreCase("add")) {
                    if (!sender.hasPermission("natwhitelist.add")) {
                        sender.sendMessage(prefix.append(noPermission));
                        continue;
                    }
                    try {
                        if (whitelist.isWhitelisted(uuid)) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("alreadyinwhitelist").replace("{player}", playerName))));
                            continue;
                        }
                        boolean success = whitelist.add(uuid);
                        if (success) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("addinwhitelist").replace("{player}", playerName))));
                        } else {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("erroraddingwhitelist").replace("{player}", playerName))));
                        }
                    } catch (SQLException e) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("sqlerror"))));
                        e.printStackTrace();
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!sender.hasPermission("natwhitelist.remove")) {
                        sender.sendMessage(prefix.append(noPermission));
                        continue;
                    }
                    try {
                        if (!whitelist.isWhitelisted(uuid)) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("notinwhitelist").replace("{player}", playerName))));
                            continue;
                        }
                        boolean success = whitelist.remove(uuid);
                        if (success) {
                            whitelist.kickNoWhitelistedPlayers(main);
                            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("removeinwhoitelist").replace("{player}", playerName))));
                        } else {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("errorremovingwhitelist").replace("{player}", playerName))));
                        }
                    } catch (SQLException e) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("sqlerror"))));
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }

        return false;
    }
}