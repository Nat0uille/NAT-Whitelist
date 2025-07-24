package fr.Nat0uille.NATWhitelist.Commands;

import fr.Nat0uille.NATWhitelist.Listeners.WhitelistListener;
import fr.Nat0uille.NATWhitelist.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;


public class WhitelistCommand implements CommandExecutor {
    private final Main main;
    private final WhitelistListener whitelistListener;

    public WhitelistCommand(Main main) {
        this.main = main;
        this.whitelistListener = new WhitelistListener(main);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component prefix = mm.deserialize(main.getConfig().getString("prefix"));
        Component noPermission = mm.deserialize(main.getConfig().getString("nopermission"));

        if (!sender.hasPermission("NATWhitelist.use")) {
            sender.sendMessage(prefix.append(noPermission));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(prefix.append(mm.deserialize("<#ffc369><bold>AIDE</bold> <newline>/whitelist add <player> - ᴀᴊᴏᴜᴛᴇʀ ᴜɴ ᴊᴏᴜᴇᴜʀ ᴀ ʟᴀ ᴡʜɪᴛᴇʟɪѕᴛ<newline>/whitelist remove <player> - ʀᴇᴛɪʀᴇʀ ᴜɴ ᴊᴏᴜᴇᴜʀ ᴅᴇ ʟᴀ ᴡʜɪᴛᴇʟɪѕᴛ<newline>/whitelist list - ᴀꜰꜰɪᴄʜᴇʀ ʟᴀ ʟɪѕᴛᴇ ᴅᴇѕ ᴊᴏᴜᴇᴜʀѕ ᴡʜɪᴛᴇʟɪѕᴛᴇѕ")));
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369><bold>LISTE</bold> <newline>" + whitelistListener.listWhitelistedPlayers())));
                return true;
            }
            if (args[0].equalsIgnoreCase("add")) {
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>/whitelist add <player>")));
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>/whitelist remove <player>")));
                return true;
            }
            if (args[0].equalsIgnoreCase("on")) {
                if (whitelistListener.isEnabled()) {
                    sender.sendMessage(prefix.append(mm.deserialize("<#C70000>La whitelist est déjà activée.")));
                    return true;
                }
                else {
                whitelistListener.setEnabled(true);
                whitelistListener.kickNonWhitelistedPlayers(main);
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>La whitelist est maintenant <#63c74d>activée<#ffc369>.")));
                return true;
                }
            }
            if (args[0].equalsIgnoreCase("off")) {
                if (!whitelistListener.isEnabled()) {
                    sender.sendMessage(prefix.append(mm.deserialize("<#C70000>La whitelist est déjà désactivée.")));
                    return true;
                }
                else {
                    whitelistListener.setEnabled(false);
                    sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>La whitelist est maintenant <#C70000>désactivée<#ffc369>.")));
                    return true;
                }
            }
        }
        if (args.length == 2) {
            String playerName = args[1];
            if (playerName.length() > 16) {
                sender.sendMessage(prefix.append(mm.deserialize("<#C70000>Le pseudo ne doit pas dépasser 16 caractères.")));
                return true;
            }
            if (args[0].equalsIgnoreCase("add")) {
                String correctName = WhitelistListener.getCorrectUsernameFromMojang(playerName);
                if (correctName == null) {
                    sender.sendMessage(prefix.append(mm.deserialize("<#C70000>Ce pseudo n'existe pas.")));
                    return true;
                }
                if (whitelistListener.isWhitelisted(correctName)) {
                    sender.sendMessage(prefix.append(mm.deserialize("<#C70000>Ce joueur est déjà dans la whitelist.")));
                    return true;
                }
                boolean success = whitelistListener.add(correctName);
                if (success) {
                    sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>Le joueur <bold>" + correctName + "</bold> a été ajouté à la whitelist.")));
                } else {
                    sender.sendMessage(prefix.append(mm.deserialize("<#C70000>Impossible d'ajouter le joueur.")));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (!whitelistListener.isWhitelisted(playerName)) {
                    sender.sendMessage(prefix.append(mm.deserialize("<#C70000>Ce joueur n'est pas dans la whitelist.")));
                    return true;
                }
                boolean success = whitelistListener.remove(playerName);
                if (success) {
                    whitelistListener.kickNonWhitelistedPlayers(main);
                    sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>Le joueur <bold>" + playerName + "</bold> a été retiré de la whitelist.")));
                } else {
                    sender.sendMessage(prefix.append(mm.deserialize("<#C70000>Impossible de retirer le joueur.")));
                }
                return true;
            }
        }
        return false;
    }
}
