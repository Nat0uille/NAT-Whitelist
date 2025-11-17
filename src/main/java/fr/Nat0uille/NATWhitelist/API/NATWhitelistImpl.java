package fr.Nat0uille.NATWhitelist.API;

import fr.Nat0uille.NATWhitelist.Main;
import fr.Nat0uille.NATWhitelist.Whitelist;

import java.util.UUID;

public class NATWhitelistImpl implements NATWhitelistAPI {

    private final Main main;
    private final Whitelist whitelist;

    public NATWhitelistImpl(Main main) {
        this.main = main;
        this.whitelist = main.getWhitelistListener();
    }

    @Override
    public boolean add(UUID uuid, String playerName) {
        try {
            return whitelist.add(uuid, playerName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean remove(UUID uuid) {
        try {
            return whitelist.remove(uuid);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isWhitelisted(UUID uuid) {
        try {
            return whitelist.isWhitelisted(uuid);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public java.util.List<String> getWhitelistedPlayers() {
        try {
            return whitelist.getWhitelistedPlayers();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public String listWhitelistedPlayers() {
        try {
            return whitelist.listWhitelistedPlayers();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        whitelist.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return whitelist.isEnabled();
    }
}
