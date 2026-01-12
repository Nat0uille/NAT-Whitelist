package fr.Nat0uille.NATWhitelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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

    public boolean remove(UUID uuid) {
        try {
            return databaseManager.delete("nat_whitelist", "uuid = ?", uuid.toString()) > 0;
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

    public String getFormattedList(Function<UUID, String> getPlayerName, Function<UUID, Boolean> isOnline) {
        List<UUID> uuids = list();

        if (uuids.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < uuids.size(); i++) {
            UUID uuid = uuids.get(i);
            String playerName = getPlayerName.apply(uuid);
            boolean online = isOnline.apply(uuid);

            // If display name not found, use uuid
            String displayName = playerName != null ? playerName : uuid.toString();

            // Green color if online, gray if offline
            String color = online ? "<green>" : "<gray>";

            result.append(color)
                  .append(displayName)
                  .append("</")
                  .append(online ? "green" : "gray")
                  .append(">");

            if (i < uuids.size() - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }
}
