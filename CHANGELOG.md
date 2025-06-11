# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Added trust anchors for system and user certificates in `network_security_config.xml`. This allows the app to trust self-signed certificates. Exercise caution when adding user certificates. Malicious certificates can compromise your security. Only add certificates from sources you fully trust. Closes #105. Contributed by @ffminus
- Added the option to allow unencrypted connections (HTTP) for the Readeck server URL. This option is disabled by default and requires explicit user consent via a checkbox in the account settings.  This allows users to connect to servers that do not have HTTPS enabled, but it is strongly discouraged for security reasons. Closes #98.

## [0.5.0] - 2025-05-30

### Added

- Added the ability to select the theme in the settings. The theme selection is now also considered when displaying content in the bookmark details. Dynamic changing of the dark mode when using the system theme is also supported. Closes #77
- Added the ability to open original url in browser. Closes #74. Contributed by @sockenklaus
- Added the ability to share links to bookmarks from list view and datail view. Closes #45. Contributed by @sockenklaus

### Changed

- Show placeholder images on image load failure in main list view. Closes #81
- Allow cleartext traffic for tor onion services. Closes #92

## [0.4.0] - 2025-05-21

### Added

- Implemented background synchronization of bookmarks. The app now automatically synchronizes with the Readeck server in the background to detect and remove bookmarks that have been deleted on the server. This ensures that the local bookmark list remains consistent with the server.
- Added translation for Spanish by @xmgz
- Added icons to navigation drawer by @sockenklaus

### Fixed

- Fix #64: Add library definitions to version control.
- Fix #66: Show bookmark detail view for all bookmark, even if no article content is available.

## [0.3.2] - 2025-04-28

### Fixed

- Fix #54: Persist article content in separate database table. Also improves performance.

### Changed

- Disabled baseline profile to allow reproducible builds for F-Droid

## [0.3.1] - 2025-04-15

### Added

- Added metadata for F-Droid builds

### Fixed

- Fix #53: Show bookmarks of type `photo` in detail view

### Changed

- Readeck now displays notifications when authentication fails. These notifications allow users to quickly navigate to the account screen to verify their credentials and log in again. This feature assists users in addressing token-related issues that may arise when upgrading to Readeck 1.8.0, as outlined in the breaking changes documentation (https://readeck.org/en/blog/202503-readeck-18/#breaking-changes).

## [0.3.0] - 2025-04-11

### Added

- Implemented the ability to delete bookmarks in list and detail screen. Closes #44
- Implemented the ability to change the read state of bookmarks in list and detail screen. Closes #47
- Implemented the ability to change the archive state of bookmarks in list and detail screen. Closes #43
- Implemented the ability to change the favorite state of bookmarks in list and detail screen. Closes #39
- Implemented the ability to view application logs within the settings screen and share them for troubleshooting purposes.

### Fixed

- Fix #34: Fix parsing error. Make field `read_progress` optional.
- Fix #40: Bookmark synchronization issues caused by incorrect timezone handling.

### Changed

- Now only bookmarks that are successfully loaded (`state = 0` in readeck api) are displayed. Bookmarks that are still loading or have encountered an error will not be displayed. 

## [0.2.0] - 2025-03-25

### Added

- Implemented the ability to receive shared URLs from other apps, automatically opening the create bookmark dialog and pre-populating the URL field. Closes #25

### Fixed

- Fix #23: fix error in release workflow
- Fix #27: Enforce HTTPS and allow cleartext traffic only for ts.net including subdomains.
- Fix #29, #30: Make login workflow more robust 
- Fix #18: automatically append /api to base URL if missing in login workflow

## [0.1.0] - 2025-03-19

### Added

- Initial release of ReadeckApp.
- Implemented adding bookmarks.
- Implemented bookmark listing and detail screens.
- Implemented settings screen with account settings.
- Implemented authentication flow.
- Implemented data storage using Room database.
- Implemented dependency injection using Hilt.
- Implemented networking using Retrofit.
- Implemented MVVM architecture.

### Changed

- Initial implementation of the ReadeckApp.

### Deprecated

### Removed

### Fixed

### Security
