# NAT-Whitelist
![GitHub Actions](https://img.shields.io/github/actions/workflow/status/Nat0uille/NAT-Whitelist/main.yml?style=for-the-badge)
![License](https://img.shields.io/github/license/Nat0uille/NAT-Whitelist?style=for-the-badge)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/nat-whitelist?style=for-the-badge&label=MODRINTH%20DOWNLOADS)
![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/nat-whitelist?style=for-the-badge)

![NAT WHITELIST BANNER](https://cdn.modrinth.com/data/cached_images/3668fef1486fe33343bcbdc792bddd742bec2787_0.webp)
## What is
NAT-Whitelist is a powerful and flexible whitelist management plugin for Minecraft servers. It allows server administrators to easily control who can join the server, with support for both MySQL and SQLite databases.

## Features

- Add or remove players from the whitelist using commands
- Supports both MySQL and SQLite for storing whitelist data
- Automatically fetches correct usernames from Mojang API
- Option to kick non-whitelisted players automatically
- Option to remove offline players from the whitelist
- Customizable messages using MiniMessage format
- Easy integration with Bukkit/Spigot servers

## Commands

| Command                | Description                                 |
|------------------------|---------------------------------------------|
| `/whitelist add <name> <another name>`    | Adds a player to the whitelist            |
| `/whitelist remove <name><another name>` | Removes a player from the whitelist        |
| `/whitelist list`          | Lists all whitelisted players              |
| `/whitelist reload`        | Reloads the plugin configuration           |
| `/whitelist enable`        | Enables the whitelist                      |
| `/whitelist disable`       | Disables the whitelist                     |
| `/whitelist removeoffline` | Remove offlines players                    |

## Permissions

| Permission                   | Description                                      |
|------------------------------|--------------------------------------------------|
| `natwhitelist.reload`        | Allows you to reload the configuration           |
| `natwhitelist.add`           | Allows you to add a player to the whitelist      | 
| `natwhitelist.remove`        | Allows you to remove a player from the whitelist | 
| `natwhitelist.on`            | Enables the whitelist                            | 
| `natwhitelist.off`           | Allows you to disable the whitelist              | 
| `natwhitelist.removeoffline` | Allows offline players to be removed             | 
| `natwhitelist.bypass`        | Allows joining even without being whitelisted    |
| `natwhitelist.list`          | Allows you to list whitelisted players           |
| `natwhitelist.admin`         | Grants all NAT-Whitelist rights (inherits all of the above permissions) |
| `natwhitelist.*`             | Same as natwhitelist.admin                       |

##  API

NAT-Whitelist provides a developer API to interact with the whitelist directly from your own plugins. You can add, remove, check, list whitelisted players, and enable/disable the whitelist programmatically.

See the full documentation in [API-README.md](./API-README.md) for all available methods, usage details, and code examples.

Main API features:
- Add or remove a player from the whitelist
- Check if a player is whitelisted
- List all whitelisted players
- Enable or disable the whitelist

To integrate the API into your plugin, follow the instructions and examples in API-README.md.
