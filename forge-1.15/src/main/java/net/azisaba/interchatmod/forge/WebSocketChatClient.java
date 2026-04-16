package net.azisaba.interchatmod.forge;

import net.azisaba.interchatmod.common.AbstractWebSocketChatClient;
import net.azisaba.interchatmod.common.InterChatMod;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;
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
        sendMessage(
                ITextComponent.Serializer.fromJson(
                        GsonComponentSerializer.colorDownsamplingGson().serialize(
                                GsonComponentSerializer.gson().deserialize(json)
                        )
                )
        );
    }

    private void sendMessage(ITextComponent text) {
        if (text == null) return;
        Mod.instance.getLogger().info("[WS] {}", text.getString());
        if (ModConfig.hideEverything) return;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        player.sendMessage(text);
    }
}
