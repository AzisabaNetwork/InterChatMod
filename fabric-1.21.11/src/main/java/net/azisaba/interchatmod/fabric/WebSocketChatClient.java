package net.azisaba.interchatmod.fabric;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.azisaba.interchatmod.common.AbstractWebSocketChatClient;
import net.azisaba.interchatmod.common.InterChatMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.TimerTask;

public class WebSocketChatClient extends AbstractWebSocketChatClient {
    public WebSocketChatClient(@NotNull InterChatMod mod, @NotNull URI uri) {
        super(mod, uri);
    }

    @Override
    protected boolean isInAzisaba() {
        return Mod.isInAzisaba();
    }

    @Override
    protected void trySwitch() {
        Mod.instance.trySwitch();
    }

    @Override
    protected void scheduleReconnect() {
        Mod.instance.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                Mod.instance.reconnect();
            }
        }, 1000 * 5);
        Mod.instance.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!Mod.instance.getWebSocketChatClient().isOpen()) {
                    Mod.instance.reconnect();
                }
            }
        }, 1000 * 15);
        Mod.instance.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!Mod.instance.getWebSocketChatClient().isOpen()) {
                    Mod.instance.reconnect();
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
        Mod.instance.getLogger().info("[WS] {}", text.getString());
        if (Mod.CONFIG.hideEverything) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        MinecraftClient.getInstance().send(() -> player.sendMessage(text, false));
    }
}
