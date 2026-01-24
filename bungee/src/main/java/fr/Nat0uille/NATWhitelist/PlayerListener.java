package fr.Nat0uille.NATWhitelist;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.ProxyServer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final WhitelistHandler whitelistHandler;
    private final BungeeAudiences audiences;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlayerListener(Main main, WhitelistManager whitelistManager, WhitelistHandler whitelistHandler) {
        this.main = main;
        this.whitelistManager = whitelistManager;
        this.whitelistHandler = whitelistHandler;
        this.audiences = BungeeAudiences.create(main);
    }

    @EventHandler
    public void onPlayerLogin(LoginEvent event) {
        UUID playerUUID = event.getConnection().getUniqueId();
        String currentName = event.getConnection().getName();

        String storedName = whitelistManager.getPlayerNameByUUID(playerUUID);
        if (storedName != null && !storedName.equalsIgnoreCase(currentName)) {
            whitelistManager.updatePlayerName(playerUUID, currentName);
        }

        if (whitelistHandler.isEnabled() && !whitelistManager.isWhitelisted(playerUUID)) {
            event.setCancelled(true);
            event.setCancelReason(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                    net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                            main.getLangMessage("kick-message"))
            ));
        }

        ProxyServer.getInstance().getScheduler().schedule(main, () -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
            if (player != null && player.hasPermission("natwhitelist.admin")) {
                if (main.getCheckVersion().outdated()) {
                    audiences.player(player).sendMessage(
                            mm.deserialize(main.getLangMessage("prefix")).append(
                                    mm.deserialize(main.getLangMessage("outdated")
                                            .replace("{latest}", main.getCheckVersion().getRemoteVersion())
                                            .replace("{local}", main.getCheckVersion().getLocalVersion()))
                            )
                    );
                }
            }
        }, 2L, TimeUnit.SECONDS);
    }
}
