package net.azisaba.interchatmod.common.command;

import com.mojang.brigadier.arguments.ArgumentType;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.commands.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class CommandManager {
    private static final List<InterChatCommand> COMMANDS;

    static {
        List<InterChatCommand> list = new ArrayList<>();
        list.add(new ClientGuildTellCommand());
        list.add(new ClientGuildSelectCommand());
        list.add(new ClientGuildShortCommand());
        list.add(new ClientGuildCommand());
        list.add(new ReconnectInterChatCommand());
        COMMANDS = Collections.unmodifiableList(list);
    }

    private CommandManager() {
    }

    public static @NotNull ActorLiteralArgumentBuilder literal(@NotNull InterChatMod mod, @NotNull String name) {
        return ActorLiteralArgumentBuilder.literal(mod, name);
    }

    public static <T> @NotNull ActorRequiredArgumentBuilder<T> argument(@NotNull InterChatMod mod, @NotNull String name, @NotNull ArgumentType<T> type) {
        return ActorRequiredArgumentBuilder.argument(mod, name, type);
    }

    public static @NotNull List<@NotNull InterChatCommand> getCommands() {
        return COMMANDS;
    }

    public static void forEachCommand(@NotNull Consumer<@NotNull InterChatCommand> consumer) {
        COMMANDS.forEach(consumer);
    }
}
