rootProject.name = "InterChatMod"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

include("common")
include("fabric-1.21.8")
include("fabric-1.21")
include("fabric-1.20")
include("fabric-1.19")
include("fabric-1.18")
include("fabric-1.17")
include("fabric-1.16")
include("forge-1.15")
