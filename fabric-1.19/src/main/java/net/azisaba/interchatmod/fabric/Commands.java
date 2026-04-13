package net.azisaba.interchatmod.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.common.util.ClipboardUtil;
import net.azisaba.interchatmod.common.util.Constants;
import net.azisaba.interchatmod.common.util.function.IOSupplier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {
    private static final Gson GSON = new Gson();
    public static final @NotNull Set<String> KNOWN_PLAYERS = new HashSet<>();

    public static LiteralArgumentBuilder<FabricClientCommandSource> builderGTell() {
        return literal("cgtell")
                .then(argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> CommandSource.suggestMatching(KNOWN_PLAYERS.stream(), builder))
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    Mod.client.tell(StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message"));
                                    KNOWN_PLAYERS.add(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                );
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> builderGS() {
        return literal("cgs")
                .then(argument("guild", StringArgumentType.word())
                        .suggests((ctx, builder) -> CommandSource.suggestMatching(Mod.GUILDS.stream().map(Guild::name), builder))
                        .executes(ctx -> executeFocus(ctx.getSource(), StringArgumentType.getString(ctx, "guild")))
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> executeChat(ctx.getSource(), StringArgumentType.getString(ctx, "guild"), StringArgumentType.getString(ctx, "message")))
                        )
                );
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> builderG() {
        return literal("cg")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> executeChat(ctx.getSource(), null, StringArgumentType.getString(ctx, "message")))
                );
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> builderReconnectInterChat() {
        return literal("reconnectinterchat")
                .executes(ctx -> {
                    Mod.reconnect();
                    return 0;
                })
                .then(argument("apikey", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            Mod.CONFIG.apiKey(StringArgumentType.getString(ctx, "apikey"));
                            Mod.reconnect();
                            return 0;
                        })
                );
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> builderGuild() {
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
                                    return CommandSource.suggestMatching(set.stream().map(GuildMember::name), builder);
                                })
                                .then(argument("role", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(Stream.of("owner", "moderator", "member"), builder))
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
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(Constants.FORMAT_VARIABLES.stream(), builder))
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
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(KNOWN_PLAYERS.stream(), builder))
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            Mod.client.tell(StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message"));
                                            KNOWN_PLAYERS.add(StringArgumentType.getString(ctx, "player"));
                                            return 0;
                                        })
                                )
                        )
                )
                .then(literal("uploadimage")
                        .then(argument("uuid", StringArgumentType.string())
                                .executes(ctx -> executeUploadImage(ctx.getSource(), StringArgumentType.getString(ctx, "uuid")))
                        )
                        .executes(ctx -> executeUploadImage(ctx.getSource(), null))
                )
                ;
    }

    private static int executeInfo(FabricClientCommandSource source) {
        Guild guild = Mod.GUILDS.stream().filter(g -> g.id() == Mod.client.getSelectedGuild()).findAny().orElse(null);
        if (guild == null) {
            source.sendError(Text.translatable("generic.invalid_selected_guild"));
            return 0;
        }
        Set<GuildMember> members = Mod.guildMembers.getOrDefault(guild.id(), Collections.emptySet());
        source.sendFeedback(
                Text.translatable("generic.guild_info_header", Text.literal(guild.name()).formatted(Formatting.AQUA)).formatted(Formatting.GOLD)
        );
        source.sendFeedback(
                Text.translatable("generic.guild_members",
                        Text.literal(String.valueOf(members.size())).formatted(Formatting.RED),
                        Text.literal(String.valueOf(guild.capacity())).formatted(Formatting.RED)
                ).formatted(Formatting.GOLD)
        );
        Consumer<String> sendRole = (role) -> {
            List<MutableText> players =
                    members.stream()
                            .filter(m -> m.role().equals(role.toUpperCase(Locale.ROOT)))
                            .map(member -> {
                                if (member.presence() == null) {
                                    return Text.literal(member.name()).formatted(Formatting.WHITE);
                                }
                                if (System.currentTimeMillis() - member.presence().lastSeen < 60000) {
                                    return Text.literal(member.name()).formatted(Formatting.GREEN)
                                            .styled(style -> style.withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Text.translatable("generic.guild_presence_playing",
                                                                    Text.literal(member.presence().server).formatted(Formatting.YELLOW)
                                                            ).formatted(Formatting.GREEN))));
                                } else {
                                    return Text.literal(member.name()).formatted(Formatting.WHITE);
                                }
                            })
                            .toList();
            MutableText mu = Text.translatable(roleTranslationKey(role), Text.empty()).formatted(Formatting.GOLD);
            for (int i = 0; i < players.size(); i++) {
                mu = mu.append(players.get(i));
                if (i < players.size() - 1) {
                    mu = mu.append(Text.literal(", "));
                }
            }
            source.sendFeedback(mu);
        };
        sendRole.accept("Owner");
        sendRole.accept("Moderator");
        sendRole.accept("Member");
        source.sendFeedback(Text.empty());
        source.sendFeedback(
                Text.translatable("generic.guild_open",
                        Text.translatable(booleanTranslationKey(guild.open())).formatted(guild.open() ? Formatting.GREEN : Formatting.RED)
                ).formatted(Formatting.GOLD)
        );
        source.sendFeedback(
                Text.translatable("generic.guild_chat_format").formatted(Formatting.GOLD)
                        .append(Text.literal(guild.format())
                                .formatted(Formatting.WHITE)
                                .styled(style ->
                                        style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("generic.copy_on_click")))
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, guild.format()))
                                )
                        )
        );
        return members.size();
    }

    private static int executeFocus(FabricClientCommandSource source, String guildName) {
        Guild guild = Mod.GUILDS.stream().filter(g -> g.name().equalsIgnoreCase(guildName)).findAny().orElse(null);
        if (guild == null) {
            source.sendError(Text.translatable("generic.guild_not_found", guildName));
            return 0;
        }
        Mod.client.selectGuild(guild.id());
        source.sendFeedback(Text.translatable("generic.guild_focus_set", guild.name()));
        return 1;
    }

    private static int executeChat(FabricClientCommandSource source, String guildName, String message) {
        if (guildName != null) {
            Guild guild = Mod.GUILDS.stream().filter(g -> g.name().equalsIgnoreCase(guildName)).findAny().orElse(null);
            if (guild == null) {
                source.sendError(Text.translatable("generic.guild_not_found", guildName));
                return 0;
            }
            Mod.client.sendMessageToGuild(guild.id(), message);
        } else {
            Mod.client.sendMessageToGuild(null, message);
        }
        return 1;
    }

    private static void uploadImage(FabricClientCommandSource source, IOSupplier<byte[]> dataSupplier) {
        source.sendFeedback(Text.translatable("generic.uploading").formatted(Formatting.GRAY));
        Thread thread = new Thread(() -> {
            try {
                byte[] data = dataSupplier.get();
                if (data == null) {
                    MinecraftClient.getInstance().execute(() -> source.sendError(Text.translatable("generic.upload_image_failed", Text.translatable("generic.data_is_empty")).formatted(Formatting.RED)));
                    return;
                }
                JsonObject obj = GSON.fromJson(Mod.uploadImage(data), JsonObject.class);
                if (!obj.has("uuid")) {
                    MinecraftClient.getInstance().execute(() -> source.sendError(Text.translatable("generic.upload_image_failed", Text.literal(obj.toString())).formatted(Formatting.RED)));
                    return;
                }
                String imageUuid = obj.get("uuid").getAsString();
                Mod.client.sendMessageToGuild(null, "https://" + Mod.getEffectiveApiHost() + "/interchat/image?key=" + imageUuid);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        thread.setName("InterChat Upload Image Thread");
        thread.start();
    }

    private static int executeUploadImage(FabricClientCommandSource source, String uuid) {
        if (uuid == null) {
            uploadImage(source, ClipboardUtil::getImageFromClipboard);
            return 1;
        }
        File image = Mod.images.get(UUID.fromString(uuid));
        if (image == null) {
            return 0;
        }
        uploadImage(source, () -> Files.readAllBytes(image.toPath()));
        return 1;
    }

    private static String roleTranslationKey(String role) {
        return "generic.guild_role." + role.toLowerCase(Locale.ROOT);
    }

    private static String booleanTranslationKey(boolean value) {
        return "generic.boolean." + value;
    }
}
