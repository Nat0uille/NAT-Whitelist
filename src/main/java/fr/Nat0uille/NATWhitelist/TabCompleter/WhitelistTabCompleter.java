package fr.Nat0uille.NATWhitelist.TabCompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import fr.Nat0uille.NATWhitelist.Listeners.WhitelistListener;
import org.bukkit.entity.Player;
import java.sql.SQLException;

public class WhitelistTabCompleter implements TabCompleter {
    private final WhitelistListener whitelistListener;
    private List<String> cachedPlayers = new ArrayList<>();

    public WhitelistTabCompleter(WhitelistListener whitelistListener) {
        this.whitelistListener = whitelistListener;
        updateCache();
    }

    public void updateCache() {
        try {
            cachedPlayers = whitelistListener.getWhitelistedPlayers();
        } catch (SQLException e) {
            cachedPlayers = new ArrayList<>();
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        updateCache();
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "on", "off", "removeoffilne");
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