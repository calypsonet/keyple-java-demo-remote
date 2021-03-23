# Keyple Java Demo Remote - Android Client's repository

This is the repository for the 'Eclipse Keyple' Android Remote Client control application.

It implements simultaneously multiple plugins for handling mutiple multiple readers of the device :

* [NFC Reader](https://github.com/calypsonet/keyple-android-plugin-nfc/)
* [Sim card reader](https://github.com/calypsonet/keyple-android-plugin-omapi/)
* [eSE (powered by Wizway)](https://github.com/calypsonet/keyple-android-plugin-wizway/)

## Screens

- SplashScreen (SplashScreenActivity) Setup Screen
- Home (HomeActivity): Display a menu allowing to chose the Card reader to user.
    - 'Contactless support': uses native Android NFC reader and is available for any android smartphone.
    - 'SIM Card': uses native Android Sim card reader and is available for any android smartphone. (Work in progress)
    - 'Embedded Secure Element': Uses Wizway Solution to access to eSE. (Work in progress)     
- Settings (SettingsMenuActivity)
    - Server (ServerSettingsActivity): Settings for server connexion.
    - Configuration (ConfigurationSettingsActivity): Activate/Deactivate each reader availability.
    - Personalization (PersonalizationActivity): Reset a card (clean contracts).
- Card Reader (CardReaderActivity): Will try to read Card using selected card reader
    - Initialize Keyple plugin regarding selected Reader.
    - Connect to remote server
    - With remote server connected to a SAM, proceed to a secured reading of card content.
- Card Summary (CardSummaryActivity): Will present the read content of the Card.
    - Card content can be one way tickets or season pass.
- Select Tickets (Select Tickets Activity): The remote server will returns a list of available tickets to buy for this Card. This list presented in this view.
- Checkout (CheckoutActivity): This screen siumulate a payment done with a credit card
- Charge (ChargeActivity): This screen presents the process of loading the build ticket.
    - Block loading if card has been swapped
    - Initialize Keyple plugin regarding selected Reader.
    - Connect to remote server
    - With remote server connected to a SAM, proceed to a Card writing.

## Features
Associated with a running instance of the server application within the same git repository, 
This application demonstrates:

* Secured reading of a ticketing application
* Loading of titles in a ticketing application
* Resets a ticketing application (clear titles, only for demo purpose)

A reader with a SAM connected to device running the server is, for now, mandatory.

## Calypso applications
This demo evolves to provide wide Calypso applet support. For now this demo can support:

* CD LIGHT / GMTL: 315449432E49434131
* (Work in progress) Hoplink: A000000291A000000191
* (Work in progress) Navigo2013: A00000040401250901

## Dependencies

Android Client
- /common: Contains components shared between server side and client side of this demo. 

## Sign application

In order to access to SIM card content you must own a specific simcard and sign the application with related
keystore. Otherwise you'll encounter a security exception.

## Use wizway platform

In order to use eSE, this demo relies on Wizway Platform. 
Wizway provides an application to install on the device (called the Agent) which will handle 
operations opening access to the secure element (can also be used with the SIM card) and allowing to exchange
data with it.
To create a connection with this agent, Wizway provides an android library. 
Once the library and agent are correctly initialized, Wizway Service Instances are provided to
Keyple Wizway Plugin and the Android application can benefit from Keyple toolkit.

Please refer to [Keyple Android Wizway Plugin](https://github.com/calypsonet/keyple-android-plugin-wizway/) and
[Wizway Solutions](https://www.wizwaysolutions.com) for more informations.

## Ticketing implementation

Ticketing procedures and session's management are handled by server side.

This client provides remote PO readers to the server

All Ticketing process is server side executed (reading/loading/personalization)

## Keyple Manager

