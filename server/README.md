
# Keyple distributed demo - Server

## Running the server

This example is based on the Quarkus framework. To start the server, you need to install Quarkus dependencies : 
- JDK 1.8+ installed with JAVA_HOME configured appropriately
- gradle

You can run the server in dev mode:

```
./gradlew runDevServer
```

Be aware that the server will start correctly if a PCSC reader is connected. 

A web browser should be launched automatically pointing to the server dashboard application. If not started, open the default url : http://localhost:8080

## Packaging and running the application

The server depends on an external module located in `../common`.

This project includes a web app `dashboard-app` based on the [create react app](https://github.com/facebook/create-react-app) and [material-ui](https://material-ui.com/) projects.

As such to complete the build, you need to install :
- npm 5.2+

The application can be packaged using 

```./gradlew buildServerExecutable```

It produces the `keyple-java-demo-remote-server-YYYY.MM-runner.jar` file in the `build` directory (where YYYY and MM are respectively the year and the month of the release)
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar along with the dashboard-app.

The application is now runnable using :

```java -jar build/keyple-java-demo-remote-server-YYYY.MM-runner.jar```
(where YYYY and MM are respectively the year and the month of the release)

or using the custom gradle task that runs the previous command : 

```./gradlew runServerExecutable```



## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
