
## Running the server

This example is based on the Quarkus framework. To execute the example, you need to install Quarkus dependencies : 
- JDK 1.8+ installed with JAVA_HOME configured appropriately
- gradle

You can run the server in dev mode:

```
./gradlew runServer
```

This project depends on an external module located in `../common`. 

## Packaging and running the application

The application can be packaged using `./gradlew quarkusBuild`.
It produces the `keyple-java-demo-remote-server-1.0.0-SNAPSHOT-runner.jar` file in the `build` directory.
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar.

The application is now runnable using `java -jar build/keyple-java-demo-remote-server-1.0.0-SNAPSHOT-runner.jar`

## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
