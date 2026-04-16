package net.azisaba.interchatmod.common.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.command.ActorLiteralArgumentBuilder;
import net.azisaba.interchatmod.common.command.InterChatCommand;
import org.jetbrains.annotations.NotNull;

import static net.azisaba.interchatmod.common.command.CommandManager.argument;
import static net.azisaba.interchatmod.common.command.CommandManager.literal;

public class ClientGuildTellCommand implements InterChatCommand {
    @Override
    public @NotNull ActorLiteralArgumentBuilder builder(@NotNull InterChatMod mod) {
        return literal(mod, "cgtell")
                .then(argument(mod, "player", StringArgumentType.word())
                        .suggests((ctx, builder) -> InterChatCommand.suggestMatching(mod.getKnownPlayers().stream(), builder))
                        .then(argument(mod, "message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().tell(StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message"));
                                    mod.getKnownPlayers().add(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                );
    }
}
