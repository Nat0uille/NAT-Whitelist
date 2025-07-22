package fr.Nat0uille.NATWhitelist.Listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener implements Listener {
    private final WhitelistListener whitelistListener;

    public PlayerListener(WhitelistListener whitelistListener) {
        this.whitelistListener = whitelistListener;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        if (whitelistListener.isEnabled() && !whitelistListener.isWhitelisted(playerName)) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "§cVous n'êtes pas whitelist sur ce serveur.");
        }
    }
}