# Keyple Remote Demo

This is the repository for the Keyple Java Remote Demo application. 

This demo is an open source project provided by [Calypso Networks Association](https://calypsonet.org), you can adapt the demo for your cards, terminals, projects, etc. 

This demo is a client/server demonstration of [Eclipse Keyple Distributed Solution](https://keyple.org/docs/developer-guide/distributed-application/) feature.

This demo shows how to easily reload a contract (Season Pass and/or Multi-trip ticket) on a Calypso card using the [Eclipse Keyple](https://keyple.org) components.
And also to initialize the Calypso card for use by the Keyple demos. 

It implements simultaneously multiple plugins for handling multiple solutions of the device:
- [Keyple Plugin Android NFC | Eclipse Keyple](https://keyple.org/components-java/plugins/nfc/) for use with contactless cards
- (Work in progress) [Keyple Plugin OMAPI | Eclipse Keyple](https://keyple.org/components-java/plugins/omapi/) for use with Contact cards.
- (Work in progress) [Wizway plugin](https://github.com/calypsonet/keyple-android-plugin-wizway/) for use with eSE powered by Wizway Solutions.

The source code and APK for several solutions are available at [calypsonet/keyple-java-demo-remote/releases](https://github.com/calypsonet/keyple-java-demo-remote/releases)

## Keyple Demos

This demo is part of a set of three demos:
* [Keyple Remote Demo](https://github.com/calypsonet/keyple-java-demo-remote)
* [Keyple Validation Demo](https://github.com/calypsonet/keyple-android-demo-validation)
* [Keyple Control Demo](https://github.com/calypsonet/keyple-android-demo-control)

## Calypso Card Applications

The demo works with the cards provided in the [Test kit](https://calypsonet.org/technical-support-documentation/)

This demo can be used with Calypso cards with the following configurations:
* AID 315449432E49434131h - File Structure 05h (CD Light/GTML Compatibility)
* (Work in progress) AID 315449432E49434133h - File Structure 32h (Calypso Light Classic)
* (Work in progress) AID A0000004040125090101h - File Structure 05h (CD Light/GTML Compatibility)

## Code organization

This repository is organized in 3 source code folders:

- **server**: source code of the server part of the demo. The sever should run on a computer with a JVM and have a card reader with a SAM connected.
- **client**: source code of the client part of the demo. Content clients available to consume server's webservices. 
- **common**: code library shared by the client/server for communication.

## Project's details

Each application's code source contains a README, please refer to this document for more information.

## Card Issuance / Personalization and Distribution / Loading Procedures

### Data Structures

#### Environment/Holder structure
            
| Field Name           | Bits| Description                                        | Type          | Status    |
| :------------------- | ---:| :------------------------------------------------- | :-----------: | :-------: |
| EnvVersionNumber     |    8| Data structure version number                      | VersionNumber | Mandatory | 
| EnvApplicationNumber |  32 | Card application number (unique system identifier) | Int           | Mandatory |
| EnvIssuingDate       |  16 | Card application issuing date                      | DateCompact   | Mandatory | 
| EnvEndDate           |  16 | Card application expiration date                   | DateCompact   | Mandatory | 
| HolderCompany        |   8 | Holder company                                     | Int           | Optional  | 
| HolderIdNumber       |  32 | Holder Identifier within HolderCompany             | Int           | Optional  | 
| EnvPadding           | 120 | Padding (bits to 0)                                | Binary        | Optional  | 
            
#### Event structure            

| Field Name         | Bits| Description                                   | Type          | Status    |
| :----------------- | ---:| :-------------------------------------------- | :-----------: | :-------: |
| EventVersionNumber |   8 | Data structure version number                 | VersionNumber | Mandatory | 
| EventDateStamp     |  16 | Date of the event                             | DateCompact   | Mandatory | 
| EventTimeStamp     |  16 | Time of the event                             | TimeCompact   | Mandatory | 
| EventLocation      |  32 | Location identifier                           | Int           | Mandatory | 
| EventContractUsed  |   8 | Index of the contract used for the validation | Int           | Mandatory | 
| ContractPriority1  |   8 | Priority for contract #1                      | PriorityCode  | Mandatory | 
| ContractPriority2  |   8 | Priority for contract #2                      | PriorityCode  | Mandatory | 
| ContractPriority3  |   8 | Priority for contract #3                      | PriorityCode  | Mandatory | 
| ContractPriority4  |   8 | Priority for contract #4                      | PriorityCode  | Mandatory | 
| EventPadding       | 120 | Padding (bits to 0)                           | Binary        | Optional  | 
            
#### Contract structure             

| Field Name              | Bits| Description                          | Type                | Status    |
| :---------------------- | ---:| :----------------------------------- | :-----------------: | :-------: |
| ContractVersionNumber      |   8 | Data structure version number        | VersionNumber       | Mandatory | 
| ContractTariff         |   8 | Contract Type                        | PriorityCode        | Mandatory | 
| ContractSaleDate        |  16 | Sale date of the contract            | DateCompact         | Mandatory | 
| ContractValidityEndDate |  16 | Last day of validity of the contract | DateCompact         | Mandatory | 
| ContractSaleSam         |  32 | SAM which loaded the contract        | Int                 | Optional  | 
| ContractSaleCounter     |  24 | SAM auth key counter value           | Int                 | Optional  | 
| ContractAuthKvc         |   8 | SAM auth key KVC                     | Int                 | Optional  | 
| ContractAuthenticator   |  24 | Security authenticator               | Authenticator (Int) | Optional  | 
| ContractPadding         |  96 | Padding (bits to 0)                  | Binary              | Optional  | 
            
#### Counter structure          

| Field Name   | Bits| Description     | Type  | Status    |
| :----------- | ---:| :-------------- | :---: | :-------: |
| CounterValue |  24 | Number of trips | Int   | Mandatory | 

### Data Types

| Name         |Bits| Description             |
| :----------- |---:|:------------------------|    
|DateCompact   | 16 | Number of days since January 1st, 2010 (being date 0). Maximum value is 16,383, last complete year being 2053. All dates are in legal local time.|   
|PriorityCode  |  8 | Types of contracts defined: <br>0 Forbidden (present in clean records only)<br>1 Season Pass<br>2 Multi-trip ticket<br>3 Stored Value<br>4 to 30 RFU<br>31 Expired|
|TimeCompact   | 16 | Time in minutes, value = hour*60+minute (0 to 1,439)|    
|VersionNumber |  8 | Data model version:<br>0 Forbidden (undefined)<br>1 Current version<br>2..254 RFU<br>255 Forbidden (reserved)|

### Card Issuance / Personalization Use Case

This use case prepares the cards to be used for the following use cases by setting the appropriate values for the data model fields of the Environment Record and making sure that the other relevant files are set to 0s (i.e. Events, Contracts and Counters).

Steps:
1. Detection and Selection
2. Clean / Initialize application

### Card Issuance / Personalization Process
For this demo, a simple example card issuance / personalization procedure has been implemented. 
This procedure is implemented in the 'XXXX' class.

Opening a Calypso secure session is mandatory for this procedure since we need to write on the card.

This procedure's main steps are as follows:
- Detection and Selection  
  - Detection Analysis:
    - Perform the Application Selection using the AID defined for the demo cards
    - If AID not found reject the card
    - If the status of the DF is invalid reject the card. <Exit process>
  - Selection Analysis:
    - If File Structure unknow reject the card. <Exit process>
- Clean/Initialize Application
  - Open an Issuer session without reading any information.
  - Fill the environment structure, pack it into a byte array and update the environment record:
    - EnvVersionNumber = 1
    - EnvApplicationNumber = GlobalNumber++
    - EnvIssuingDate = Current Date converted to DateCompact
    - EnvEndDate = (1st day of the month of Current Date + 6 years) converted to DateCompact
    - HolderCompany, HolderIdNumber and EnvPadding all filled to 0.
  - Clear the first event (update with a byte array filled with 0s).
  - Clear all contracts (update with a byte array filled with 0s).
  - Clear the counter file (update with a byte array filled with 0s).
  - Close the session.
  - Return the status of the operation to the upper layer. <Exit process>

### Distribution / Loading Use Case

This use case loads a new transport title, or reloads/extends an existing contract, while updating the contract priority levels if necessary.

Transport Titles can be period duration in which they are valid for a specific period or have an associated counter that will represent either Trips or Stored Value (depending on the configuration of the contract).

This use case must also contemplate the option of loading the contracts with traceability mode (thus adding an authenticator to the end of the file). During the Contract Analysis phase the authenticator needs to be checked.

Contracts will be loaded first into blank records and then, when it is not a reload, the expired contract with the lowest index will be replaced."

Steps:
1. Detection and Selection
2. Contract Analysis
3. Write new contract/extend existing one
4. Update contract priority value (if necessary)

### Distribution / Loading Process
For this demo, a simple example card issuance / personalization procedure has been implemented. 
This procedure is implemented in the 'XXXX' class.

Opening a Calypso secure session is mandatory for this procedure since we need to write on the card.

This procedure's main steps are as follows:
- Detection and Selection
  - Detection Analysis:
    - If AID not found reject the card. <Exit process>
  - Selection Analysis:
    - If File Structure unknow reject the card. <Exit process>
- Environment Analysis:
  - Open a Reload session reading the environment record.
  - Unpack environment structure from the binary present in the environment record.
    - If EnvVersionNumber of the Environment structure is not the expected one (==1 for the current version) reject the card. <Abort Transaction and exit process>
    - If EnvEndDate points to a date in the past reject the card. <Abort Transaction and exit process>
- Event Analysis:
  - Read and unpack the last event record. 
    - If EventVersionNumber is not the expected one (==0 for clean card or ==1 for the current version) reject the card. <Abort Transaction and exit process>
  - Store the ContractPriority fields in a persistent object with a flag to control any priority change set to false.
- Contract Analysis:
  - For each contract:
    - Read and unpack the contract record for the index being iterated.
      - If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is 0 and move on to the next contract.
      - If ContractValidityEndDate points to a date in the past update the associated ContractPriorty field present in the persistent object to 31 and set the change flag to true.
    - Add contract to the list of possible contracts to be reloaded.
    - Return the list with the contract information. <Exit process>

-----------------

**Wait for input from the user regarding choice of product to load**

-----------------

- Write new contract/extend existing one & Update Contract Priority Value:
  - Fill the contract structure to update:
    - ContractVersionNumber = 1.
    - ContractTariff = Value provided by upper layer.
    - ContractSaleDate = Current Date converted to DateCompact.
    - If the operation is a reload of the ContractTariff ==1,  set ContractValidityEndDate = original ContractValidityEndDate + 30
    - Else ContractValidityEndDate = (ContractSaleDate + 30 if ContractTariff == 1) or (EnvEndDate if ContractTariff == 2 or 3)
  - Pack the Contract structure to write into the contract record.
  - If the operation is a reload use the index of the original contract to update the information.
  - ContractPriority Analysis: 
    - If ContractPriority Value == ContractTariff skip the next points and go to CNT_UPD. (can be expired)
    - Else, Set ContractPriority Value = ContractTariff, set the flag to true.
  - If the operation is a new contract write (indicated by the layer above or when ContractPriorty Value != ContractTariff):
    - If there is one, or more, ContractPriority Field at 0 then the one with the lowest index will be the position of the contract to load. Set ContractPriority Value = ContractTariff, set the flag to true and go to CNT_UPD.
    - Else, if there is no ContractPriority Field at 0 then search for the first ContractPriority field at 31 and set that index as the one to load. Set ContractPriority Value = ContractTariff, set the flag to true and go to CNT_UPD.
    - Else (there are no expired nor empty positions) reject the card. <Abort Transaction and exit process>.
  - CNT_UPD: Update the contract record present in the appropriate index with the binary data.
  - If the ContractTariff == 2 or 3 then increment the counter with the value sent from the upper layer. 
  - If ContractPriority changed flag is true, fill the event structure to update:
   - EventVersionNumber = 1.
   - EventDateStamp = value read from previous event.
   - EventTimeStamp = value read from previous event.
   - EventLocation = value read from previous event.
   - EventContractUsed = value read from previous event.
   - ContractPriority1 = Value of index 0 of ContractPriority persistent object.
   - ContractPriority2 = Value of index 1 of ContractPriority persistent object.
   - ContractPriority3 = Value of index 2 of ContractPriority persistent object.
   - ContractPriority4 = Value of index 3 of ContractPriority persistent object.
   - EventPadding = 0.
  - Pack the Event structure and update the last event with that binary data.
  - Close the session.
  - Return the status of the operation to the upper layer. <Exit process>

## Ticketing implementation

Ticketing procedures and session's management are handled by server.

The client provides remote card readers to the server.

All Ticketing process is server side executed (reading/loading/personalization).
