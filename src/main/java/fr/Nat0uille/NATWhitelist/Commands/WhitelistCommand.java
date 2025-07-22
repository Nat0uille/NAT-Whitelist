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
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369><bold>LISTE</bold> <newline> ajout fonction ici")));
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
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                boolean success = whitelistListener.add(args[1]);
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>Le joueur <bold>" + args[1] + "</bold> a été ajouté à la whitelist.")));
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                boolean success = whitelistListener.remove(args[1]);
                sender.sendMessage(prefix.append(mm.deserialize("<#ffc369>Le joueur <bold>" + args[1] + "</bold> a été retiré de la whitelist.")));
                return true;
            }
        }
        return false;
    }
}
