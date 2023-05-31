# Keyple Reload Demo

This is the repository for the Keyple Java Reload Demo application. 

This demo is an open source project provided by [Calypso Networks Association](https://calypsonet.org),
you can adapt the demo for your cards, terminals, projects, etc. 

This is a client/server demonstration of the 
[Eclipse Keyple Distributed Solution](https://keyple.org/docs/developer-guide/distributed-application/) 
feature where all ticketing procedures and sessions are fully managed by the server.

The [Eclipse Keyple](https://keyple.org) SDK is used to remotely initialize a card and reload a contract (Season Pass 
and/or Multi-trip ticket) on a Calypso contactless card using the 
[Keyple Plugin Android NFC | Eclipse Keyple](https://keyple.org/components-java/plugins/nfc/) 
plugin.

Alternatively, there's also a client (written in C#) that shows how an application can interact with the server without 
using the Keyple SDK.

## Keyple Demos

This demo is part of a set of three demos:
* [Keyple Reload Demo](https://github.com/calypsonet/keyple-java-demo-remote)
* [Keyple Validation Demo](https://github.com/calypsonet/keyple-android-demo-validation)
* [Keyple Control Demo](https://github.com/calypsonet/keyple-android-demo-control)

These demos are all based on a common library that defines elements such as constants and data structures implemented
for the logic of the ticketing application: 
[Keyple Demo Common Library](https://github.com/calypsonet/keyple-demo-common-lib).

Please refer to the [README](https://github.com/calypsonet/keyple-demo-common-lib/blob/main/README.md)
file of this library to discover these data structures.

## Code organization

This repository is organized in 3 source code folders:

- **server**: source code of the server part of the demo. The sever should run on a computer with a JVM and have a card reader with a SAM connected.
- **client/android**: source code of the client part of the demo as an Android project.
- **client/dotnet**: source code of the client part of the demo not using the Keyple SDK as Microsoft Visual Studio project.

## Project's details

Each application's code source contains a `README` file, please refer to this document for more information.

## Card Issuance / Personalization and Distribution / Loading Procedures

### Card Issuance / Personalization Use Case

This use case prepares the cards to be used for the following use cases by setting the appropriate values for the data
model fields of the Environment Record and making sure that the other relevant files are set to 0s (i.e. Events, 
Contracts and Counters).

Steps:
1. Detection and Selection
2. Clean / Initialize application

### Card Issuance / Personalization Process

For this demo, a simple example card issuance / personalization procedure has been implemented. 

Opening a Calypso secure session is mandatory for this procedure since we need to write on the card.

This procedure's main steps are as follows:
- Detection and Selection  
  - Detection Analysis:
    - Perform the Application Selection using the AID defined for the demo cards
    - If AID not found reject the card
    - If the status of the DF is invalid reject the card. <Exit process>
  - Selection Analysis:
    - If File Structure is unknown reject the card. <Exit process>
- Clean/Initialize Application
  - Open an Issuer session without reading any information.
  - Fill the environment structure, pack it into a byte array and update the environment record:
    - `EnvVersionNumber` = 1
    - `EnvApplicationNumber` = GlobalNumber++
    - `EnvIssuingDate` = Current Date converted to `DateCompact`
    - `EnvEndDate` = (1st day of the month of Current Date + 6 years) converted to DateCompact
    - `HolderCompany`, `HolderIdNumber` and `EnvPadding` all filled to 0.
  - Clear the first event (update with a byte array filled with 0s).
  - Clear all contracts (update with a byte array filled with 0s).
  - Clear the counter file (update with a byte array filled with 0s).
  - Close the session.
  - Return the status of the operation to the upper layer. <Exit process>

### Distribution / Loading Use Case

This use case loads a new transport title, or reloads/extends an existing contract, while updating the contract priority
levels if necessary.

Transport Titles can be period duration in which they are valid for a specific period or have an associated counter that
will represent either Trips or Stored Value (depending on the configuration of the contract).

This use case must also contemplate the option of loading the contracts with traceability mode (thus adding an 
authenticator to the end of the file). During the Contract Analysis phase the authenticator needs to be checked.

Contracts will be loaded first into blank records and then, when it is not a reload, the expired contract with the 
lowest index will be replaced.

Steps:
1. Detection and Selection
2. Contract Analysis
3. Write new contract/extend existing one
4. Update contract priority value (if necessary)

### Distribution / Loading Process

For this demo, a simple example card issuance / personalization procedure has been implemented. 

Opening a Calypso secure session is mandatory for this procedure since we need to write on the card.

This procedure's main steps are as follows:
- Detection and Selection
  - Detection Analysis:
    - If AID not found reject the card. <Exit process>
  - Selection Analysis:
    - If File Structure is unknown reject the card. <Exit process>
- Environment Analysis:
  - Open a Reload session reading the environment record.
  - Unpack environment structure from the binary present in the environment record.
    - If `EnvVersionNumber` of the Environment structure is not the expected one (==1 for the current version) reject the card. <Abort Transaction and exit process>
    - If `EnvEndDate` points to a date in the past reject the card. <Abort Transaction and exit process>
- Event Analysis:
  - Read and unpack the last event record. 
    - If `EventVersionNumber` is not the expected one (==0 for clean card or ==1 for the current version) reject the card. <Abort Transaction and exit process>
  - Store the `ContractPriority` fields in a persistent object with a flag to control any priority change set to false.
- Contract Analysis:
  - For each contract:
    - Read and unpack the contract record for the index being iterated.
      - If `ContractVersionNumber` is 0 ensure that the associated `ContractPriority` field value is 0 and move on to the next contract.
      - If `ContractValidityEndDate` points to a date in the past update the associated `ContractPriorty` field present in the persistent object to 31 and set the change flag to true.
    - Add contract to the list of possible contracts to be reloaded.
    - Return the list with the contract information. <Exit process>

-----------------

**Wait for input from the user regarding choice of product to load**

-----------------

- Write new contract/extend existing one & Update Contract Priority Value:
  - Fill the contract structure to update:
    - `ContractVersionNumber` = 1.
    - `ContractTariff` = Value provided by upper layer.
    - `ContractSaleDate` = Current Date converted to `DateCompact`.
    - If the operation is a reload of the `ContractTariff` == 1,  set `ContractValidityEndDate` = original `ContractValidityEndDate` + 30
    - Else `ContractValidityEndDate` = (`ContractSaleDate` + 30 if `ContractTariff` == 1) or (`EnvEndDate` if `ContractTariff` == 2 or 3)
  - Pack the Contract structure to write into the contract record.
  - If the operation is a reload use the index of the original contract to update the information.
  - `ContractPriority` Analysis: 
    - If `ContractPriority` Value == `ContractTariff` skip the next points and go to CNT_UPD. (can be expired)
    - Else, Set `ContractPriority` Value = `ContractTariff`, set the flag to `true`.
  - If the operation is a new contract write (indicated by the layer above or when `ContractPriorty` Value != `ContractTariff`):
    - If there is one, or more, `ContractPriority` Field at 0 then the one with the lowest index will be the position of the contract to load. Set `ContractPriority` Value = `ContractTariff`, set the flag to true and go to CNT_UPD.
    - Else, if there is no `ContractPriority` Field at 0 then search for the first `ContractPriority` field at 31 and set that index as the one to load. Set `ContractPriority` Value = `ContractTariff`, set the flag to true and go to CNT_UPD.
    - Else (there are no expired nor empty positions) reject the card. <Abort Transaction and exit process>.
  - CNT_UPD: Update the contract record present in the appropriate index with the binary data.
  - If the `ContractTariff` == 2 or 3 then increment the counter with the value sent from the upper layer. 
  - If `ContractPriority` changed flag is true, fill the event structure to update:
   - `EventVersionNumber` = 1.
   - `EventDateStamp` = value read from previous event.
   - `EventTimeStamp` = value read from previous event.
   - `EventLocation` = value read from previous event.
   - `EventContractUsed` = value read from previous event.
   - `ContractPriority1` = Value of index 0 of `ContractPriority` persistent object.
   - `ContractPriority2` = Value of index 1 of `ContractPriority` persistent object.
   - `ContractPriority3` = Value of index 2 of `ContractPriority` persistent object.
   - `ContractPriority4` = Value of index 3 of `ContractPriority` persistent object.
   - `EventPadding` = 0.
  - Pack the Event structure and update the last event with that binary data.
  - Close the session.
  - Return the status of the operation to the upper layer. <Exit process>
