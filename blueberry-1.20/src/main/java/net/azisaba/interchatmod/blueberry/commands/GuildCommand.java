package net.azisaba.interchatmod.blueberry.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.azisaba.interchatmod.blueberry.Mod;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.common.util.Constants;
import net.blueberrymc.client.commands.ClientCommandHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@SuppressWarnings("SameReturnValue")
public class GuildCommand implements ClientCommandHandler {
    public static final Set<String> KNOWN_PLAYERS = Collections.synchronizedSet(new HashSet<>());
    private final Mod mod;

    public GuildCommand(Mod mod) {
        this.mod = mod;
    }

    @Override
    public void register(@NotNull CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(
                literal("cguild")
                        .then(literal("invite")
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx -> executeInvite(StringArgumentType.getString(ctx, "player")))
                                )
                        )
                        .then(literal("accept")
                                .then(argument("guild", StringArgumentType.word())
                                        .executes(ctx -> executeRespondInvite(StringArgumentType.getString(ctx, "guild"), true))
                                )
                        )
                        .then(literal("reject")
                                .then(argument("guild", StringArgumentType.word())
                                        .executes(ctx -> executeRespondInvite(StringArgumentType.getString(ctx, "guild"), false))
                                )
                        )
                        .then(literal("nick")
                                .executes(ctx -> executeNick(null))
                                .then(argument("nickname", StringArgumentType.greedyString())
                                        .executes(ctx -> executeNick(StringArgumentType.getString(ctx, "nickname")))
                                )
                        )
                        .then(literal("jp-on")
                                .executes(ctx -> {
                                    mod.client.toggleTranslate(true);
                                    return 0;
                                })
                        )
                        .then(literal("jp-off")
                                .executes(ctx -> {
                                    mod.client.toggleTranslate(false);
                                    return 0;
                                })
                        )
                        .then(literal("role")
                                .then(argument("member", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            var set = mod.guildMembers.getOrDefault(mod.client.getSelectedGuild(), Collections.emptySet());
                                            return SharedSuggestionProvider.suggest(set.stream().map(GuildMember::name), builder);
                                        })
                                        .then(argument("role", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Stream.of("owner", "moderator", "member"), builder))
                                                .executes(ctx -> {
                                                    mod.client.role(StringArgumentType.getString(ctx, "member"), StringArgumentType.getString(ctx, "role"));
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(literal("toggleinvites")
                                .executes(ctx -> {
                                    mod.client.toggleInvites();
                                    return 0;
                                })
                        )
                        .then(literal("hide-guild")
                                .executes(ctx -> {
                                    mod.client.hideGuild();
                                    return 0;
                                })
                        )
                        .then(literal("hideall")
                                .executes(ctx -> {
                                    mod.client.hideAll("");
                                    return 0;
                                })
                                .then(argument("duration", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            mod.client.hideAll(StringArgumentType.getString(ctx, "duration"));
                                            return 0;
                                        })
                                )
                        )
                        .then(literal("format")
                                .then(argument("format", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Constants.FORMAT_VARIABLES.stream(), builder))
                                        .executes(ctx -> {
                                            mod.client.format(StringArgumentType.getString(ctx, "format"));
                                            return 0;
                                        })
                                )
                        )
                        .then(literal("info").executes(ctx -> executeInfo(ctx.getSource())))
                        .then(literal("block")
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx -> {
                                            mod.client.block(StringArgumentType.getString(ctx, "player"));
                                            return 0;
                                        })
                                )
                        )
                        .then(literal("unblock")
                                .then(argument("player", StringArgumentType.word())
                                        .executes(ctx -> {
                                            mod.client.unblock(StringArgumentType.getString(ctx, "player"));
                                            return 0;
                                        })
                                )
                        )
                        .then(literal("tell")
                                .then(argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(KNOWN_PLAYERS.stream(), builder))
                                        .then(argument("message", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    mod.client.tell(StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message"));
                                                    KNOWN_PLAYERS.add(StringArgumentType.getString(ctx, "player"));
                                                    return 0;
                                                })
                                        )
                                )
                        )
        );
    }

    private int executeInfo(CommandSourceStack source) {
        var guild = mod.guilds.stream().filter(g -> g.id() == mod.client.getSelectedGuild()).findAny().orElse(null);
        if (guild == null) {
            source.sendFailure(Component.literal("無効なギルドが指定されています。/cgs (ギルド)で選択してください。"));
            return 0;
        }
        var members = mod.guildMembers.getOrDefault(guild.id(), Collections.emptySet());
        source.sendSystemMessage(
                Component.literal("--- ギルド").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(guild.name()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("の情報 ---").withStyle(ChatFormatting.GOLD))
        );
        source.sendSystemMessage(
                Component.literal("メンバー数: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(members.size())).withStyle(ChatFormatting.RED))
                        .append(Component.literal("/").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(String.valueOf(guild.capacity())).withStyle(ChatFormatting.RED))
        );
        Consumer<String> sendRole = (role) -> {
            String players =
                    members.stream()
                            .filter(m -> m.role().equals(role.toUpperCase(Locale.ROOT)))
                            .map(GuildMember::name)
                            .collect(Collectors.joining(", "));
            source.sendSystemMessage(
                    Component.literal(role + ": ").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(players).withStyle(ChatFormatting.WHITE))
            );
        };
        sendRole.accept("Owner");
        sendRole.accept("Moderator");
        sendRole.accept("Member");
        source.sendSystemMessage(Component.empty());
        source.sendSystemMessage(
                Component.literal("公開: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(guild.open())).withStyle(guild.open() ? ChatFormatting.GREEN : ChatFormatting.RED))
        );
        source.sendSystemMessage(
                Component.literal("チャット形式: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(guild.format())
                                .withStyle(ChatFormatting.WHITE)
                                .withStyle(style ->
                                        style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("クリックでコピー")))
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, guild.format()))
                                )
                        )
        );
        return 0;
    }

    private int executeInvite(String player) {
        mod.client.invite(player);
        return 1;
    }

    private int executeRespondInvite(String guildName, boolean accept) {
        mod.client.respondInvite(guildName, accept);
        return 1;
    }

    private int executeNick(String nickname) {
        mod.client.nick(nickname);
        return 1;
    }
}
