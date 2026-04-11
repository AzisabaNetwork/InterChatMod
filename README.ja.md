# InterChatMod

[English](https://github.com/AzisabaNetwork/InterChatMod/blob/main/README.md) | 日本語

---

サーバーをまたいだチャットができるMod

## ⚠️ とても重要

APIキーを設定に入れて保存すると、このModはInterChatサーバーに接続するために、アジ鯖とCloudflare(サードパーティ)がホストする`api-ktor.azisaba.net`に接続します。

ギルドに送信されたメッセージは、悪用を防ぐためログに記録されます。
詳しくは、[プライバシーポリシー](https://www.azisaba.net/privacy-policy/) ([英語訳](https://gist.github.com/acrylic-style/262d36c861678f31f104da87393fee61))をお読みください。

メッセージデータの取得などを行われたくない場合は、このModを使用しない、もしくはAPIホストを設定で書き換えてください。

メッセージの削除は[azisaba.devのDiscordサーバー](https://azisaba.dev)から依頼ができます。

このModは現在、以下の制限があります:
- 英語は一部のみ対応しています。クライアント側は翻訳されていますが、サーバー側は翻訳されていないため、コマンドの結果が日本語になります。
- ギルドの作成はまだサポートされていません。デフォルトのAPIホストを使用している場合、`mc.azisaba.net`に接続して`/guild create <name>`でギルドを作成できます。

## Fabric

### 1.21.11以上

#### 必須Mod

- [fabric-api](https://modrinth.com/mod/fabric-api)
- [Cloth Config API](https://modrinth.com/mod/cloth-config)

#### 任意だけど推奨Mod

- [Mod Menu](https://modrinth.com/mod/modmenu)

### 1.19.4 - 1.21.8

#### 必須Mod

- [fabric-api](https://modrinth.com/mod/fabric-api)
- [owo-lib](https://modrinth.com/mod/owo-lib/version/0.11.3+1.20.2)

#### 任意だけど推奨Mod

- [Mod Menu](https://modrinth.com/mod/modmenu)

### 1.18.2 and below

#### 必須Mod

- [fabric-api](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu)

### Forge 1.15.2

#### 必須Mod

ありません。`/interchatconfig`で設定画面が開けます。

### 使い方

- まず最初にAPIキーを入手します。デフォルトでは、`mc.azisaba.net`に接続して`/apikey`を実行する必要があります。
- Mod Menuが入っている場合、Modリストから設定画面が開けます。入っていない場合は`/reconnectinterchat <APIキー>`でAPIキーの設定、保存、接続が可能です。
- `/cgs <ギルド> [メッセージ]`でギルドの選択、もしくはメッセージがある場合はメッセージを送信します。
- `/cg <メッセージ>`で選択したギルドにメッセージを送信します。
- `コマンドなしでチャット`設定がオンの場合は、デフォルトのチャットがギルドチャットになります。`!`を先頭に付けると一時的に無効化できます。

## Building

`./gradlew build`

For a Fabric jar, you know what to do.

For a Forge jar, you need to use `-srg` jar when you put it in your mods folder.

## Links

- [InterChat](https://github.com/AzisabaNetwork/InterChat) – Server-side plugin for Spigot, Paper and Velocity.
- [api](https://github.com/AzisabaNetwork/api/tree/ktor/server/src/main/kotlin/net/azisaba/api/server/interchat) – WebSocket-based API which actually powers InterChatMod.
- [GuildChatDiscord](https://github.com/AzisabaNetwork/GuildChatDiscord) – Discord Bot for InterChat.
