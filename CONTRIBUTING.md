# Contributing to Universal Daily Rewards

First off, thank you for considering contributing to Universal Daily Rewards! It's people like you that make the open-source community such a great place.

## 🌍 How to Contribute Translations (i18n)

Since version 4.0.0, the mod supports native Server Translations. We would love to have the mod available in as many languages as possible!

To ensure translations function correctly for a 100% server-side experience, translation files must be placed in **two separate directories**:

1. **Assets Directory:** `src/main/resources/assets/rewards/lang/`
   - *Purpose:* Used as a fallback and for standard client-side translation lookup (e.g. if a player or server uses a resource pack).
2. **Data Directory:** `src/main/resources/data/rewards/lang/`
   - *Purpose:* Used by the **Server Translations API** to read translation keys and translate text directly on the server before sending packets to the Minecraft client. This ensures the UI/menus are fully localized even if the client doesn't have the mod installed.

### Steps to contribute a translation:

1. Fork this repository.
2. Copy `en_us.json` from **both** `assets` and `data` directories, then rename them to your target language code (e.g., `fr_fr.json` for French, `de_de.json` for German, `ko_kr.json` for Korean).
3. Translate the values (the text on the right side of the colon) into your language. **Do not change the keys** on the left. Make sure the translated contents are identical in both files.
4. Commit your changes and open a Pull Request!

## 🐛 Reporting Bugs

Before creating bug reports, please check the existing issues to see if the problem has already been reported. When you are creating a bug report, please include as many details as possible, including:

* Minecraft version (e.g., 1.21.1)
* Mod version (e.g., 4.0.1)
* Fabric API and Polymer versions
* A pastebin link to your server crash report or logs.

## 💻 Contributing Code

If you want to add a new feature or fix a bug:

1. Fork the repo and create your branch from `main`.
2. Ensure you test your changes in a local server environment.
3. Keep your code style consistent with the rest of the project.
4. Issue that pull request!
