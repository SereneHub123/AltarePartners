plugins {
    java
}

group = "com.altare.partners"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Spigot 1.8.8 API, plus the chat library that exact server version bundles.
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") {
        exclude(group = "net.md-5", module = "bungeecord-chat")
    }
    compileOnly("net.md-5:bungeecord-chat:1.8-SNAPSHOT")
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("--release", "8"))
    }

    jar {
        archiveBaseName.set("AltarePartners-Legacy")
    }
}
