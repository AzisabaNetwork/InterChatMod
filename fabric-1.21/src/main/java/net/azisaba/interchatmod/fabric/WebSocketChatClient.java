package net.azisaba.interchatmod.fabric;

import net.azisaba.interchatmod.common.AbstractWebSocketChatClient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

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
    protected void sendMessage(Component component) {
        var serializer = LegacyComponentSerializer.legacySection();
        sendMessage(Text.literal(serializer.serialize(component)));
    }

    private void sendMessage(Text text) {
        if (text == null) return;
        Mod.LOGGER.info("[WS] {}", text.getString());
        if (Mod.CONFIG.hideEverything()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        player.sendMessage(text);
    }

    @Override
    public void onError(Exception ex) {
        Mod.LOGGER.error("WebSocket error", ex);
    }
}
