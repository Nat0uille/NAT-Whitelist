package fr.Nat0uille.NATWhitelist.Commands;

import fr.Nat0uille.NATWhitelist.Listeners.WhitelistListener;
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
    private final WhitelistListener whitelistListener;

    public WhitelistCommand(Main main, WhitelistListener whitelistListener) {
        this.main = main;
        this.whitelistListener = whitelistListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component prefix = mm.deserialize(main.getConfig().getString("prefix"));
        Component noPermission = mm.deserialize(main.getConfig().getString("nopermission"));

        if (args.length == 0) {
            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("help"))));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("natwhitelist.list")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                try {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("list") + whitelistListener.listWhitelistedPlayers())));
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
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
                if (whitelistListener.isEnabled()) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("whitelistalreadyon"))));
                    return true;
                } else {
                    if (main.getConfig().getBoolean("kicknowhitelisted")) {
                        whitelistListener.kickNoWhitelistedPlayers(main);
                    }
                    whitelistListener.setEnabled(true);
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("whiteliston"))));
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("off")) {
                if (!sender.hasPermission("natwhitelist.off")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                if (!whitelistListener.isEnabled()) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("whitelistalreadyoff"))));
                    return true;
                } else {
                    whitelistListener.setEnabled(false);
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("whitelistoff"))));
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("natwhitelist.reload")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                main.reloadConfig();
                sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("reload"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("removeoffilne")) {
                if (!sender.hasPermission("natwhitelist.removeoffline")) {
                    sender.sendMessage(prefix.append(noPermission));
                }
                try {
                     whitelistListener.removeNoWhitelistedPlayers(main);
                    List<String> removed = whitelistListener.getRemovedPlayers();
                    String removedList = removed.isEmpty() ? "No players retired." : String.join(", ", removed);
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("removeoffline").replace("{players}", removedList))));
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
        }

        if (args.length == 2) {
            String playerName = args[1];
            if (playerName.length() > 16) {
                sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("playertoolong"))));
                return true;
            }
            UUID uuid = null;
            String correctName = WhitelistListener.getCorrectUsernameFromMojang(playerName);
            if (correctName != null) {
                playerName = correctName;
                uuid = Bukkit.getOfflinePlayer(correctName).getUniqueId();
            } else {
                Player onlinePlayer = Bukkit.getPlayer(playerName);
                if (onlinePlayer != null) {
                    uuid = onlinePlayer.getUniqueId();
                    playerName = onlinePlayer.getName();
                } else {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("crackedneverconnected").replace("{player}", playerName))));
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (!sender.hasPermission("natwhitelist.add")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                try {
                    if (whitelistListener.isWhitelisted(uuid)) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("alreadyinwhitelist").replace("{player}", playerName))));
                        return true;
                    }
                    boolean success = whitelistListener.add(uuid, playerName);
                    if (success) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("addinwhitelist").replace("{player}", playerName))));
                    } else {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("erroraddingwhitelist").replace("{player}", playerName))));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
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
                    if (!whitelistListener.isWhitelisted(uuid)) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("notinwhitelist").replace("{player}", playerName))));
                        return true;
                    }
                    boolean success = whitelistListener.remove(uuid);
                    if (success) {
                        whitelistListener.kickNoWhitelistedPlayers(main);
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("removeinwhoitelist").replace("{player}", playerName))));
                    } else {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("errorremovingwhitelist").replace("{player}", playerName))));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
        }

        if (args.length >= 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            for (int i = 1; i < args.length; i++) {
                String playerName = args[i];
                if (playerName.length() > 16) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("playertoolong"))));
                    continue;
                }
                UUID uuid = null;
                String correctName = WhitelistListener.getCorrectUsernameFromMojang(playerName);
                if (correctName != null) {
                    playerName = correctName;
                    uuid = Bukkit.getOfflinePlayer(correctName).getUniqueId();
                } else {
                    Player onlinePlayer = Bukkit.getPlayer(playerName);
                    if (onlinePlayer != null) {
                        uuid = onlinePlayer.getUniqueId();
                        playerName = onlinePlayer.getName();
                    } else {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("crackedneverconnected").replace("{player}", playerName))));
                        continue;
                    }
                }
                if (args[0].equalsIgnoreCase("add")) {
                    if (!sender.hasPermission("natwhitelist.add")) {
                        sender.sendMessage(prefix.append(noPermission));
                        continue;
                    }
                    try {
                        if (whitelistListener.isWhitelisted(uuid)) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("alreadyinwhitelist").replace("{player}", playerName))));
                            continue;
                        }
                        boolean success = whitelistListener.add(uuid, playerName);
                        if (success) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("addinwhitelist").replace("{player}", playerName))));
                        } else {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("erroraddingwhitelist").replace("{player}", playerName))));
                        }
                    } catch (SQLException e) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                        e.printStackTrace();
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!sender.hasPermission("natwhitelist.remove")) {
                        sender.sendMessage(prefix.append(noPermission));
                        continue;
                    }
                    try {
                        if (!whitelistListener.isWhitelisted(uuid)) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("notinwhitelist").replace("{player}", playerName))));
                            continue;
                        }
                        boolean success = whitelistListener.remove(uuid);
                        if (success) {
                            whitelistListener.kickNoWhitelistedPlayers(main);
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("removeinwhoitelist").replace("{player}", playerName))));
                        } else {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("errorremovingwhitelist").replace("{player}", playerName))));
                        }
                    } catch (SQLException e) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }

        return false;
    }
}