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

public class ClientGuildSelectCommand implements InterChatCommand {
    @Override
    public @NotNull ActorLiteralArgumentBuilder builder(@NotNull InterChatMod mod) {
        return literal(mod, "cgs")
                .then(argument(mod, "guild", StringArgumentType.word())
                        .suggests((ctx, builder) -> InterChatCommand.suggestMatching(mod.getGuilds().stream().map(Guild::name), builder))
                        .executes(ctx -> executeFocus(mod, ctx.getSource(), StringArgumentType.getString(ctx, "guild")))
                        .then(argument(mod, "message", StringArgumentType.greedyString())
                                .executes(ctx -> ClientGuildShortCommand.executeChat(mod, ctx.getSource(), StringArgumentType.getString(ctx, "guild"), StringArgumentType.getString(ctx, "message")))
                        )
                );
    }

    private static int executeFocus(InterChatMod mod, Actor source, String guildName) {
        Guild guild = mod.getGuilds().stream().filter(g -> g.name().equalsIgnoreCase(guildName)).findAny().orElse(null);
        if (guild == null) {
            source.sendError(Component.translatable("generic.guild_not_found", Component.text(guildName)));
            return 0;
        }
        mod.getWebSocketChatClient().selectGuild(guild.id());
        source.sendFeedback(Component.translatable("generic.guild_focus_set", Component.text(guild.name())));
        return 1;
    }
}
