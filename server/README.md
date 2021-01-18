
## Running the example

This example is based on the Quarkus framework. To execute the example, you need to install Quarkus dependencies : 
- JDK 1.8+ installed with JAVA_HOME configured appropriately
- gradle

You can run the example in dev mode:

```
./gradlew runExample
```

This project depends on an external module located in `../common`. If you need a standalone module, you can copy manually all classes in the `common` module inside this project. Don't forget to delete references to the `common` module in the `gradle.properties` and `build.gradle` configuration files. 

## Packaging and running the application

The application can be packaged using `./gradlew quarkusBuild`.
It produces the `UseCase1_ReaderClientSide_Webservice-1.0.0-runner.jar` file in the `build` directory.
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar.

The application is now runnable using `java -jar build/UseCase1_ReaderClientSide_Webservice-1.0.0-runner.jar`.

## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
