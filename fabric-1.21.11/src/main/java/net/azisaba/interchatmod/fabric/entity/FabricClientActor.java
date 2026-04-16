package net.azisaba.interchatmod.fabric.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.azisaba.interchatmod.common.entity.Actor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.NotNull;

public class FabricClientActor implements Actor {
    private static final Gson GSON = new Gson();
    private final FabricClientCommandSource source;

    public FabricClientActor(FabricClientCommandSource source) {
        this.source = source;
    }

    @Override
    public void sendFeedback(@NotNull String json) {
        source.sendFeedback(deserializeFromJson(json));
    }

    @Override
    public void sendError(@NotNull String json) {
        source.sendError(deserializeFromJson(json));
    }

    @Override
    public String toString() {
        return source.toString();
    }

    private static Text deserializeFromJson(String json) {
        return TextCodecs.CODEC
                .decode(JsonOps.INSTANCE, GSON.fromJson(json, JsonElement.class))
                .getOrThrow()
                .getFirst();
    }
}
