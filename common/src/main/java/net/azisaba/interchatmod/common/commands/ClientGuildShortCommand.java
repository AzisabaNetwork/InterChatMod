package net.azisaba.interchatmod.common.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.command.ActorLiteralArgumentBuilder;
import net.azisaba.interchatmod.common.command.InterChatCommand;
import net.azisaba.interchatmod.common.entity.Actor;
import net.azisaba.interchatmod.common.model.Guild;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import static net.azisaba.interchatmod.common.command.CommandManager.argument;
import static net.azisaba.interchatmod.common.command.CommandManager.literal;

public class ClientGuildShortCommand implements InterChatCommand {
    @Override
    public @NotNull ActorLiteralArgumentBuilder builder(@NotNull InterChatMod mod) {
        return literal(mod, "cg")
                .then(argument(mod, "message", StringArgumentType.greedyString())
                        .executes(ctx -> executeChat(mod, ctx.getSource(), null, StringArgumentType.getString(ctx, "message")))
                );
    }

    static int executeChat(InterChatMod mod, Actor source, String guildName, String message) {
        if (guildName != null) {
            Guild guild = mod.getGuilds().stream().filter(g -> g.name().equalsIgnoreCase(guildName)).findAny().orElse(null);
            if (guild == null) {
                source.sendError(Component.translatable("generic.guild_not_found", Component.text(guildName)));
                return 0;
            }
            mod.getWebSocketChatClient().sendMessageToGuild(guild.id(), message);
        } else {
            mod.getWebSocketChatClient().sendMessageToGuild(null, message);
        }
        return 1;
    }
}
