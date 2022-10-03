# Keyple Reload Demo - Android Client's repository

This is the repository for the Android Client of the Keyple Reload Demo application.

This demo is an open source project provided by the [Calypso Networks Association](https://calypsonet.org) implementing
the [Eclipse Keyple SDK](https://keyple.org) in a typical use case that can serve as a basis for building a ticketing
ecosystem based on contactless cards and/or NFC smartphones.

The source code and APK are available at  [calypsonet/keyple-java-demo-remote/releases](https://github.com/calypsonet/keyple-java-demo-remote/releases)

The code can be easily adapted to other cards, terminals and business logic.

It shows how to load contracts into a Calypso card, the whole ticketing process being managed remotely.
Following the contract loading the card can pay presented to a validator running the 
[Keyple Demo Validation](https://github.com/calypsonet/keyple-android-demo-validation) application and then checked with
the [Keyple Demo Control](https://github.com/calypsonet/keyple-android-demo-control) application.

Read the main [README](https://github.com/calypsonet/keyple-java-demo-remote#readme) to understand the purpose of this
application.

## Screens

- Main screen (`MainActivity`): Setup Screen.
- Home (`HomeActivity`): Display a menu allowing to choose the Calypso card type to read and load.
    - 'Contactless support': works with the native Android NFC reader and is available for any android smartphone.
    - 'SIM Card': works with the native Android OMAPI reader and is available for any android smartphone. (Work in progress)
    - 'Embedded Secure Element': works with the Wizway plugin to access to eSE. (Work in progress)
- Settings (`SettingsMenuActivity`):
    - Server (ServerSettingsActivity): Settings for server connexion.
    - Configuration (ConfigurationSettingsActivity): Activate/Deactivate each plugin availability in the android smartphone.
    - Personalization (PersonalizationActivity): Reset a card (clean contracts).
- Card Reader (`CardReaderActivity`): Launches the flavour associated Keyple plugin. It will try to read Card using selected card reader.
    - Initialize Keyple plugin regarding selected Calypso card type.
    - Connect to remote server.
    - With remote server connected to a Calypso SAM, proceed to a secured reading of card content.
- Card Summary (`CardSummaryActivity`): displays the card content.
    - Card content can be season pass and/or multi-trip ticket.
- Select Tickets (`SelectTicketsActivity`): The remote server will returns a list of available products (Season Pass and Multi-trip ticket) to buy for this card. This list presented in this view.
- Checkout (`CheckoutActivity`): Simulates a payment done with a credit card.
- Payment Validated (`PayementValidatedActivity`): Simulates the payment validation.
- Charge (`ChargeActivity`): Presents the process of loading the product selected.
    - Block loading if card has been swapped.
    - Initialize Keyple plugin regarding selected Calypso card type.
    - Connect to remote server.
    - With remote server connected to a SAM, proceed to a card writing.
- Charge Result (`ChargeResultActivity`): Displays if loading was successful or failed.