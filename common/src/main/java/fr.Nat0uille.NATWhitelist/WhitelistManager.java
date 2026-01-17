package fr.Nat0uille.NATWhitelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistManager {

    private final DatabaseManager databaseManager;
    private java.util.function.Function<String, Boolean> onlineChecker;

    public WhitelistManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.onlineChecker = playerName -> false; // Par défaut, tous les joueurs sont considérés hors ligne
    }

    public WhitelistManager(DatabaseManager databaseManager, java.util.function.Function<String, Boolean> onlineChecker) {
        this.databaseManager = databaseManager;
        this.onlineChecker = onlineChecker;
    }

    public void setOnlineChecker(java.util.function.Function<String, Boolean> onlineChecker) {
        this.onlineChecker = onlineChecker;
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

    public List<String> getWhitelistedPlayers() {
        Map<UUID, String> playersMap = listWithNames();
        return new ArrayList<>(playersMap.values());
    }

    public String getFormattedList() {
        List<String> players = getWhitelistedPlayers();
        List<String> coloredPlayers = new ArrayList<>();

        for (String playerName : players) {
            boolean isOnline = onlineChecker.apply(playerName);
            if (isOnline) {
                coloredPlayers.add("<#63c74d>" + playerName);
            } else {
                coloredPlayers.add("<#951919>" + playerName);
            }
        }

        return String.join(", ", coloredPlayers);
    }

    public String getPlayerNameByUUID(UUID uuid) {
        try {
            List<Map<String, Object>> results = databaseManager.select("nat_whitelist", "uuid = '" + uuid.toString() + "'");
            if (!results.isEmpty()) {
                Object nameObj = results.getFirst().get("player_name");
                return nameObj != null ? nameObj.toString() : null;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePlayerName(UUID uuid, String playerName) {
        databaseManager.update("nat_whitelist", Map.of("player_name", playerName), "uuid = ?", uuid.toString());
    }

}
