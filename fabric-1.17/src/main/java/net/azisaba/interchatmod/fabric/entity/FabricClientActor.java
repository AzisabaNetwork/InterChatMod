package net.azisaba.interchatmod.fabric.entity;

import net.azisaba.interchatmod.common.entity.Actor;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class FabricClientActor implements Actor {
    private final FabricClientCommandSource source;

    public FabricClientActor(FabricClientCommandSource source) {
        this.source = source;
    }

    @Override
    public void sendFeedback(@NotNull String json) {
        source.sendFeedback(Text.Serializer.fromJson(json));
    }

    @Override
    public void sendError(@NotNull String json) {
        source.sendError(Text.Serializer.fromJson(json));
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
