package fr.Nat0uille.NATWhitelist.TabCompleter;

import fr.Nat0uille.NATWhitelist.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.entity.Player;

public class WhitelistTabCompleter implements TabCompleter {
    private final WhitelistManager whitelist;
    private List<String> cachedPlayers = new ArrayList<>();

    public WhitelistTabCompleter(WhitelistManager whitelist) {
        this.whitelist = whitelist;
        updateCache();
    }

    public void updateCache() {
        cachedPlayers = whitelist.getWhitelistedPlayers();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        updateCache();
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "on", "off", "removeoffline", "enable", "disable", "reload", "help", "addallonlineplayer");
        }
        if (args.length >= 2) {
            String prefix = args[args.length - 1].toLowerCase();
            List<String> alreadyTyped = Arrays.asList(Arrays.copyOfRange(args, 1, args.length - 1));
            List<String> result = new ArrayList<>();
            if (args[0].equalsIgnoreCase("add")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
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