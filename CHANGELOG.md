# Changelog

All notable changes to this project will be documented in this file.

## [4.1.0] - 2026-06-09

### Added
- **Dynamic Adaptability**: The Daily Rewards layout now dynamically adjusts the number of rows (from 3 to 6 rows) based on the maximum day configured in the `daily.json` settings:
  - Configs with 1–7 days are automatically displayed in a compact 3-row layout (1 week grid).
  - Configs with up to 14, 21, or 28+ days expand to 4, 5, or 6 rows respectively, preserving a clean weekly alignment.
- **Shared Center-Aligned GUI Layout**: Restructured the base screen layout in all rewards screens to feature a centered Return button flanked symmetrically by previous/next page navigation buttons.
  - Applied this clean layout to both the Daily Rewards and Playtime Rewards screens.
- **Documentation & Screenshots**: 
  - Added new high-resolution in-game screenshots to the repository under the `media/` folder.
  - Documented the daily grid, playtime milestone trackers, reward tooltips, active cooldown indicators, and NPC binding features in the `README.md`.

## [4.0.1] - 2026-06-07

### Added
- **Native Localization**: Added native Korean translation (`ko_kr`) thanks to the contribution of @Kakaka999.

## [4.0.0] - 2026-06-02

### Added
- **Universal Rebranding**: Mod officially renamed to **Universal Daily Rewards**.
- **Template System**: Introduced a powerful template engine.
  - New admin command: `/rewards-setup load <template>` (Level 4) to quickly apply pre-configured setups.
  - Included templates: `vanilla`, `economy`, and `cobbleverse`.
  - Automatic template export to `config/rewards/templates/` on startup.
- **Player Access Commands**: Added `/daily` and `/rewards open` for remote GUI access.
  - Toggleable via the new `allow_player_command` setting in `global.json`.
  - New admin command: `/rewards-setup allow-player-command <true/false>` to toggle this setting in-game.
- **New Utility Admin Commands**:
  - `/rewards-check <player>`: Check a player's current streak and playtime stats.
  - `/rewards-force-save`: Forcefully save all player data to disk immediately.
- **Internationalized Templates**: Updated default templates to use Minecraft's `translate` keys, allowing reward names to adapt to the player's client language automatically.
- **Improved UX**: 
  - Added claim sound effects (`ENTITY_EXPERIENCE_ORB_PICKUP`) for better feedback.
  - Silent command execution: Server-side reward commands are now executed silently to avoid chat spam.
- **Looping Streaks**: Daily rewards now automatically reset to Day 1 after reaching the end of the configured list, allowing for infinite racha cycles.

### Changed
- **Mod Identity**: Visible name updated to "Universal Daily Rewards", internal mod ID remains `rewards` for legacy support.
- **Global Config Overhaul**: `global.json` now includes `allow_player_command`. The mod automatically migrates old v3 configs to the new v4 format on startup.
- **Documentation**: Updated README and help strings to reflect the new 4.0.0 architecture.

## [3.1.0] - 2026-06-01

### Added
- **Visual Cooldown Timer**: Introduced a dynamic "Available in: Xh Ym" line in the Daily Rewards GUI tooltips.
  - This timer only displays for the reward immediately following the player's current streak to maintain a clean interface.
- **Short Time Formatting**: Added `TimeFormatter.formatShort` to support compact server-side translated time strings.
- **Expanded Localization**: Updated all 16 language files (English and Spanish variants) with new keys for the cooldown message and short time units ("h", "m").

## [3.0.1] - 2026-05-31

### Fixed
- **Time Translation**: Resolved a critical bug where time components (days, hours, minutes) in the Playtime Rewards menu were hardcoded and not translating to the client's language.
  - Refactored `TimeFormatter` to use `Text.translatable` components.
  - Added "days" formatting to time played/required strings.
- **Tooltip Localization**: Fixed reward item tooltips not being localized server-side. They now correctly respect each player's language preference.
- **Language Coverage**: Updated all 16 language files (8 assets, 8 data) with new keys for time units.

## [3.0.0] - 2026-05-30

### Changed
- **Server-Side Migration**: Successfully migrated the mod to a 100% Server-Side architecture using the **Polymer** framework.
  - The mod is no longer required on the client side.
  - GUI screens, buttons, and items are now handled via Polymer's virtual entity and screen handler systems.
- **Localization Refactor**: Integrated **Server Translations API** to handle multi-language support directly from the server.
  - Custom `Localization` helper implemented to safely translate `Text.translatable` keys based on each player's client language.
  - Translation files moved to `data/rewards/lang/` for automatic server-side discovery and loading.
- **Improved Stability**: 
  - Centralized translation logic in `MainMod.t()` to prevent `NullPointerException` in contexts where player context is not automatically available (e.g., entity interaction).
  - Wrapped GUI opening calls in `ServerInteractMixin` with `PolymerUtils.executeWithPlayerContext` to ensure proper synchronization.
- **Dependencies Updated**:
  - Updated to Fabric 1.21.1.
  - Added dependencies for `polymer-core`, `polymer-resource-pack`, and `server_translations_api`.

### Technical Summary
The migration involved a complete decoupling of the client-side rendering logic. All `Text` objects are now intercepted and translated server-side using the player's locale before being sent over the network. By utilizing Polymer, we've enabled the server to "spoof" custom UI elements using vanilla packet structures, ensuring a seamless experience for players without them needing to install any additional software.
