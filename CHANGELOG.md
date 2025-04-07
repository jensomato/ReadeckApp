# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Implemented the ability to change the archive state of bookmarks in list and detail screen. Closes #43
- Implemented the ability to change the favorite state of bookmarks in list and detail screen. Closes #39
- Implemented the ability to view application logs within the settings screen and share them for troubleshooting purposes.

### Fixed

- Fix #34: Fix parsing error. Make field `read_progress` optional.
- Fix #40: Bookmark synchronization issues caused by incorrect timezone handling.

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
