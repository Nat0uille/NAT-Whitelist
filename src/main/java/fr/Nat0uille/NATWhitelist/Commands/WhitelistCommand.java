package fr.Nat0uille.NATWhitelist.Commands;

import fr.Nat0uille.NATWhitelist.Listeners.WhitelistListener;
import fr.Nat0uille.NATWhitelist.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

import java.sql.SQLException;

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
                try {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("list") + whitelistListener.listWhitelistedPlayers())));
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (!sender.hasPermission("NATWhitelist.add")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>/whitelist add <player>")));
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("NATWhitelist.remove")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>/whitelist remove <player>")));
                return true;
            }
            if (args[0].equalsIgnoreCase("on")) {
                if (!sender.hasPermission("NATWhitelist.on")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                if (whitelistListener.isEnabled()) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("whitelistalreadyon"))));
                    return true;
                } else {

                    whitelistListener.setEnabled(true);
                    try {
                        whitelistListener.kickNonWhitelistedPlayers(main);
                    } catch (SQLException e) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                        e.printStackTrace();
                    }
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("whiteliston"))));
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("off")) {
                if (!sender.hasPermission("NATWhitelist.off")) {
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
                if (!sender.hasPermission("NATWhitelist.reload")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                main.reloadConfig();
                sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("reload"))));
                return true;
            }
        }

        if (args.length == 2) {
            String playerName = args[1];
            if (playerName.length() > 16) {
                sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("playertoolong"))));
                return true;
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (!sender.hasPermission("NATWhitelist.add")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                String correctName = WhitelistListener.getCorrectUsernameFromMojang(playerName);
                if (correctName == null) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("playernotfound"))));
                    return true;
                }
                try {
                    if (whitelistListener.isWhitelisted(correctName)) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("alreadyinwhitelist").replace("{player}", correctName))));
                        return true;
                    }
                    boolean success = whitelistListener.add(correctName);
                    if (success) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("whitelistadd").replace("{player}", correctName))));
                    } else {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("erroraddingwhitelist").replace("{player}", correctName))));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                    e.printStackTrace();
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("NATWhitelist.remove")) {
                    sender.sendMessage(prefix.append(noPermission));
                    return true;
                }
                try {
                    if (!whitelistListener.isWhitelisted(playerName)) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("notinwhitelist").replace("{player}", playerName))));
                        return true;
                    }
                    boolean success = whitelistListener.remove(playerName);
                    if (success) {
                        whitelistListener.kickNonWhitelistedPlayers(main);
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
                if (args[0].equalsIgnoreCase("add")) {
                    if (!sender.hasPermission("NATWhitelist.add")) {
                        sender.sendMessage(prefix.append(noPermission));
                        continue;
                    }
                    String correctName = WhitelistListener.getCorrectUsernameFromMojang(playerName);
                    if (correctName == null) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("playernotfound"))));
                        continue;
                    }
                    try {
                        if (whitelistListener.isWhitelisted(correctName)) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("alreadyinwhitelist").replace("{player}", correctName))));
                            continue;
                        }
                        boolean success = whitelistListener.add(correctName);
                        if (success) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("addinwhitelist").replace("{player}", correctName))));
                        } else {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("erroraddingwhitelist").replace("{player}", correctName))));
                        }
                    } catch (SQLException e) {
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("sqlerror"))));
                        e.printStackTrace();
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!sender.hasPermission("NATWhitelist.remove")) {
                        sender.sendMessage(prefix.append(noPermission));
                        continue;
                    }
                    try {
                        if (!whitelistListener.isWhitelisted(playerName)) {
                            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("notinwhitelist").replace("{player}", playerName))));
                            continue;
                        }
                        boolean success = whitelistListener.remove(playerName);
                        if (success) {
                            whitelistListener.kickNonWhitelistedPlayers(main);
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