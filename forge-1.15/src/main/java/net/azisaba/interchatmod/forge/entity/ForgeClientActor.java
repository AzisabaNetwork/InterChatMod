package net.azisaba.interchatmod.forge.entity;

import net.azisaba.interchatmod.common.entity.Actor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

public class ForgeClientActor implements Actor {
    private final CommandSource source;

    public ForgeClientActor(@NotNull CommandSource source) {
        this.source = source;
    }

    @Override
    public void sendFeedback(@NotNull String json) {
        source.sendFeedback(deserialize(json), false);
    }

    @Override
    public void sendError(@NotNull String json) {
        source.sendErrorMessage(deserialize(json));
    }

    private static ITextComponent deserialize(String json) {
        return ITextComponent.Serializer.fromJson(
                GsonComponentSerializer.colorDownsamplingGson().serialize(
                        GsonComponentSerializer.gson().deserialize(json)
                )
        );
    }
}
