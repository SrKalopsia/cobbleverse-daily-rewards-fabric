# Cobbleverse Daily Rewards Fabric

![Version](https://img.shields.io/badge/version-3.0.1-blue.svg) ![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg) ![License](https://img.shields.io/badge/license-MIT-yellow.svg) ![Platform](https://img.shields.io/badge/platform-Fabric-orange.svg)

This release marks a complete evolution and total overhaul from version 1.0.0 of the original `daily-rewards-fabric` mod.

[![Download on Modrinth](https://img.shields.io/badge/Download-Modrinth-1bd96a.svg)](https://modrinth.com/mod/cobbleverse-daily-rewards) [![GitHub](https://img.shields.io/badge/GitHub-Source-black.svg)](https://github.com/SrKalopsia/cobbleverse-daily-rewards-fabric)

While this fork has been redesigned, translated, and optimized with the **Cobbleverse** ecosystem in mind, **it is 100% standalone and can be used on any Fabric server or modpack.**

📖 **[Click here to read the full CHANGELOG](CHANGELOG.md)**

### ✨ New Features & Improvements

* 🚀 **100% Server-Side (Polymer):** The mod is now fully server-side. Players do not need to install the mod to see the GUIs or interact with the rewards system.
* 🌍 **Native Localization Support (i18n):** GUI texts are no longer hardcoded. Using the **Server Translations API**, the mod now automatically adapts to the player's client language (Currently includes native support for English and Spanish, falling back to English for other languages) without requiring any client-side resource packs.
* 🛠️ **New Admin Commands:** Added the `/rewards-setplaytime <player> <seconds>` command to allow administrators to manually adjust a player's tracked playtime, making server management and user support much easier.
* ⚙️ **Generic Configurations:** The mod now generates much cleaner, universal default configuration files (`daily.json` and `playtime.json`) right out of the box, ready for a "Plug & Play" experience.

### 🐛 Critical Bug Fixes

Several severe bugs present in the original version have been patched:

* **NPC Persistence:** Fixed a critical issue where NPCs (Screen Entities) configured to open the rewards menu would lose their link and stop working after a server restart. Data is now forcefully saved to disk when adding or removing an entity.
* **Broken Pagination:** Fixed a mathematical logic error (`Math.ceil`) in `AbstractRewardScreen` that miscalculated the maximum number of pages, preventing players from accessing subsequent reward pages.
* **Ghost Click Console Crashes:** Added a safety check to prevent the `java.lang.IndexOutOfBoundsException: Index -1` server console error, which occurred when a player clicked outside the GUI inventory bounds.

## 🛠️ Commands

### Player Commands

* *(Configurable via NPCs or custom items using the Mod's API)*

### Admin Commands (Permission Level 2)

* `/rewards-reload-<type>-config` - Reloads a specific config file without restarting the server.
* `/rewards-reset <player>` - Resets all playtime and daily streaks to zero for a specific player.
* `/rewards-setstreak <player> <days>` - Manually adjusts a player's daily streak.
* `/rewards-setplaytime <player> <seconds>` - Manually sets a player's tracked playtime.
* `/rewards-screen-entity add <entity>` - Registers an entity (like an NPC) to open the rewards GUI when clicked.
* `/rewards-screen-entity remove <entity>` - Unregisters a screen entity.

## ⚙️ Configuration Example & Flexibility
Curious about how customizable this is? The mod uses a clean, intuitive JSON structure that gives you total freedom over how rewards are distributed. You can mix and match two methods:

* **Method 1 (Via Commands):** Perfect for virtual economy (like *Cobbledollars*), permissions, or complex items with NBT/Lore. You run the command in the background and set `"give_item": false` so the GUI item only acts as a visual icon.
* **Method 2 (Direct Physical Items):** You can leave the `commands` array empty and simply set `"give_item": true`. The mod will directly give the player the exact physical item shown in the GUI.

Here is a quick sneak peek of how a daily reward is configured using both methods:

```json
[
  {
    "day": 1,
    "id": "day_1",
    "commands": [
      "cobbledollars give %player% 5000"
    ],
    "items": [
      {
        "item": "minecraft:paper",
        "name": "{\"text\":\"5,000 Cobbledollars (Command)\",\"color\":\"green\"}",
        "amount": 1,
        "give_item": false
      },
      {
        "item": "cobblemon:rare_candy",
        "name": "{\"text\":\"Rare Candy (Direct Item)\",\"color\":\"aqua\"}",
        "amount": 3,
        "give_item": true
      }
    ]
  }
]
```
> 📄 **Want to see the full potential?** 
> Check out the complete default configurations on our GitHub:
> * [View default daily.json](https://github.com/SrKalopsia/cobbleverse-daily-rewards-fabric/blob/main/src/main/resources/config/daily.json)
> * [View default playtime.json](https://github.com/SrKalopsia/cobbleverse-daily-rewards-fabric/blob/main/src/main/resources/config/playtime.json)

## 📜 Credits

This project is an overhauled fork of the original [Daily-Rewards-Fabric](https://github.com/SmugTheKiler/daily-rewards-fabric) by SmugTheKiler, originally released to the Public Domain (CC0). Huge thanks to them for laying the foundation!
