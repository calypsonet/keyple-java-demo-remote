# Keyple Remote Demo - Java Server's repository

This is the repository for the Java Server of the Keyple Java Remote Demo application. 

Read the main [README](https://github.com/calypsonet/keyple-java-demo-remote#readme) to understand the purpose of the Keyple Java Remote Demo application. 

## Running the server from the executable

The server is packaged in an executable jar `keyple-demo-remote-server-vYYYY.MM.jar` which can be started with the command: 

```
java -jar keyple-demo-remote-server-vYYYY.MM.jar
```

By default the server starts only if a PCSC reader is connected. The PC/SC reader name should match the default filter (regex format) defined in the application.properties. 
``sam.pcsc.reader.filter=.*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*``

If you want to use a different filter, start the server with the parameter ``-Dsam.pcsc.reader.filter=XXX`` where XXX is a matching regex of your PC/SC reader.

For instance: 
```
>java -Dsam.pcsc.reader.filter="Identive CLOUD 2700 R Smart Card Reader.*" -jar keyple-demo-remote-server-vYYYY.MM.jar 
```
The command below starts the server with the PCSC reader "Identive CLOUD 2700 R Smart Card Reader"

```
>java -Dsam.pcsc.reader.filter=.* -jar keyple-demo-remote-server-vYYYY.MM.jar 
```
The command below starts the server with any PC/SC reader connected. Be aware that if multiple readers are connected, the server will select one of them randomly.


## Running the server from the source

This example is based on the Quarkus framework. To start the server, you need to install Quarkus dependencies: 
- JDK 1.8+ installed with JAVA_HOME configured appropriately
- gradle

You can run the server in dev mode:

```
./gradlew runDevServer
```

Be aware that the server will start correctly if a PC/SC reader is connected whose name matches the default filter defined in the ``application.properties`` file:
``sam.pcsc.reader.filter=.*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*``

A web browser should be launched automatically pointing to the server dashboard application. If not started, open the default url: http://localhost:8080

## Packaging and running the application

The server depends on an external module located in `../common`.

This project includes a web app `dashboard-app` based on the [create react app](https://github.com/facebook/create-react-app) and [material-ui](https://material-ui.com/) projects.

As such to complete the build, you need to install:
- npm 5.2+

The application can be packaged using:

```./gradlew buildServerExecutable```

It produces the `keyple-demo-remote-server-vYYYY.MM.jar` file in the `build` directory (where YYYY and MM are respectively the year and the month of the release)
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar along with the dashboard-app.

The application is now runnable using:

```java -jar build/keyple-demo-remote-server-vYYYY.MM.jar```
(where YYYY and MM are respectively the year and the month of the release)

or using the custom gradle task that runs the previous command: 

```./gradlew runServerExecutable```


## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.
