package fr.Nat0uille.NATWhitelist.API;

import java.util.UUID;

public interface NATWhitelistAPI {
    boolean add(UUID uuid, String playerName);
}
