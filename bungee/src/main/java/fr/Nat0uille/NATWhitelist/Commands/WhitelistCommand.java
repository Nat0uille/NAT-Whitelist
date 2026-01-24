package fr.Nat0uille.NATWhitelist.Commands;

import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.WhitelistHandler;
import fr.Nat0uille.NATWhitelist.WhitelistManager;
import fr.Nat0uille.NATWhitelist.WhitelistTabCompleter;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class WhitelistCommand extends Command implements TabExecutor {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final WhitelistHandler whitelistHandler;
    private final WhitelistTabCompleter tabCompleter;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final BungeeAudiences audiences;

    public WhitelistCommand(Main main, WhitelistManager whitelistManager, WhitelistHandler whitelistHandler,
                          WhitelistTabCompleter tabCompleter, String name, String... aliases) {
        super(name, null, aliases);
        this.main = main;
        this.whitelistManager = whitelistManager;
        this.whitelistHandler = whitelistHandler;
        this.tabCompleter = tabCompleter;
        this.audiences = BungeeAudiences.create(main);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            whitelistHandler.showHelp(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")) {
            whitelistHandler.setWhitelist(true, sender);
            return;
        }

        if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) {
            whitelistHandler.setWhitelist(false, sender);
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (!sender.hasPermission("natwhitelist.add")) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }

            if (args.length == 1) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(mm.deserialize(main.getLangMessage("add-usage"))));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                whitelistHandler.addPlayerInWhitelist(sender, args[i]);
            }
            return;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("natwhitelist.remove")) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }

            if (args.length == 1) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(mm.deserialize(main.getLangMessage("remove-usage"))));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                whitelistHandler.removePlayerInWhitelist(sender, args[i]);
            }
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("natwhitelist.list")) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.listWhitelistedPlayersFormatted(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("removeoffline")) {
            if (!sender.hasPermission("natwhitelist.removeoffline")) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.removeOfflinePlayerInWhitelist(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("natwhitelist.reload")) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.reloadConfig(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("addallonlineplayer")) {
            if (!sender.hasPermission("natwhitelist.addallonlineplayer")) {
                audiences.sender(sender).sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.addAllOnlinePlayersToWhitelist(sender);
            return;
        }

        whitelistHandler.showHelp(sender);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return tabCompleter.onTabComplete(sender, args);
    }
}
