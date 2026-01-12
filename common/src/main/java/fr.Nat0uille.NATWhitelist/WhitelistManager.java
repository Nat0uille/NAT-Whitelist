package fr.Nat0uille.NATWhitelist;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhitelistManager {

    private final DatabaseManager databaseManager;

    public WhitelistManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean add(UUID uuid) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("uuid", uuid.toString());
            return databaseManager.insert("nat_whitelist", data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
