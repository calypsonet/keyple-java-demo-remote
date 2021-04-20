# keyple-java-demo-remote

This is the repository for the 'Eclipse Keyple' a client/server demonstration of Keyple [distribued solution](https://keyple.org/docs/developer-guide/distributed-application/)feature.

This demo provides SAM secured Calypso container's reading as well as loading contracts with an Android application.

This demo supports following containers:

**CD LIGHT/GTML**
**NAVIGO 2013 (soon)**

This demo can can use the following readers:

**Android Native NCF reader** for contactless Card.
**Android Native OMAPI** for Contact Card. (soon)
**eSE** for internal secure element, powered by Wizway Solutions. (soon)

## Code organization

This repository is organized in 3 source code folders:

- **server**: source code of the server part of the demo. The sever should run on a computer with a JVM and have a card reader with a SAM connected.
- **client**: content clients available to consume server's webservices. 
- **common**: code library shared by the client/server for communication.

## Project's details

Each application's code source contains a README, please refer to this document for more informations.