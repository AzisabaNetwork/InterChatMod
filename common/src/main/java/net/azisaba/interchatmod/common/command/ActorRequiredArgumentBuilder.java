package net.azisaba.interchatmod.common.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.entity.Actor;
import org.jetbrains.annotations.NotNull;

public class ActorRequiredArgumentBuilder<T> extends ActorArgumentBuilder<ActorRequiredArgumentBuilder<T>, RequiredArgumentBuilder<Object, T>> {
    protected ActorRequiredArgumentBuilder(@NotNull InterChatMod mod, @NotNull RequiredArgumentBuilder<Object, T> builder) {
        super(mod, builder);
    }

    @NotNull
    public static <T> ActorRequiredArgumentBuilder<T> argument(@NotNull InterChatMod mod, @NotNull String name, @NotNull ArgumentType<T> type) {
        return new ActorRequiredArgumentBuilder<>(mod, RequiredArgumentBuilder.argument(name, type));
    }

    @NotNull
    public ActorRequiredArgumentBuilder<T> suggests(@NotNull SuggestionProvider<Actor> provider) {
        builder.suggests((ctx, b) -> provider.getSuggestions(adaptContext(ctx), b));
        return getThis();
    }

    @Override
    protected ActorRequiredArgumentBuilder<T> getThis() {
        return this;
    }

    @Override
    public @NotNull ArgumentCommandNode<Object, T> build() {
        return builder.build();
    }
}
