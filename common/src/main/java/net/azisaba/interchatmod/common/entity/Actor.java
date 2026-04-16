package net.azisaba.interchatmod.common.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Actor {
    default GsonComponentSerializer createGsonComponentSerializer() {
        return GsonComponentSerializer.builder().editOptions(serializerOptions()).build();
    }

    default Consumer<OptionState.Builder> serializerOptions() {
        return b -> b.values(JSONOptions.compatibility());
    }

    default void sendFeedback(@NotNull Component component) {
        sendFeedback(createGsonComponentSerializer().serialize(component));
    }

    default void sendFeedback(@NotNull String json) {
        throw new RuntimeException("Implementation must override sendFeedback method");
    }

    default void sendError(@NotNull Component component) {
        sendError(createGsonComponentSerializer().serialize(component));
    }

    default void sendError(@NotNull String json) {
        throw new RuntimeException("Implementation must override sendError method");
    }
}
