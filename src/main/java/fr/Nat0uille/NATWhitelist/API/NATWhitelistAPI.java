package fr.Nat0uille.NATWhitelist.API;

import java.util.UUID;

public interface NATWhitelistAPI {
    boolean add(UUID uuid);
    boolean remove(UUID uuid);
    boolean isWhitelisted(UUID uuid);
    java.util.List<String> getWhitelistedPlayers();
    String listWhitelistedPlayers();
    void setEnabled(boolean enabled);
    boolean isEnabled();
}
