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

## ✨ Méthodes de l'API

### Ajouter un joueur à la whitelist
```java
boolean add(UUID uuid);
```
Ajoute le joueur à la whitelist. Retourne `true` si le joueur a été ajouté.

### Retirer un joueur de la whitelist
```java
boolean remove(UUID uuid);
```
Retire le joueur de la whitelist. Retourne `true` si le joueur a été retiré.

### Vérifier si un joueur est whitelisté
```java
boolean isWhitelisted(UUID uuid);
```
Retourne `true` si le joueur est dans la whitelist.

### Obtenir la liste des joueurs whitelistés
```java
List<String> getWhitelistedPlayers();
```
Retourne une liste des noms des joueurs whitelistés.

### Obtenir la liste formatée des joueurs whitelistés
```java
String listWhitelistedPlayers();
```
Retourne une chaîne contenant tous les joueurs whitelistés séparés par des virgules.

### Activer ou désactiver la whitelist
```java
void setEnabled(boolean enabled);
```
Active (`true`) ou désactive (`false`) la whitelist.

### Vérifier si la whitelist est activée
```java
boolean isEnabled();
```
Retourne `true` si la whitelist est activée.
