package fr.Nat0uille.NATWhitelist.Listeners;

import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.CheckVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import java.sql.SQLException;

public class PlayerListener implements Listener {
    private final WhitelistListener whitelistListener;
    private final Main main;

    public PlayerListener(WhitelistListener whitelistListener, Main main) {
        this.whitelistListener = whitelistListener;
        this.main = main;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component prefix = mm.deserialize(main.getConfig().getString("prefix"));
        Component kickmessage = mm.deserialize(main.getConfig().getString("kickmessage"));
        String playerName = event.getPlayer().getName();
        try {
            if (whitelistListener.isEnabled() && !whitelistListener.isWhitelisted(playerName)) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, prefix.append(kickmessage));
            }
        } catch (SQLException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, prefix.append(mm.deserialize("<red>Erreur SQL</red>")));
            e.printStackTrace();
        }
        if (event.getPlayer().hasPermission("natwhitelist.admin") || CheckVersion.isUpdateAvailable) {
            Component updateMessage = mm.deserialize(main.getConfig().getString("updateMessage"));
            if (CheckVersion.isUpdateAvailable) {
                event.getPlayer().sendMessage(prefix.append(updateMessage));
            }

        }
    }
}