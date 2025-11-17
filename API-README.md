# NAT-Whitelist API Documentation
```
⚠️ This part of the documentation is currently being written. Sorry if anything is missing.
```

## 🚀 Installation
Add NAT-Whitelist as a dependency in your `plugin.yml`:
```yaml
depend: [NAT-Whitelist]
# or
softdepend: [NAT-Whitelist]
```

## 🔧 Getting the API instance

```java
import fr.Nat0uille.NATWhitelist.API.NATWhitelistAPI;
import org.bukkit.Bukkit;

public class MyPlugin extends JavaPlugin {
    private NATWhitelistAPI whitelistAPI;

    @Override
    public void onEnable() {
        // Get the API instance
        whitelistAPI = Bukkit.getServer().getServicesManager().getRegistration(NATWhitelistAPI.class).getProvider();
        if (whitelistAPI == null) {
            getLogger().severe("Unable to get NAT-Whitelist API!");
            return;
        }
        getLogger().info("NAT-Whitelist API loaded successfully!");
    }
}
```

## ✨ API Methods

### Add a player to the whitelist
```java
boolean add(UUID uuid, String playerName);
```
Adds the player to the whitelist. Returns `true` if the player was added.

### Remove a player from the whitelist
```java
boolean remove(UUID uuid);
```
Removes the player from the whitelist. Returns `true` if the player was removed.

### Check if a player is whitelisted
```java
boolean isWhitelisted(UUID uuid);
```
Returns `true` if the player is whitelisted.

### Get the list of whitelisted players
```java
List<String> getWhitelistedPlayers();
```
Returns a list of whitelisted player names.

### Get a formatted list of whitelisted players
```java
String listWhitelistedPlayers();
```
Returns a string containing all whitelisted players separated by commas.

### Enable or disable the whitelist
```java
void setEnabled(boolean enabled);
```
Enables (`true`) or disables (`false`) the whitelist.

### Check if the whitelist is enabled
```java
boolean isEnabled();
```
Returns `true` if the whitelist is enabled.
