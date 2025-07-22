package fr.Nat0uille.NATWhitelist.TabCompleter;

import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import fr.Nat0uille.NATWhitelist.Listeners.WhitelistListener;

public class WhitelistTabCompleter implements TabCompleter {
    private final WhitelistListener whitelistListener;
    private List<String> cachedPlayers = new ArrayList<>();

    public WhitelistTabCompleter(WhitelistListener whitelistListener) {
        this.whitelistListener = whitelistListener;
        updateCache();
    }

    public void updateCache() {
        cachedPlayers = whitelistListener.getWhitelistedPlayers();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return cachedPlayers;
        }
        return Collections.emptyList();
    }
}