package net.azisaba.interchatmod.common.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.command.ActorLiteralArgumentBuilder;
import net.azisaba.interchatmod.common.command.InterChatCommand;
import org.jetbrains.annotations.NotNull;

import static net.azisaba.interchatmod.common.command.CommandManager.argument;
import static net.azisaba.interchatmod.common.command.CommandManager.literal;

public class ReconnectInterChatCommand implements InterChatCommand {
    @Override
    public @NotNull ActorLiteralArgumentBuilder builder(@NotNull InterChatMod mod) {
        return literal(mod, "reconnectinterchat")
                .executes(ctx -> {
                    mod.reconnect();
                    return 0;
                })
                .then(argument(mod, "apikey", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            mod.getConfig().setApiKey(StringArgumentType.getString(ctx, "apikey"));
                            mod.reconnect();
                            return 0;
                        })
                );
    }
}
