plugins {
    java
}

group = "com.altare.partners"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

// Compile with --release 11 for broad server compatibility (Paper 1.16.5+ runs on
// Java 11 or newer), but resolve the Paper API (published as a Java 21 library)
// as a Java 21 dependency.
configurations.compileClasspath {
    attributes {
        attribute(
            org.gradle.api.attributes.Attribute.of("org.gradle.jvm.version", Int::class.javaObjectType),
            21
        )
    }
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("--release", "11"))
    }
}
