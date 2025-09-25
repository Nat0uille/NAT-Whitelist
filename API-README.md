# NAT-Whitelist API Documentation
```
⚠️This part of the documentation is currently being written, sorry if anything is missing.
```
## 🚀 Installation
Add NAT-Whitelist as a dependency in your `plugin.yml` :
```yaml
depend: [NAT-Whitelist]
# or
softdepend: [NAT-Whitelist]
```
## 🔧 Get the API instance

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

## ⚠️ Error Handling

The API can throw a `SQLException` during database operations. It is important to properly handle these exceptions:

```java
catch (SQLException e) {
    // Log the complete error for debugging
    plugin.getLogger().severe("NAT-Whitelist API SQL Error message: " + e.getMessage());
    e.printStackTrace();
}
```