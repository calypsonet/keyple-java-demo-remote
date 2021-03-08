# Keyple Java Demo Remote - Android Client's repository

This source code belongs to the Android client of Java demo remote.  

## Features
Associated with a running instance of the server application within the same git repository, 
This application demonstrates:

* Secured reading of a ticketing application
* Loading of titles in a ticketing application
* Reseting a ticketing application (clear titles, only for demo purpose)

## Keyple plugins
To interact with applets, this demo relies on following Android's Keyple plugins:

* 'keyple-android-plugin-nfc': Used when 'Contactless support' is chosen at demo's launch. This plugin uses native Android
NFC reader's library and is available for any android smartphone.

* 'keyple-android-plugin-omapi': Used when 'SIM Card' is chosen at demo's launch. This plugin uses native Android
OMAPI reader's library and is available for any android smartphone.

* (Work in progress) 'keyple-android-plugin-wizway': Used when 'Embedded Secure Element' is chosen at demo's launch. 
This plugin uses Wizway platform to provide access to the eSE.

## Calypso applications
This demo evolves to provide wide Calypso applet support. For now this demo can support:

* CD LIGHT / GMTL
* (Work in progress) Hoplink
* (Work in progress) Navigo2013


## Dependencies

Android Client
- /common: Contains components share between server side and client side of this demo. 

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