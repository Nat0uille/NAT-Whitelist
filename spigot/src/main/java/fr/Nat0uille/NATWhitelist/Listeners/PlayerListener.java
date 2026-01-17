package fr.Nat0uille.NATWhitelist.Listeners;

import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.WhitelistHandler;
import fr.Nat0uille.NATWhitelist.WhitelistManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final WhitelistHandler whitelistHandler;

    MiniMessage mm = MiniMessage.miniMessage();
    private Component prefix;
    private Component kickMessage;

    public PlayerListener(Main main, WhitelistManager whitelistManager, WhitelistHandler whitelistHandler) {
        this.main = main;
        this.whitelistManager = whitelistManager;
        this.whitelistHandler = whitelistHandler;

        // Component messages
        this.prefix = mm.deserialize(main.getLangMessage("prefix"));
        this.kickMessage = mm.deserialize(main.getLangMessage("kick-message"));
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        String currentName = event.getPlayer().getName();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().hasPermission("natwhitelist.admin")) {
                    if (main.getCheckVersion().outdated()) {
                        event.getPlayer().sendMessage(prefix.append(mm.deserialize(main.getLangMessage("outdated")
                                .replace("{latest}", main.getCheckVersion().getRemoteVersion())
                                .replace("{local}", main.getCheckVersion().getLocalVersion()))));
                    }
                }
            }
        }.runTaskLater(main, 10);

        String storedName = whitelistManager.getPlayerNameByUUID(playerUUID);
        if (storedName != null && !storedName.equalsIgnoreCase(currentName)) {
            whitelistManager.updatePlayerName(playerUUID, currentName);
        }
        if (!event.getPlayer().hasPermission("natwhitelist.bypass")) {
            if (whitelistHandler.isEnabled() && !whitelistManager.isWhitelisted(playerUUID)) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, prefix.append(kickMessage));
            }
        }
    }
}