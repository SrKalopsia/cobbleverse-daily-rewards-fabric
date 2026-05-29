# Cobbleverse Daily Rewards Fabric

A fully functional, bilingual, and heavily optimized fork of the original Daily Rewards mod, tailored for modern Fabric servers.

This mod rewards players for their loyalty and playtime, featuring interactive GUI menus, configurable item/command rewards, and support for complex modpack ecosystems.

## ✨ New Features in this Fork

* **Full Internationalization (i18n):** Texts are no longer hardcoded. The mod natively supports English (`en_us`) and Spanish (`es_es`, `es_mx`, etc.), adapting automatically to the player's client language.
* **New Admin Commands:** Added the ability to manually set a player's playtime via commands, making server administration much easier.
* **Critical Bug Fixes:** * Fixed an issue where NPC Screen Entities would reset upon server restart.
  * Fixed a pagination calculation bug (`Math.ceil`) that prevented proper display of subsequent reward pages.
  * Patched a ghost crash (`Index -1 out of bounds`) in the server console caused by clicking outside the GUI.
* **Generic Configs:** Ships with clean, universal default configurations out of the box.

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

## 📜 Credits

This project is an overhauled fork of the original [Daily-Rewards-Fabric](https://github.com/SmugTheKiler/daily-rewards-fabric) by SmugTheKiler, originally released to the Public Domain (CC0). Huge thanks to them for laying the foundation!
