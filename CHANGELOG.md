# Changelog

All notable changes to this project are documented here.

## [2.0.0] - 2026-09-06

### Added

- Real WireGuard tunnel activation through the official Android tunnel library
- All-app, allowlist, and denylist routing modes
- Searchable app selector with user and system app filters
- Manual package-name entry
- Controlled tunnel re-establishment when routing policy changes
- Animated Jetpack Compose connection dashboard
- Explicit VPN connection state machine
- Live upload and download totals
- Ongoing foreground-service notification
- Android package-visibility declaration without `QUERY_ALL_PACKAGES`
- Unit tests and GitHub Actions checks
- Architecture, testing, release, policy, and article documentation

### Changed

- Replaced the legacy TUN-only demonstration with a working WireGuard client architecture
- Moved configuration parsing and tunnel control out of the UI layer
- Kept imported credentials in process memory and disabled always-on support accordingly

### Security

- No real client keys or endpoint credentials are included
- Application backup and cleartext application traffic are disabled
- Configuration, private keys, and packet contents are not logged
