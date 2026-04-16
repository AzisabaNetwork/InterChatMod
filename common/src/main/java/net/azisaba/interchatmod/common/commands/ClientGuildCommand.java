package net.azisaba.interchatmod.common.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.command.ActorLiteralArgumentBuilder;
import net.azisaba.interchatmod.common.command.InterChatCommand;
import net.azisaba.interchatmod.common.entity.Actor;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.common.util.ClipboardUtil;
import net.azisaba.interchatmod.common.util.Constants;
import net.azisaba.interchatmod.common.util.function.IOSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.azisaba.interchatmod.common.command.CommandManager.argument;
import static net.azisaba.interchatmod.common.command.CommandManager.literal;

public class ClientGuildCommand implements InterChatCommand {
    private static final Gson GSON = new Gson();

    @Override
    public @NotNull ActorLiteralArgumentBuilder builder(@NotNull InterChatMod mod) {
        return literal(mod, "cguild")
                .then(literal(mod, "invite")
                        .then(argument(mod, "player", StringArgumentType.word())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().invite(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "accept")
                        .then(argument(mod, "guild", StringArgumentType.word())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().respondInvite(StringArgumentType.getString(ctx, "guild"), true);
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "reject")
                        .then(argument(mod, "guild", StringArgumentType.word())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().respondInvite(StringArgumentType.getString(ctx, "guild"), false);
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "nick")
                        .executes(ctx -> {
                            mod.getWebSocketChatClient().nick(null);
                            return 0;
                        })
                        .then(argument(mod, "nickname", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().nick(StringArgumentType.getString(ctx, "nickname"));
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "jp-on")
                        .executes(ctx -> {
                            mod.getWebSocketChatClient().toggleTranslate(true);
                            return 0;
                        })
                )
                .then(literal(mod, "jp-off")
                        .executes(ctx -> {
                            mod.getWebSocketChatClient().toggleTranslate(false);
                            return 0;
                        })
                )
                .then(literal(mod, "role")
                        .then(argument(mod, "member", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Set<GuildMember> set = mod.getGuildMembers().getOrDefault(mod.getWebSocketChatClient().getSelectedGuild(), Collections.emptySet());
                                    return InterChatCommand.suggestMatching(set.stream().map(GuildMember::name), builder);
                                })
                                .then(argument(mod, "role", StringArgumentType.word())
                                        .suggests((ctx, builder) -> InterChatCommand.suggestMatching(Stream.of("owner", "moderator", "member"), builder))
                                        .executes(ctx -> {
                                            mod.getWebSocketChatClient().role(StringArgumentType.getString(ctx, "member"), StringArgumentType.getString(ctx, "role"));
                                            return 0;
                                        })
                                )
                        )
                )
                .then(literal(mod, "toggleinvites")
                        .executes(ctx -> {
                            mod.getWebSocketChatClient().toggleInvites();
                            return 0;
                        })
                )
                .then(literal(mod, "hide-guild")
                        .executes(ctx -> {
                            mod.getWebSocketChatClient().hideGuild();
                            return 0;
                        })
                )
                .then(literal(mod, "hideall")
                        .executes(ctx -> {
                            mod.getWebSocketChatClient().hideAll("");
                            return 0;
                        })
                        .then(argument(mod, "duration", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().hideAll(StringArgumentType.getString(ctx, "duration"));
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "format")
                        .then(argument(mod, "format", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> InterChatCommand.suggestMatching(Constants.FORMAT_VARIABLES.stream(), builder))
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().format(StringArgumentType.getString(ctx, "format"));
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "info").executes(ctx -> executeInfo(mod, ctx.getSource())))
                .then(literal(mod, "block")
                        .then(argument(mod, "player", StringArgumentType.word())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().block(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "unblock")
                        .then(argument(mod, "player", StringArgumentType.word())
                                .executes(ctx -> {
                                    mod.getWebSocketChatClient().unblock(StringArgumentType.getString(ctx, "player"));
                                    return 0;
                                })
                        )
                )
                .then(literal(mod, "tell")
                        .then(argument(mod, "player", StringArgumentType.word())
                                .suggests((ctx, builder) -> InterChatCommand.suggestMatching(mod.getKnownPlayers().stream(), builder))
                                .then(argument(mod, "message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            mod.getWebSocketChatClient().tell(StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message"));
                                            mod.getKnownPlayers().add(StringArgumentType.getString(ctx, "player"));
                                            return 0;
                                        })
                                )
                        )
                )
                .then(literal(mod, "uploadimage")
                        .then(argument(mod, "id", StringArgumentType.string())
                                .executes(ctx -> executeUploadImage(mod, ctx.getSource(), StringArgumentType.getString(ctx, "id")))
                        )
                )
                ;
    }

    private static int executeInfo(InterChatMod mod, Actor source) {
        Guild guild = mod.getGuilds().stream().filter(g -> g.id() == mod.getWebSocketChatClient().getSelectedGuild()).findAny().orElse(null);
        if (guild == null) {
            source.sendError(Component.translatable("generic.invalid_selected_guild"));
            return 0;
        }
        Set<GuildMember> members = mod.getGuildMembers().getOrDefault(guild.id(), Collections.emptySet());
        source.sendFeedback(
                Component.translatable("generic.guild_info_header", Component.text(guild.name(), NamedTextColor.AQUA)).color(NamedTextColor.GOLD)
        );
        source.sendFeedback(
                Component.translatable("generic.guild_members",
                        Component.text(String.valueOf(members.size())).color(NamedTextColor.RED),
                        Component.text(String.valueOf(guild.capacity())).color(NamedTextColor.RED)
                ).color(NamedTextColor.GOLD)
        );
        Consumer<String> sendRole = (role) -> {
            List<Component> players =
                    members.stream()
                            .filter(m -> m.role().equals(role.toUpperCase(Locale.ROOT)))
                            .map(member -> {
                                if (member.presence() == null) {
                                    return Component.text(member.name()).color(NamedTextColor.WHITE);
                                }
                                if (System.currentTimeMillis() - member.presence().lastSeen < 60000) {
                                    return Component.text(member.name())
                                            .color(NamedTextColor.GREEN)
                                            .hoverEvent(HoverEvent.showText(
                                                    Component.translatable("generic.guild_presence_playing", Component.text(member.presence().server, NamedTextColor.YELLOW)).color(NamedTextColor.GREEN)));
                                } else {
                                    return Component.text(member.name()).color(NamedTextColor.WHITE);
                                }
                            })
                            .collect(Collectors.toList());
            source.sendFeedback(Component.translatable(InterChatCommand.roleTranslationKey(role), Component.join(JoinConfiguration.separator(Component.text(", ")), players)).color(NamedTextColor.GOLD));
        };
        sendRole.accept("Owner");
        sendRole.accept("Moderator");
        sendRole.accept("Member");
        source.sendFeedback(Component.empty());
        source.sendFeedback(
                Component.translatable("generic.guild_open",
                        Component.translatable(InterChatCommand.booleanTranslationKey(guild.open())).color(guild.open() ? NamedTextColor.GREEN : NamedTextColor.RED)
                ).color(NamedTextColor.GOLD)
        );
        source.sendFeedback(
                Component.translatable("generic.guild_chat_format").color(NamedTextColor.GOLD)
                        .append(Component.text(guild.format())
                                .color(NamedTextColor.WHITE)
                                .hoverEvent(HoverEvent.showText(Component.translatable("generic.copy_on_click")))
                                .clickEvent(ClickEvent.copyToClipboard(guild.format()))
                        )
        );
        return members.size();
    }

    private static int executeUploadImage(InterChatMod mod, Actor source, String id) {
        if (id == null) {
            uploadImage(mod, source, ClipboardUtil::getImageFromClipboard);
            return 1;
        }
        File image = mod.getScreenshots().get(id);
        if (image == null) {
            return 0;
        }
        uploadImage(mod, source, () -> Files.readAllBytes(image.toPath()));
        return 1;
    }

    private static void uploadImage(InterChatMod mod, Actor source, IOSupplier<byte[]> dataSupplier) {
        source.sendFeedback(Component.translatable("generic.uploading", NamedTextColor.GRAY));
        Thread thread = new Thread(() -> {
            try {
                byte[] data = dataSupplier.get();
                if (data == null) {
                    mod.execute(() -> source.sendError(Component.translatable("generic.upload_image_failed", Component.translatable("generic.data_is_empty")).color(NamedTextColor.RED)));
                    return;
                }
                JsonObject obj = GSON.fromJson(mod.uploadImage(data), JsonObject.class);
                if (!obj.has("uuid")) {
                    mod.execute(() -> source.sendError(Component.translatable("generic.upload_image_failed", Component.text(obj.toString())).color(NamedTextColor.RED)));
                    return;
                }
                String imageUuid = obj.get("uuid").getAsString();
                mod.getWebSocketChatClient().sendMessageToGuild(null, "https://" + mod.getConfig().getEffectiveApiHost() + "/interchat/image?key=" + imageUuid);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.setName("InterChat Upload Image Thread");
        thread.start();
    }
}
