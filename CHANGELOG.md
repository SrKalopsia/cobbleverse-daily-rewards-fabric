# Changelog

All notable changes to this project will be documented in this file.

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
