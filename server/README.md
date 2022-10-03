# Keyple Reload Demo - Java Server's repository

This is the repository for the Java Server of the Keyple Reload Demo application.

The server is accessible from the client application and can be monitored via a GUI.

Read the main [README](https://github.com/calypsonet/keyple-java-demo-remote#readme) to understand the purpose of this
demo.

## Running the server from the executable JAR file

The server is packaged as an executable jar `keyple-demo-remote-server-X.Y.Z-full.jar` which can be started with the
command: 

```
java -jar keyple-demo-remote-server-X.Y.Z-full.jar
```

A web browser should be launched automatically pointing to the server dashboard application.
If not started, open the default url: `http://localhost:8080`

By default, the server starts only if a PC/SC reader is connected.
The PC/SC reader name should match the default filter (regex format) defined in the `application.properties` file:  

```
sam.pcsc.reader.filter=.*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*
```

If you want to use a different filter, start the server with the parameter `-Dsam.pcsc.reader.filter=XXX` where XXX is a
matching regex of your PC/SC reader.

- **For Windows Command Prompt:**

The command hereafter starts the server with the PCSC reader `Identive CLOUD 2700 R Smart Card Reader`

```
java "-Dsam.pcsc.reader.filter=Identive CLOUD 2700 R Smart Card Reader.*" -jar keyple-demo-remote-server-X.Y.Z-full.jar 
```

- **For Windows Powershell:**

Beware of the syntax on Windows Powershell to pass an argument

```
java '-Dsam.pcsc.reader.filter=Identive CLOUD 2700 R Smart Card Reader.*' -jar .\keyple-demo-remote-server-X.Y.Z-full.jar
```

## Running the server from the sources (for developers)

The server is based on the [Quarkus](https://quarkus.io/) framework and the web app `dashboard-app` is based on the
[React](https://fr.reactjs.org/) library.

To start the server, you need to install first the following components: 

- `JDK 1.8+`
- `gradle`
- `Node JS`

Then, you have to configure `npm` for the project:

- Open a terminal in `dashboard-app` directory and execute these commands:

```
npm cache clear --force
npm install
```

You can now build and run the server:

```
./gradlew build
./gradlew startServer
```

The build command produces the `keyple-demo-remote-server-X.Y.Z-full.jar` JAR file in the `build` directory.
