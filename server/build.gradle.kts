import org.apache.tools.ant.taskdefs.condition.Os
///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    java
    id("com.diffplug.spotless") version "5.10.2"
    id("io.quarkus") version "1.8.1.Final"
}
buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://repo.eclipse.org/service/local/repositories/maven_central/content")
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.keyple:keyple-gradle:0.2.+") { isChanging = true }
    }
}
apply(plugin = "org.eclipse.keyple")

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
repositories {
    mavenLocal()
    maven(url = "https://repo.eclipse.org/service/local/repositories/maven_central/content")
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
}

val jarName = quarkus.finalName().replace("-SNAPSHOT", "")
quarkus.setFinalName(jarName)

val javaSourceLevel: String by project
val javaTargetLevel: String by project
java {
    sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
    targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    println("Compiling Java $sourceCompatibility to Java $targetCompatibility.")
}

dependencies {
    // Demo common
    implementation("org.calypsonet.keyple:keyple-demo-common-lib:1.0.0-SNAPSHOT") { isChanging = true }

    // Keyple dependencies
    implementation("org.calypsonet.terminal:calypsonet-terminal-reader-java-api:1.0.+") { isChanging = true }
    implementation("org.calypsonet.terminal:calypsonet-terminal-calypso-java-api:1.0.+") { isChanging = true }
    implementation("org.eclipse.keyple:keyple-common-java-api:2.0.+") { isChanging = true }
    implementation("org.eclipse.keyple:keyple-service-java-lib:2.0.1")
    implementation("org.eclipse.keyple:keyple-service-resource-java-lib:2.0.1")
    implementation("org.eclipse.keyple:keyple-distributed-network-java-lib:2.0.0")
    implementation("org.eclipse.keyple:keyple-distributed-remote-java-lib:2.0.0")
    implementation("org.eclipse.keyple:keyple-card-calypso-java-lib:2.0.1")
    implementation("org.eclipse.keyple:keyple-plugin-pcsc-java-lib:2.0.0")
    implementation("org.eclipse.keyple:keyple-util-java-lib:2.+") { isChanging = true }
    // Quarkus
    implementation(enforcedPlatform("io.quarkus:quarkus-universe-bom:1.8.1.Final"))
    implementation("io.quarkus:quarkus-resteasy-jsonb")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-rest-client")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    // Google GSON
    implementation("com.google.code.gson:gson:2.8.9")
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    spotless {
        java {
            target("**/src/**/*.java")
            licenseHeaderFile("${project.rootDir}/LICENSE_HEADER")
            importOrder("java", "javax", "org", "com", "")
            removeUnusedImports()
            googleJavaFormat()
        }
    }
    clean {
        delete("dashboard-app/build")
    }
    jar {
        dependsOn.add("copyDashboard")
    }
}
val buildDashboard by tasks.creating(Exec::class) {
    workingDir = File("dashboard-app")
    var npm = "npm"
    if (Os.isFamily(Os.FAMILY_WINDOWS)) {
        npm = "npm.cmd"
    }
    commandLine(npm, "run", "build")
}
val copyDashboard by tasks.creating(Copy::class) {
    from("dashboard-app/build")
    into("build/resources/main/META-INF/resources")
    dependsOn.add("buildDashboard")
}
val start by tasks.creating(Exec::class) {
    group = "server"
    workingDir = File("build")
    commandLine("java", "-jar", "$jarName-full.jar")
}