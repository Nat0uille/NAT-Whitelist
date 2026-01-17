package fr.Nat0uille.NATWhitelist.Listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.WhitelistHandler;
import fr.Nat0uille.NATWhitelist.WhitelistManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener {

    private final Main main;
    private final WhitelistManager whitelistManager;
    private final WhitelistHandler whitelistHandler;
    private final ProxyServer server;

    private final MiniMessage mm = MiniMessage.miniMessage();
    private Component prefix;
    private Component kickMessage;

    public PlayerListener(Main main, WhitelistManager whitelistManager, WhitelistHandler whitelistHandler, ProxyServer server) {
        this.main = main;
        this.whitelistManager = whitelistManager;
        this.whitelistHandler = whitelistHandler;
        this.server = server;

        // Component messages
        this.prefix = mm.deserialize(main.getLangMessage("prefix"));
        this.kickMessage = mm.deserialize(main.getLangMessage("kick-message"));
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String currentName = player.getUsername();

        // Schedule version check notification
        server.getScheduler()
            .buildTask(main, () -> {
                if (player.hasPermission("natwhitelist.admin")) {
                    if (main.getCheckVersion().outdated()) {
                        player.sendMessage(prefix.append(mm.deserialize(main.getLangMessage("outdated")
                                .replace("{latest}", main.getCheckVersion().getRemoteVersion())
                                .replace("{local}", main.getCheckVersion().getLocalVersion()))));
                    }
                }
            })
            .delay(500, TimeUnit.MILLISECONDS)
            .schedule();

        // Update player name if changed
        String storedName = whitelistManager.getPlayerNameByUUID(playerUUID);
        if (storedName != null && !storedName.equalsIgnoreCase(currentName)) {
            whitelistManager.updatePlayerName(playerUUID, currentName);
        }

        // Check whitelist
        if (!player.hasPermission("natwhitelist.bypass")) {
            if (whitelistHandler.isEnabled() && !whitelistManager.isWhitelisted(playerUUID)) {
                event.setResult(LoginEvent.ComponentResult.denied(prefix.append(kickMessage)));
            }
        }
    }
}
