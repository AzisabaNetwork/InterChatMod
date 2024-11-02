package net.azisaba.interchatmod.common;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public abstract class AbstractWebSocketChatClient extends WebSocketClient {
    protected final LegacyComponentSerializer legacyComponentSerializer =
            LegacyComponentSerializer.builder().extractUrls().character('§').build();
    protected final Gson gson = new Gson();
    private long openAt;
    private long selectedGuild = -1;

    public AbstractWebSocketChatClient(URI uri) {
        super(uri);
    }

    protected final void sendMessage(String message) {
        sendMessage(legacyComponentSerializer.deserialize(message));
    }

    protected void sendMessage(Component component) {
        sendJsonMessage(GsonComponentSerializer.gson().serialize(component));
    }

    protected void sendJsonMessage(String json) {
        throw new RuntimeException("Implementation must override sendJsonMessage or sendMessage(Component) method");
    }

    protected abstract String getApiKey();

    protected abstract boolean isInAzisaba();

    protected abstract void trySwitch();

    protected abstract void scheduleReconnect();

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        openAt = System.currentTimeMillis();
        auth(getApiKey());
        trySwitch();
        sendMessage("ギルドチャットに接続しました。");
    }

    @Override
    public void onMessage(String message) {
        JsonObject obj = gson.fromJson(message, JsonObject.class);
        if (!obj.has("type")) return;
        String type = obj.get("type").getAsString();
        if (type.equals("message") && !isInAzisaba()) {
            sendMessage(obj.get("message").getAsString());
        }
        if (type.equals("component") && !isInAzisaba()) {
            sendMessage(GsonComponentSerializer.gson().deserialize(obj.get("message").getAsString()));
        }
        if (type.equals("feedback")) {
            sendMessage(GsonComponentSerializer.gson().deserialize(obj.get("json").getAsString()));
        }
        if (type.equals("error_message")) {
            sendMessage(Component.text("サーバーからエラーが返されました: " + obj.get("message").getAsString(), NamedTextColor.RED));
        }
        if (type.equals("guild")) {
            selectedGuild = obj.get("guildId").getAsLong();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (System.currentTimeMillis() - openAt < 1000) {
            sendMessage("ギルドチャットから切断されました。APIキーを確認して、/reconnectinterchat [apikey]を実行してください。");
            return;
        }
        if (remote) {
            sendMessage("ギルドチャットから切断されました。5秒後に再接続を試みます。");
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        sendMessage("WebSocket接続でエラーが発生しました: " + ex.getMessage());
        ex.printStackTrace();
    }

    public long getSelectedGuild() {
        return selectedGuild;
    }

    protected final @NotNull JsonObject createPacket(String type) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        return obj;
    }

    public void auth(String key) {
        JsonObject obj = createPacket("auth");
        obj.addProperty("key", key);
        send(gson.toJson(obj));
    }

    public void switchServer(String server) {
        JsonObject obj = createPacket("switch_server");
        obj.addProperty("server", server);
        send(gson.toJson(obj));
    }

    public void selectGuild(long id) {
        JsonObject obj = createPacket("select");
        obj.addProperty("guildId", id);
        send(gson.toJson(obj));
    }

    public void sendMessageToGuild(@Nullable Long guildId, String message) {
        JsonObject obj = createPacket("message");
        obj.add("guildId", guildId == null ? JsonNull.INSTANCE : new JsonPrimitive(guildId));
        obj.addProperty("message", message);
        send(gson.toJson(obj));
    }

    public void invite(String player) {
        JsonObject obj = createPacket("invite");
        obj.addProperty("player", player);
        send(gson.toJson(obj));
    }

    public void respondInvite(String guildName, boolean accept) {
        JsonObject obj = createPacket("respond_invite");
        obj.addProperty("guildName", guildName);
        obj.addProperty("accept", accept);
        send(gson.toJson(obj));
    }

    public void nick(@Nullable String nickname) {
        JsonObject obj = createPacket("nick");
        obj.addProperty("nickname", nickname);
        send(gson.toJson(obj));
    }

    public void toggleTranslate(boolean doTranslate) {
        JsonObject obj = createPacket("toggle_translate");
        obj.addProperty("doTranslate", doTranslate);
        send(gson.toJson(obj));
    }

    public void role(@NotNull String member, @NotNull String role) {
        JsonObject obj = createPacket("role");
        obj.addProperty("member", member);
        obj.addProperty("role", role);
        send(gson.toJson(obj));
    }

    public void block(@NotNull String player) {
        JsonObject obj = createPacket("block");
        obj.addProperty("player", player);
        send(gson.toJson(obj));
    }

    public void unblock(@NotNull String player) {
        JsonObject obj = createPacket("unblock");
        obj.addProperty("player", player);
        send(gson.toJson(obj));
    }

    public void tell(@NotNull String player, @NotNull String message) {
        JsonObject obj = createPacket("tell");
        obj.addProperty("player", player);
        obj.addProperty("message", message);
        send(gson.toJson(obj));
    }

    public void toggleInvites() {
        send(gson.toJson(createPacket("toggle_invites")));
    }

    public void hideGuild() {
        send(gson.toJson(createPacket("hide_guild")));
    }

    public void hideAll(@NotNull String duration) {
        JsonObject obj = createPacket("hide_all");
        obj.addProperty("duration", duration);
        send(gson.toJson(obj));
    }

    public void format(@NotNull String format) {
        JsonObject obj = createPacket("format");
        obj.addProperty("format", format);
        send(gson.toJson(obj));
    }
}
