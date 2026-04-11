# InterChatMod

English | [日本語](https://github.com/AzisabaNetwork/InterChatMod/blob/main/README.ja.md)

---

Cross-server chat mod for Minecraft.

## ⚠️ Very Important

After an API key has entered into the config (and saved), this mod will attempt to connect to `api-ktor.azisaba.net`
to connect you to the InterChat server which we host on private infrastructure and Cloudflare (a third-party service).

Also, messages you sent to a guild will be logged to prevent abuse.
See [Private Policy](https://www.azisaba.net/privacy-policy/) ([English translation](https://gist.github.com/acrylic-style/262d36c861678f31f104da87393fee61)) for details on what data we collect and how we use it.

If you are not comfortable with this, don't use this mod or, you can override the API host in the config.

Deletion of your message data can be requested by contacting us at our [Discord server](https://azisaba.dev).

This mod currently has the following limitations:
- English support is experimental. Client-side is fully translated, but server-side is not. For example, command responses will be in Japanese.
- Creation of guilds is not supported yet. You can only create guilds through the Minecraft server. If you are using the default API host, you can create guilds by connecting to the `mc.azisaba.net` and typing `/guild create <name>`.

## Fabric

### 1.21.11 and up

#### Required mods

- [fabric-api](https://modrinth.com/mod/fabric-api)
- [Cloth Config API](https://modrinth.com/mod/cloth-config)

#### Optional but recommended mods

- [Mod Menu](https://modrinth.com/mod/modmenu)

### 1.19.4 - 1.21.8

#### Required mods

- [fabric-api](https://modrinth.com/mod/fabric-api)
- [owo-lib](https://modrinth.com/mod/owo-lib/version/0.11.3+1.20.2)

#### Optional but recommended mods

- [Mod Menu](https://modrinth.com/mod/modmenu)

### 1.18.2 and below

#### Required mods

- [fabric-api](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu)

### Forge 1.15.2

#### Required mods

None. Use `/interchatconfig` to open the config screen.

### How to use

- Get an API key from the server. By default, you would need to connect to `mc.azisaba.net` and type `/apikey` in-game.
    - Message from `/apikey` is not translated at time of writing. Sorry! Please click the message with the underline to copy the API key.
- You may use Mod Menu to open the config screen if you have it installed. Otherwise, you can use `/reconnectinterchat <api key>` in-game. 
- `/cgs <guild> [message]` to select a guild or chat in a guild.
- `/cg <message>` to chat in the guild previously selected with `/cgs`.
- `Chat without command` config to make default chat guild chat. Prefix with '!' to temporarily disable.

## Building 

`./gradlew build`

For a Fabric jar, you know what to do.

For a Forge jar, you need to use `-srg` jar when you put it in your mods folder.

## Links

- [InterChat](https://github.com/AzisabaNetwork/InterChat) – Server-side plugin for Spigot, Paper and Velocity.
- [api](https://github.com/AzisabaNetwork/api/tree/ktor/server/src/main/kotlin/net/azisaba/api/server/interchat) – WebSocket-based API which actually powers InterChatMod.
- [GuildChatDiscord](https://github.com/AzisabaNetwork/GuildChatDiscord) – Discord Bot for InterChat.
