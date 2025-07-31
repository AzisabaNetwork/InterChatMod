package net.azisaba.interchatmod.fabric;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.azisaba.interchatmod.common.AbstractWebSocketChatClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.net.URI;
import java.util.TimerTask;

public class WebSocketChatClient extends AbstractWebSocketChatClient {
    public WebSocketChatClient(URI uri) {
        super(uri);
    }

    @Override
    protected String getApiKey() {
        return Mod.CONFIG.apiKey();
    }

    @Override
    protected boolean isInAzisaba() {
        return Mod.isInAzisaba();
    }

    @Override
    protected void trySwitch() {
        Mod.trySwitch();
    }

    @Override
    protected void scheduleReconnect() {
        Mod.TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                Mod.reconnect();
            }
        }, 1000 * 5);
        Mod.TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!Mod.client.isOpen()) {
                    Mod.reconnect();
                }
            }
        }, 1000 * 15);
        Mod.TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!Mod.client.isOpen()) {
                    Mod.reconnect();
                }
            }
        }, 1000 * 30);
    }

    @Override
    protected void sendJsonMessage(String json) {
        sendMessage(TextCodecs.CODEC
                .decode(JsonOps.INSTANCE, gson.fromJson(json, JsonElement.class))
                .getOrThrow()
                .getFirst());
    }

    private void sendMessage(Text text) {
        if (text == null) return;
        Mod.LOGGER.info("[WS] {}", text.getString());
        if (Mod.CONFIG.hideEverything()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        player.sendMessage(text, false);
    }

    @Override
    public void onError(Exception ex) {
        Mod.LOGGER.error("WebSocket error", ex);
    }
}
