import com.modrinth.minotaur.ModrinthExtension

plugins {
    java
    `java-library`
    id("com.modrinth.minotaur") version "2.+" apply false
}

allprojects {
    apply {
        plugin("java")
        plugin("java-library")
    }

    group = "net.azisaba.interchatmod"
    version = "0.7.1-alpha.2"

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

subprojects {
    if (name == "common") return@subprojects

    apply {
        plugin("com.modrinth.minotaur")
    }

    extensions.configure<ModrinthExtension>("modrinth") {
        fun getVersionType(): String {
            return if (project.version.toString().contains("alpha")) {
                "alpha"
            } else if (project.version.toString().contains("beta")) {
                "beta"
            } else {
                "release"
            }
        }

        projectId.set("interchat")
        versionType.set(getVersionType())
    }
}
