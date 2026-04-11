package net.azisaba.interchatmod.forge;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Commands {
    public static final @NotNull Set<String> KNOWN_PLAYERS = new HashSet<>();

    public static LiteralArgumentBuilder<ISuggestionProvider> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<ISuggestionProvider, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static LiteralArgumentBuilder<ISuggestionProvider> builderGTell() {
        return literal("cgtell")
                .then(argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(KNOWN_PLAYERS.stream(), builder))
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    Mod.client.tell(StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message"));
                                    KNOWN_PLAYERS.add(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                );
    }


    public static LiteralArgumentBuilder<ISuggestionProvider> builderGS() {
        return literal("cgs")
                .then(argument("guild", StringArgumentType.word())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(Mod.GUILDS.stream().map(Guild::name), builder))
                        .executes(ctx -> executeFocus(ctx.getSource(), StringArgumentType.getString(ctx, "guild")))
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> executeChat(ctx.getSource(), StringArgumentType.getString(ctx, "guild"), StringArgumentType.getString(ctx, "message")))
                        )
                );
    }

    public static LiteralArgumentBuilder<ISuggestionProvider> builderG() {
        return literal("cg")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> executeChat(ctx.getSource(), null, StringArgumentType.getString(ctx, "message")))
                );
    }

    public static LiteralArgumentBuilder<ISuggestionProvider> builderReconnectInterChat() {
        return literal("reconnectinterchat")
                .executes(ctx -> {
                    Mod.reconnect();
                    return 0;
                })
                .then(argument("apikey", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ModConfig.apiKey = StringArgumentType.getString(ctx, "apikey");
                            Mod.reconnect();
                            return 0;
                        })
                );
    }

    public static LiteralArgumentBuilder<ISuggestionProvider> builderGuild() {
        return literal("cguild")
                .then(literal("invite")
                        .then(argument("player", StringArgumentType.word())
                                .executes(ctx -> {
                                    Mod.client.invite(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                )
                .then(literal("accept")
                        .then(argument("guild", StringArgumentType.word())
                                .executes(ctx -> {
                                    Mod.client.respondInvite(StringArgumentType.getString(ctx, "guild"), true);
                                    return 0;
                                })
                        )
                )
                .then(literal("reject")
                        .then(argument("guild", StringArgumentType.word())
                                .executes(ctx -> {
                                    Mod.client.respondInvite(StringArgumentType.getString(ctx, "guild"), false);
                                    return 0;
                                })
                        )
                )
                .then(literal("nick")
                        .executes(ctx -> {
                            Mod.client.nick(null);
                            return 0;
                        })
                        .then(argument("nickname", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    Mod.client.nick(StringArgumentType.getString(ctx, "nickname"));
                                    return 0;
                                })
                        )
                )
                .then(literal("jp-on")
                        .executes(ctx -> {
                            Mod.client.toggleTranslate(true);
                            return 0;
                        })
                )
                .then(literal("jp-off")
                        .executes(ctx -> {
                            Mod.client.toggleTranslate(false);
                            return 0;
                        })
                )
                .then(literal("role")
                        .then(argument("member", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Set<GuildMember> set = Mod.guildMembers.getOrDefault(Mod.client.getSelectedGuild(), Collections.emptySet());
                                    return ISuggestionProvider.suggest(set.stream().map(GuildMember::name), builder);
                                })
                                .then(argument("role", StringArgumentType.word())
                                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(Stream.of("owner", "moderator", "member"), builder))
                                        .executes(ctx -> {
                                            Mod.client.role(StringArgumentType.getString(ctx, "member"), StringArgumentType.getString(ctx, "role"));
                                            return 0;
                                        })
                                )
                        )
                )
                .then(literal("toggleinvites")
                        .executes(ctx -> {
                            Mod.client.toggleInvites();
                            return 0;
                        })
                )
                .then(literal("hide-guild")
                        .executes(ctx -> {
                            Mod.client.hideGuild();
                            return 0;
                        })
                )
                .then(literal("hideall")
                        .executes(ctx -> {
                            Mod.client.hideAll("");
                            return 0;
                        })
                        .then(argument("duration", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    Mod.client.hideAll(StringArgumentType.getString(ctx, "duration"));
                                    return 0;
                                })
                        )
                )
                .then(literal("format")
                        .then(argument("format", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(Constants.FORMAT_VARIABLES.stream(), builder))
                                .executes(ctx -> {
                                    Mod.client.format(StringArgumentType.getString(ctx, "format"));
                                    return 0;
                                })
                        )
                )
                .then(literal("info").executes(ctx -> executeInfo(ctx.getSource())))
                .then(literal("block")
                        .then(argument("player", StringArgumentType.word())
                                .executes(ctx -> {
                                    Mod.client.block(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                )
                .then(literal("unblock")
                        .then(argument("player", StringArgumentType.word())
                                .executes(ctx -> {
                                    Mod.client.unblock(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                )
                .then(literal("tell")
                        .then(argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(KNOWN_PLAYERS.stream(), builder))
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            Mod.client.tell(StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message"));
                                            KNOWN_PLAYERS.add(StringArgumentType.getString(ctx, "player"));
                                            return 0;
                                        })
                                )
                        )
                )
                ;
    }

    public static LiteralArgumentBuilder<ISuggestionProvider> builderInterChatConfig() {
        return literal("interchatconfig").executes(ctx -> {
            Minecraft.getInstance().enqueue(() -> Minecraft.getInstance().displayGuiScreen(new ModConfigScreen(Minecraft.getInstance().currentScreen)));
            return 0;
        });
    }

    private static int executeInfo(ISuggestionProvider provider) {
        if (!(provider instanceof CommandSource)) return 0;
        CommandSource source = (CommandSource) provider;
        Guild guild = Mod.GUILDS.stream().filter(g -> g.id() == Mod.client.getSelectedGuild()).findAny().orElse(null);
        if (guild == null) {
            source.sendErrorMessage(new TranslationTextComponent("generic.invalid_selected_guild"));
            return 0;
        }
        Set<GuildMember> members = Mod.guildMembers.getOrDefault(guild.id(), Collections.emptySet());
        source.sendFeedback(
                new TranslationTextComponent("generic.guild_info_header", new StringTextComponent(guild.name()).applyTextStyles(TextFormatting.AQUA)).applyTextStyles(TextFormatting.GOLD),
                false
        );
        source.sendFeedback(
                new TranslationTextComponent("generic.guild_members",
                        new StringTextComponent(String.valueOf(members.size())).applyTextStyles(TextFormatting.RED),
                        new StringTextComponent(String.valueOf(guild.capacity())).applyTextStyles(TextFormatting.RED)
                ).applyTextStyles(TextFormatting.GOLD),
                false
        );
        // TODO: implement player presence
        /*
                Consumer<String> sendRole = (role) -> {
            List<MutableText> players =
                    members.stream()
                            .filter(m -> m.role().equals(role.toUpperCase(Locale.ROOT)))
                            .map(member -> {
                                if (member.presence() == null) {
                                    return new LiteralText(member.name()).formatted(Formatting.WHITE);
                                }
                                if (System.currentTimeMillis() - member.presence().lastSeen < 60000) {
                                    return new LiteralText(member.name()).formatted(Formatting.GREEN)
                                            .styled(style -> style.withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            new LiteralText("プレイ中: ")
                                                                    .formatted(Formatting.GREEN)
                                                                    .append(new LiteralText(member.presence().server).formatted(Formatting.YELLOW)))));
                                } else {
                                    return new LiteralText(member.name()).formatted(Formatting.WHITE);
                                }
                            })
                            .toList();
            MutableText mu = new LiteralText(role + ": ").formatted(Formatting.GOLD);
            for (int i = 0; i < players.size(); i++) {
                mu = mu.append(players.get(i));
                if (i < players.size() - 1) {
                    mu = mu.append(new LiteralText(", "));
                }
            }
            source.sendFeedback(mu);
        };
         */
        Consumer<String> sendRole = (role) -> {
            String players =
                    members.stream()
                            .filter(m -> m.role().equals(role.toUpperCase(Locale.ROOT)))
                            .map(GuildMember::name)
                            .collect(Collectors.joining(", "));
            source.sendFeedback(
                    new TranslationTextComponent(roleTranslationKey(role), new StringTextComponent("")).applyTextStyles(TextFormatting.GOLD)
                            .appendSibling(new StringTextComponent(players).applyTextStyles(TextFormatting.WHITE)),
                    false
            );
        };
        sendRole.accept("Owner");
        sendRole.accept("Moderator");
        sendRole.accept("Member");
        source.sendFeedback(new StringTextComponent(""), false);
        source.sendFeedback(
                new TranslationTextComponent("generic.guild_open",
                        new TranslationTextComponent(booleanTranslationKey(guild.open())).applyTextStyles(guild.open() ? TextFormatting.GREEN : TextFormatting.RED)
                ).applyTextStyles(TextFormatting.GOLD),
                false
        );
        source.sendFeedback(
                new TranslationTextComponent("generic.guild_chat_format").applyTextStyles(TextFormatting.GOLD)
                        .appendSibling(new StringTextComponent(guild.format())
                                .applyTextStyles(TextFormatting.WHITE)
                                .applyTextStyle(style -> {
                                    style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("generic.copy_on_click")));
                                    style.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, guild.format()));
                                })
                        ),
                false
        );
        return members.size();
    }

    private static int executeFocus(ISuggestionProvider source, String guildName) {
        Guild guild = Mod.GUILDS.stream().filter(g -> g.name().equalsIgnoreCase(guildName)).findAny().orElse(null);
        if (guild == null) {
            if (source instanceof CommandSource) {
                ((CommandSource) source).sendErrorMessage(new TranslationTextComponent("generic.guild_not_found", guildName));
            }
            return 0;
        }
        Mod.client.selectGuild(guild.id());
        if (source instanceof CommandSource) {
            ((CommandSource) source).sendFeedback(new TranslationTextComponent("generic.guild_focus_set", guild.name()), false);
        }
        return 1;
    }

    private static int executeChat(ISuggestionProvider source, String guildName, String message) {
        if (guildName != null) {
            Guild guild = Mod.GUILDS.stream().filter(g -> g.name().equalsIgnoreCase(guildName)).findAny().orElse(null);
            if (guild == null) {
                if (source instanceof CommandSource) {
                    ((CommandSource) source).sendErrorMessage(new TranslationTextComponent("generic.guild_not_found", guildName));
                }
                return 0;
            }
            Mod.client.sendMessageToGuild(guild.id(), message);
        } else {
            Mod.client.sendMessageToGuild(null, message);
        }
        return 1;
    }

    private static String roleTranslationKey(String role) {
        return "generic.guild_role." + role.toLowerCase(Locale.ROOT);
    }

    private static String booleanTranslationKey(boolean value) {
        return "generic.boolean." + value;
    }
}
