package fr.Nat0uille.NATWhitelist;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WhitelistTabCompleter {
    private final WhitelistManager whitelist;
    private List<String> cachedPlayers = new ArrayList<>();

    public WhitelistTabCompleter(WhitelistManager whitelist) {
        this.whitelist = whitelist;
        updateCache();
    }

    public void updateCache() {
        cachedPlayers = whitelist.getWhitelistedPlayers();
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        updateCache();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> options = Arrays.asList("add", "remove", "list", "on", "off", "removeoffline", "enable", "disable", "reload", "help", "addallonlineplayer");
            List<String> result = new ArrayList<>();
            for (String option : options) {
                if (prefix.isEmpty() || option.toLowerCase().startsWith(prefix)) {
                    result.add(option);
                }
            }
            return result;
        }
        if (args.length >= 2) {
            String prefix = args[args.length - 1].toLowerCase();
            List<String> alreadyTyped = Arrays.asList(Arrays.copyOfRange(args, 1, args.length - 1));
            List<String> result = new ArrayList<>();

            if (args[0].equalsIgnoreCase("add")) {
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    String name = player.getName();
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
        return Collections.emptyList();
    }
}
