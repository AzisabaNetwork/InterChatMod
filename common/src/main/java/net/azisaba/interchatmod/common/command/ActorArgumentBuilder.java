package net.azisaba.interchatmod.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.entity.Actor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public abstract class ActorArgumentBuilder<T extends ActorArgumentBuilder<T, B>, B extends ArgumentBuilder<Object, B>> {
    protected final InterChatMod mod;
    protected final B builder;

    protected ActorArgumentBuilder(@NotNull InterChatMod mod, @NotNull B builder) {
        this.mod = mod;
        this.builder = builder;
    }

    protected abstract T getThis();

    @NotNull
    public T then(@NotNull ActorArgumentBuilder<?, ?> argument) {
        builder.then(argument.builder);
        return getThis();
    }

    @NotNull
    public T executes(@NotNull Command<Actor> command) {
        builder.executes(context -> command.run(adaptContext(context)));
        return getThis();
    }

    @NotNull
    public T requires(@NotNull Predicate<Actor> requirement) {
        builder.requires(o -> requirement.test(mod.adaptActor(o)));
        return getThis();
    }

    @NotNull
    public CommandNode<Object> build() {
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected CommandContext<Actor> adaptContext(@NotNull CommandContext<Object> context) {
        return (CommandContext<Actor>) (Object) context.copyFor(mod.adaptActor(context.getSource()));
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    @NotNull
    public <S2, T2 extends ArgumentBuilder<S2, T2>> ArgumentBuilder<S2, T2> getUnsafeBuilder() {
        return (ArgumentBuilder<S2, T2>) builder;
    }
}
