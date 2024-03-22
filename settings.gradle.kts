rootProject.name = "noisy-patterns"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            name = "EldoNexus"
            url = uri("https://eldonexus.de/repository/maven-public/")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("fawe", "2.9.1")
            library("fawe-core", "com.fastasyncworldedit", "FastAsyncWorldEdit-Core").versionRef("fawe")
            library("fawe-bukkit", "com.fastasyncworldedit", "FastAsyncWorldEdit-Bukkit").versionRef("fawe")
            bundle("fawe", listOf("fawe-core", "fawe-bukkit"))

            library("paper", "io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

            // plugins
            plugin("spotless", "com.diffplug.spotless").version("6.25.0")
            plugin("publishdata", "de.chojo.publishdata").version("1.4.0")
            plugin("pluginyml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")
            plugin("hangar", "io.papermc.hangar-publish-plugin").version("0.1.2")
            plugin("modrinth", "com.modrinth.minotaur").version("2.8.7")
            plugin("runserver", "xyz.jpenilla.run-paper").version("2.2.3")
        }
    }
}
