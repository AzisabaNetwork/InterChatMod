java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

val adventureVersion by project.properties

repositories {
    maven("https://libraries.minecraft.net")
}

dependencies {
    api("net.kyori:adventure-api:$adventureVersion")
    api("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    api("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    api("org.java-websocket:Java-WebSocket:1.5.4")
    compileOnly("com.mojang:brigadier:1.3.10")
}

plugins.withId("com.modrinth.minotaur") {
    tasks.named("modrinth") {
        enabled = false
    }
}
