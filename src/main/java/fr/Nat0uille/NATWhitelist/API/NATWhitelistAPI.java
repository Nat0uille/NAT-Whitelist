package fr.Nat0uille.NATWhitelist.API;

import java.util.UUID;
import java.sql.SQLException;

public interface NATWhitelistAPI {
    boolean add(UUID uuid, String playerName) throws SQLException;
}
