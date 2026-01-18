package fr.Nat0uille.NATWhitelist.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.WhitelistHandler;
import fr.Nat0uille.NATWhitelist.WhitelistManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhitelistCommand implements SimpleCommand {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final WhitelistHandler whitelistHandler;
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WhitelistCommand(Main main, WhitelistManager whitelistManager, WhitelistHandler whitelistHandler, ProxyServer server) {
        this.main = main;
        this.whitelistManager = whitelistManager;
        this.whitelistHandler = whitelistHandler;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

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
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }

            if (args.length == 1) {
                sender.sendMessage(whitelistHandler.getPrefix().append(mm.deserialize(main.getLangMessage("add-usage"))));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                whitelistHandler.addPlayerInWhitelist(sender, args[i]);
            }
            return;
        }

        if (args[0].equalsIgnoreCase("remove")) {

            if (!sender.hasPermission("natwhitelist.remove")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }

            if (args.length == 1) {
                sender.sendMessage(whitelistHandler.getPrefix().append(mm.deserialize(main.getLangMessage("remove-usage"))));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                whitelistHandler.removePlayerInWhitelist(sender, args[i]);
            }
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("natwhitelist.list")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.listWhitelistedPlayersFormatted(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("removeoffline")) {
            if (!sender.hasPermission("natwhitelist.removeoffline")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.removeOfflinePlayerInWhitelist(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("natwhitelist.reload")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.reloadConfig(sender);
        }

        if (args[0].equalsIgnoreCase("addallonlineplayer")) {
            if (!sender.hasPermission("natwhitelist.addallonlineplayer")) {
                sender.sendMessage(whitelistHandler.getPrefix().append(whitelistHandler.getNoPermission()));
                return;
            }
            whitelistHandler.addAllOnlinePlayersToWhitelist(sender);
            return;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0 || args.length == 1) {
            return Arrays.asList("add", "remove", "list", "on", "off", "removeoffline", "enable", "disable", "reload", "help", "addallonlineplayer");
        }

        // args.length >= 2
        {
            String prefix = args[args.length - 1].toLowerCase();
            List<String> alreadyTyped = Arrays.asList(Arrays.copyOfRange(args, 1, args.length - 1));
            List<String> result = new ArrayList<>();
            List<String> cachedPlayers = whitelistManager.getWhitelistedPlayers();

            if (args[0].equalsIgnoreCase("add")) {
                for (Player player : server.getAllPlayers()) {
                    String name = player.getUsername();
                    if (!cachedPlayers.contains(name) && !alreadyTyped.contains(name) &&
                            (prefix.isEmpty() || name.toLowerCase().startsWith(prefix))) {
                        result.add(name);
                    }
                }
                return result;
            }

            if (args[0].equalsIgnoreCase("remove")) {
                for (String player : cachedPlayers) {
                    if (!alreadyTyped.contains(player) && (prefix.isEmpty() || player.toLowerCase().contains(prefix))) {
                        result.add(player);
                    }
                }
                return result;
            }
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("natwhitelist.use");
    }
}
