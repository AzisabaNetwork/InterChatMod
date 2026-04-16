package net.azisaba.interchatmod.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.azisaba.interchatmod.common.InterChatMod;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class ActorLiteralArgumentBuilder extends ActorArgumentBuilder<ActorLiteralArgumentBuilder, LiteralArgumentBuilder<Object>> {
    protected ActorLiteralArgumentBuilder(@NotNull InterChatMod mod, @NotNull LiteralArgumentBuilder<Object> builder) {
        super(mod, builder);
    }

    @NotNull
    public static ActorLiteralArgumentBuilder literal(@NotNull InterChatMod mod, @NotNull String literal) {
        return new ActorLiteralArgumentBuilder(mod, LiteralArgumentBuilder.literal(literal));
    }

    @Override
    protected ActorLiteralArgumentBuilder getThis() {
        return this;
    }

    @Override
    public @NotNull LiteralCommandNode<Object> build() {
        return builder.build();
    }

    public @NotNull String getLiteral() {
        return builder.getLiteral();
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public <S2> @NotNull LiteralArgumentBuilder<S2> getUnsafeLiteralBuilder() {
        return (LiteralArgumentBuilder<S2>) builder;
    }
}
