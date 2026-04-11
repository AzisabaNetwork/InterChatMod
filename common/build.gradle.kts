java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

val adventureVersion by project.properties

dependencies {
    api("net.kyori:adventure-api:$adventureVersion")
    api("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    api("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    api("org.java-websocket:Java-WebSocket:1.5.4")
}

plugins.withId("com.modrinth.minotaur") {
    tasks.named("modrinth") {
        enabled = false
    }
}
