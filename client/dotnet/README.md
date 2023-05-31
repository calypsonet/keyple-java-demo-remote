[![.NET](https://github.com/jeanpierrefortune/demo-keyple-less/actions/workflows/dotnet.yml/badge.svg)](https://github.com/jeanpierrefortune/demo-keyple-less/actions/workflows/dotnet.yml)

# Keyple Reload Demo - Client without Keyple SDK

This demo is an open source project provided by the [Calypso Networks Association](https://calypsonet.org) that
demonstrates how to develop a client connected to a ticketing Keyple-based server without using the Keyple SDK, but
only implementing the [Keyple Distributed JSON API](https://keyple.org/learn/user-guide/distributed-json-api-1-0/).

This **C#** project targets the **.NET** environment on **Windows**, but can serve as a model for the development of
any application using the **Keyple Distributed JSON API**, whatever the operating system or language.

## Features
- C# (.NET 7.0) for Windows.
- Command line interface.
- PC/SC card reader interface.
- Connected to the [Java Server Application](https://github.com/calypsonet/keyple-java-demo-remote/tree/main/server).

## Operations
### Prerequisites
1. Build the C# client application using **Microsoft Visual Studio 2022**.
2. Make sure a PC/SC reader is connected.
3. Have a pre-personalized card (use the [Android Client Application](https://github.com/calypsonet/keyple-java-demo-remote/tree/main/client/android)) with at least one of these AIDs:
   - `A000000291FF9101`: Keyple Generic
   - `315449432E49434131`: CD Light GTML
   - `315449432E49434133`: Calypso Light
   - `A0000004040125090101`: Normalized IDFM AID
4. Check the application configuration in `appsettings.json` file.
5. Launch the server (make sure a SAM is available).

### Steps
1. Launch the client application (`App.exe`).
2. Present the card, the available contracts are displayed in the console (if any).
3. Indicate the number of units to add to the contract counter (MULTI_TRIP contract).
4. Present the card again, the contract is updated.

## Design
The project's source code follows a hexagonal architecture and is structured into three main folders, each serving
a distinct purpose:
- `application`: contains the main application entry point.
- `domain`: contains the isolated core business logic of the application divided into the following subdirectories:
  - `api`: contains the interfaces implemented by the domain layer and dedicated to the application layer.
  - `spi`: contains the interfaces used by the domain layer and implemented by the infrastructure layer.
  - `data`: contains the objects representing the exchanged data between the terminal and the server.
  - `utils`: contains various utilities.
- `infrastructure`: contains the implementations of the SPIs (reader and server).
