package fr.Nat0uille.NATWhitelist.TabCompleter;

import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WhitelistTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list");
        }
        // Pour add/remove, tu peux proposer des pseudos ici si tu veux
        return Collections.emptyList();
    }
}
