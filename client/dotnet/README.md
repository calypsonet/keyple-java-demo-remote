[![.NET](https://github.com/jeanpierrefortune/demo-keyple-less/actions/workflows/dotnet.yml/badge.svg)](https://github.com/jeanpierrefortune/demo-keyple-less/actions/workflows/dotnet.yml)

# demo-keyple-less

This is a C# application designed according to the Hexagonal Architecture pattern, 
demonstrating how to implement a client for a Keyple Distributed server without using the Keyple library, 
solely utilizing the Distributed JSON API as documented here: https://keyple.org/learn/user-guide/distributed-json-api-1-0/.

The Hexagonal Architecture pattern, creates a loosely coupled application for seamless integration 
with its software environment. This architecture effectively segregates the application into domain, 
, and infrastructure layers. The core business logic resides in the domain layer, while the outside layer encompasses 
all entities interacting with the application, such as databases, UI, and test scripts. 
The boundary layer orchestrates the communication between the inside and outside layers.

This architectural pattern's primary advantage is the isolation it provides to the application's core logic from 
external elements such as databases, web interfaces, or other communication systems. 
This isolation facilitates more straightforward testing, maintenance, and interface adaptation or future changes.

In this application, two main infrastructure components are a reader service to access the smart card and 
a server service to connect with the ticketing server.

In the 'domain' directory, you find the core business logic of the application divided into 'api', 'data', 'spi', and 'utils' subdirectories. 
'infrastructure' houses the concrete implementations of the reader service and server service. 
'application' is where the main application entry point is located.