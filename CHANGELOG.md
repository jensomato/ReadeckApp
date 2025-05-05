# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- Fix #64: Add library definitions to version control.

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
