plugins {
    java
    `java-library`
    id("com.modrinth.minotaur") version "2.+"
}

allprojects {
    apply {
        plugin("java")
        plugin("java-library")
        plugin("com.modrinth.minotaur")
    }

    group = "net.azisaba.interchatmod"
    version = "0.7.0"

    repositories {
        // mavenLocal()
        mavenCentral()
        maven { url = uri("https://libraries.minecraft.net/") }
        maven {
            url = uri("https://repo.azisaba.net/repository/maven-public/")
            content {
                excludeGroup("org.spongepowered")
            }
        }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }
}
