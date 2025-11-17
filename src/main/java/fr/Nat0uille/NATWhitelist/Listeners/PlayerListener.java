package fr.Nat0uille.NATWhitelist.Listeners;

import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final Whitelist whitelist;
    private final Main main;

    public PlayerListener(Whitelist whitelist, Main main) {
        this.whitelist = whitelist;
        this.main = main;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component prefix = mm.deserialize(main.getLangMessage("prefix"));
        Component kickmessage = mm.deserialize(main.getLangMessage("kickmessage"));
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
        }.runTaskLater(main, 20);

        try {
            String storedName = whitelist.getPlayerNameByUUID(playerUUID);
            if (storedName != null && !storedName.equalsIgnoreCase(currentName)) {
                whitelist.updatePlayerName(playerUUID, currentName);
            }
            if (!event.getPlayer().hasPermission("natwhitelist.bypass")) {
                 if (whitelist.isEnabled() && !whitelist.isWhitelisted(playerUUID)) {
                    event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, prefix.append(kickmessage));
                }
            }
        } catch (SQLException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, prefix.append(mm.deserialize("<red>SQL error</red>")));
            e.printStackTrace();
        }
    }
}