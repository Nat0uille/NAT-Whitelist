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
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "on", "off");
        }
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            if (args[0].equalsIgnoreCase("remove")) {
                List<String> result = new ArrayList<>();
                for (String player : cachedPlayers) {
                    if (player.toLowerCase().startsWith(prefix)) {
                        result.add(player);
                    }
                }
                return result;
            }
            if (args[0].equalsIgnoreCase("add")) {
                List<String> result = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String name = player.getName();
                    if (!cachedPlayers.contains(name) && name.toLowerCase().startsWith(prefix)) {
                        result.add(name);
                    }
                }
                return result;
            }
        }
        return Collections.emptyList();
    }
}