package fr.Nat0uille.NATWhitelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistManager {

    private final DatabaseManager databaseManager;

    public WhitelistManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean isWhitelisted(UUID uuid) {
        try {
            List<Map<String, Object>> results = databaseManager.select("nat_whitelist", "uuid = '" + uuid.toString() + "'");
            return !results.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isWhitelisted(String playerName) {
        try {
            List<Map<String, Object>> results = databaseManager.select("nat_whitelist", "player_name = '" + playerName + "'");
            return !results.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean add(UUID uuid, String playerName) {
        try {
            if (isWhitelisted(uuid)) {
                return false;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("player_name", playerName);
            data.put("uuid", uuid.toString());
            return databaseManager.insert("nat_whitelist", data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean remove(UUID uuid) {
        try {
            return databaseManager.delete("nat_whitelist", "uuid = ?", uuid.toString()) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean remove(String playerName) {
        try {
            return databaseManager.delete("nat_whitelist", "player_name = ?", playerName) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UUID> list() {
        try {
            List<Map<String, Object>> results = databaseManager.select("nat_whitelist", null);
            List<UUID> uuids = new ArrayList<>();
            for (Map<String, Object> row : results) {
                Object uuidObj = row.get("uuid");
                if (uuidObj != null) {
                    uuids.add(UUID.fromString(uuidObj.toString()));
                }
            }
            return uuids;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<UUID, String> listWithNames() {
        try {
            List<Map<String, Object>> results = databaseManager.select("nat_whitelist", null);
            Map<UUID, String> players = new HashMap<>();
            for (Map<String, Object> row : results) {
                Object uuidObj = row.get("uuid");
                Object nameObj = row.get("player_name");
                if (uuidObj != null && nameObj != null) {
                    players.put(UUID.fromString(uuidObj.toString()), nameObj.toString());
                }
            }
            return players;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public String getFormattedList() {
        Map<UUID, String> players = listWithNames();

        if (players.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int i = 0;

        for (Map.Entry<UUID, String> entry : players.entrySet()) {
            String playerName = entry.getValue();

            result.append("<gray>")
                  .append(playerName)
                  .append("</gray>");

            if (i < players.size() - 1) {
                result.append("\n");
            }
            i++;
        }

        return result.toString();
    }
}
