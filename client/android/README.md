# Keyple Remote Reload Demo - Android Client's repository

This is the repository for the Android Client of the Keyple Remote Reload Demo application.

Read the main [README](https://github.com/calypsonet/keyple-java-demo-remote#readme) to understand the purpose of this application.

## Screens

- SplashScreen (SplashScreenActivity): Setup Screen.
- Home (HomeActivity): Display a menu allowing to choose the Calypso card type to read and load.
    - 'Contactless support': works with the native Android NFC reader and is available for any android smartphone.
    - 'SIM Card': works with the native Android OMAPI reader and is available for any android smartphone. (Work in progress)
    - 'Embedded Secure Element': works with the Wizway plugin to access to eSE. (Work in progress)
- Settings (SettingsMenuActivity):
    - Server (ServerSettingsActivity): Settings for server connexion.
    - Configuration (ConfigurationSettingsActivity): Activate/Deactivate each plugin availability in the android smartphone.
    - Personalization (PersonalizationActivity): Reset a card (clean contracts).
- Card Reader (CardReaderActivity): Launches the flavour associated Keyple plugin. It will try to read Card using selected card reader.
    - Initialize Keyple plugin regarding selected Calypso card type.
    - Connect to remote server.
    - With remote server connected to a Calypso SAM, proceed to a secured reading of card content.
- Card Summary (CardSummaryActivity): displays the card content.
    - Card content can be season pass and/or multi-trip ticket.
- Select Tickets (SelectTicketsActivity): The remote server will returns a list of available products (Season Pass and Multi-trip ticket) to buy for this card. This list presented in this view.
- Checkout (CheckoutActivity): Simulates a payment done with a credit card.
- Payment Validated (PayementValidatedActivity): Simulates the payment validation.
- Charge (ChargeActivity): Presents the process of loading the product selected.
    - Block loading if card has been swapped.
    - Initialize Keyple plugin regarding selected Calypso card type.
    - Connect to remote server.
    - With remote server connected to a SAM, proceed to a card writing.
- Charge Result (ChargeResultActivity): Displays if loading was successful or failed.

## Project Dependencies

Android Client
- /common: Contains components shared between server side and client side of this demo.
```groovy
    implementation project(":common")
```

## Dependencies

The demo needs multiple dependencies to work.

First we need to import the Keyple related dependencies in the `build.gradle` file:

```groovy
    implementation "org.calypsonet.terminal:calypsonet-terminal-reader-java-api:1.0.+"
    implementation "org.calypsonet.terminal:calypsonet-terminal-calypso-java-api:1.0.+"

    implementation "org.eclipse.keyple:keyple-service-java-lib:2.0.+"
    implementation "org.eclipse.keyple:keyple-card-calypso-java-lib:2.0.+"
    implementation "org.eclipse.keyple:keyple-card-generic-java-lib:2.0.+"
    implementation "org.eclipse.keyple:keyple-util-java-lib:2.0.+"
    implementation "org.eclipse.keyple:keyple-common-java-api:2.0.+"

    implementation "org.eclipse.keyple:keyple-distributed-local-java-lib:2.0.+"
    implementation "org.eclipse.keyple:keyple-distributed-network-java-lib:2.0.+"

    implementation "org.eclipse.keyple:keyple-plugin-java-api:2.0.+"

```

## Keyple plugins 

This demo uses two keyple plugins

The [Android NFC plugin](https://github.com/eclipse/keyple-plugin-android-nfc-java-lib) allowing to
process a contactless SmartCard.
The The [Android OMAPI plugin](https://github.com/eclipse/keyple-plugin-android-omapi-java-lib) 
allowing to process a contact SmartCard.

```groovy
    implementation "org.eclipse.keyple:keyple-plugin-android-nfc-java-lib:2.0.+"
    implementation "org.eclipse.keyple:keyple-plugin-android-omapi-java-lib:2.0.+"
```

## Sign application

In order to access to SIM card content you must own a specific SIM card and sign the application with related keystore.
Otherwise you'll encounter a security exception.

Within the release package, 2 APKs are provided:
* keyple-demo-remote-client_XXXX.XX: Signed with CNA Keystore. At time no compatible SIM are available.
* keyple-demo-remote-client_XXXX.XX-ORANGE: Signed with Orange Keystore, compatible pre-production SIMs provided by Orange.

## Use Wizway Solutions platform

In order to use the eSE, this demo relies on Wizway Platform.
Wizway Solutions provides an application to install on the device (called the Agent) which will handle operations opening access to the secure element (can also be used with the SIM card) and allowing to exchange
data with it.
To create a connection with this agent, Wizway Solutions provides an android library.
Once the library and agent are correctly initialized, Wizway Service Instances are provided to Keyple Wizway Plugin and the Android application can benefit from Keyple toolkit.

Please refer to [Keyple Android Wizway Plugin](https://github.com/calypsonet/keyple-android-plugin-wizway/) and [Wizway Solutions](https://www.wizwaysolutions.com) for more information.
