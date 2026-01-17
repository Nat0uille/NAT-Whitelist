package fr.Nat0uille.NATWhitelist.Commands;

import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.WhitelistHandler;
import fr.Nat0uille.NATWhitelist.WhitelistManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

public class WhitelistCommand implements CommandExecutor {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final WhitelistHandler whitelistHandler;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WhitelistCommand(Main main, WhitelistManager whitelistManager, WhitelistHandler whitelistHandler) {
        this.main = main;
        this.whitelistManager = whitelistManager;
        this.whitelistHandler = whitelistHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            whitelistHandler.showHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")) {
            whitelistHandler.setWhitelist(true, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) {
            whitelistHandler.setWhitelist(false, sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {

            if (!sender.hasPermission("natwhitelist.add")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return true;
            }

            if (args.length == 1) {
                sender.sendMessage(whitelistHandler.getPrefix().append(mm.deserialize(main.getLangMessage("add-usage"))));
                return true;
            }

            for (int i = 1; i < args.length; i++) {
                whitelistHandler.addPlayerInWhitelist(sender, args[i]);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {

            if (!sender.hasPermission("natwhitelist.remove")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return true;
            }

            if (args.length == 1) {
                sender.sendMessage(whitelistHandler.getPrefix().append(mm.deserialize(main.getLangMessage("remove-usage"))));
                return true;
            }

            for (int i = 1; i < args.length; i++) {
                whitelistHandler.removePlayerInWhitelist(sender, args[i]);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("natwhitelist.list")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return true;
            }
            whitelistHandler.listWhitelistedPlayersFormatted(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("removeoffline")) {
            if (!sender.hasPermission("natwhitelist.removeoffline")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return true;
            }
            whitelistHandler.removeOfflinePlayerInWhitelist(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("natwhitelist.reload")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return true;
            }
            whitelistHandler.reloadConfig(sender);
            return true;
        }
        return false;
    }
}

