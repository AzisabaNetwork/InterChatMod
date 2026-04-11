plugins {
    java
    `java-library`
}

allprojects {
    apply {
        plugin("java")
        plugin("java-library")
    }

    group = "net.azisaba.interchatmod"
    version = "0.6.0"

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
