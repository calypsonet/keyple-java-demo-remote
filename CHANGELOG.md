# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]
### Upgraded
- Calypsonet Terminal Reader API `1.3.0` -> Keypop Reader API `2.0.0`
- Calypsonet Terminal Calypso API `1.8.0` -> Keypop Calypso Card API `2.0.0`
- Keyple Service Library `2.3.1` -> `3.0.0`
- Keyple Service Resource Library `2.1.1` -> `3.0.0`
- Keyple Calypso Card Library `2.3.5` -> `3.0.0`
- Keyple Util Library `2.3.0` -> `2.3.1`
- Keyple Distributed Network Library `2.2.0` -> `2.3.0`
- Keyple Distributed Remote Library `2.2.1` -> `2.3.0`

### Added
New dependencies
- Keypop Crypto Legacy SAM API `0.3.0`
- Keyple Calypso Crypto LegacySAM Library `0.4.0`

## [2023.05.31]
### Added
- Added a new C# application to demonstrate the use of the **Keyple Distributed JSON API** inside the `client/dotnet` folder.
### Upgraded
- `keyple-demo-common-lib:2.0.0-SNAPSHOT`
- `calypsonet-terminal-reader-java-api:1.3.0`
- `calypsonet-terminal-calypso-java-api:1.8.0`
- `keyple-service-java-lib:2.3.1`
- `keyple-service-resource-java-lib:2.1.1`
- `keyple-distributed-network-java-lib:2.2.0`
- `keyple-distributed-remote-java-lib:2.2.1`
- `keyple-card-calypso-java-lib:2.3.5`
- `keyple-plugin-pcsc-java-lib:2.1.2`

## [2023.03.03]
### Fixed
- Physical channel management.
- Missing Keyple generic AID.
- Calypso Basic products management.

## [2023.02.24]
### Upgraded
- `calypsonet-terminal-reader-java-api:1.2.0`
- `calypsonet-terminal-calypso-java-api:1.6.0`
- `keyple-service-java-lib:2.1.3`
- `keyple-card-calypso-java-lib:2.3.2`
- `keyple-distributed-remote-java-lib:2.1.0`
- `com.google.code.gson:gson:2.10.1`

## [2022.11.18]
### Fixed
- Various erroneous behaviors and displays.
### Added
- CI: `java-test` GitHub action.
### Changed
- Major refactoring of the source code.
### Upgraded
- `keyple-demo-common-lib:1.0.0-SNAPSHOT`
- `calypsonet-terminal-reader-java-api:1.1.0`
- `calypsonet-terminal-calypso-java-api:1.4.1`
- `keyple-service-java-lib:2.1.1`
- `keyple-service-resource-java-lib:2.0.2`
- `keyple-card-calypso-java-lib:2.2.5`
- `keyple-plugin-android-nfc-java-lib:2.0.1`
- `keyple-plugin-android-omapi-java-lib:2.0.1`
- `keyple-util-java-lib:2.3.0`
  
[Unreleased]: https://github.com/calypsonet/keyple-java-demo-remote/compare/2023.05.31...HEAD
[2023.05.31]: https://github.com/calypsonet/keyple-java-demo-remote/compare/2023.03.03...2023.05.31
[2023.03.03]: https://github.com/calypsonet/keyple-java-demo-remote/compare/2023.02.24...2023.03.03
[2023.02.24]: https://github.com/calypsonet/keyple-java-demo-remote/compare/2022.11.18...2023.02.24
[2022.11.18]: https://github.com/calypsonet/keyple-java-demo-remote/compare/v2021.11...2022.11.18