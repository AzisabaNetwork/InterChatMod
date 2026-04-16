package net.azisaba.interchatmod.common.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.azisaba.interchatmod.common.InterChatMod;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface InterChatCommand {
    @NotNull ActorLiteralArgumentBuilder builder(@NotNull InterChatMod mod);

    static @NotNull String roleTranslationKey(@NotNull String role) {
        return "generic.guild_role." + role.toLowerCase(Locale.ROOT);
    }

    static @NotNull String booleanTranslationKey(boolean value) {
        return "generic.boolean." + value;
    }

    static @NotNull CompletableFuture<Suggestions> suggestMatching(@NotNull Iterable<@NotNull String> iterable, @NotNull SuggestionsBuilder suggestionsBuilder) {
        String lowercase = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String s : iterable) {
            if (checkPrefix(lowercase, s.toLowerCase(Locale.ROOT))) {
                suggestionsBuilder.suggest(s);
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    static @NotNull CompletableFuture<Suggestions> suggestMatching(@NotNull Stream<@NotNull String> stream, @NotNull SuggestionsBuilder suggestionsBuilder) {
        String lowercase = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        stream.filter((s) -> checkPrefix(lowercase, s.toLowerCase(Locale.ROOT))).forEach(suggestionsBuilder::suggest);
        return suggestionsBuilder.buildFuture();
    }

    static @NotNull CompletableFuture<Suggestions> suggestMatching(@NotNull String @NotNull [] args, @NotNull SuggestionsBuilder suggestionsBuilder) {
        String lowercase = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String s : args) {
            if (checkPrefix(lowercase, s.toLowerCase(Locale.ROOT))) {
                suggestionsBuilder.suggest(s);
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    /**
     * Checks if a given string (`prefix`) matches a prefix of another string (`s`)
     * in a case where underscores ('_') delimit potential starting points for the prefix.
     * This method iterates through `s`, trying to find an occurrence of `prefix` as
     * a prefix starting at any segment following underscores.
     *
     * @param prefix the prefix string to check for. It is the target prefix that is searched in `s`.
     * @param s the string to be searched, which may contain multiple segments delimited by underscores.
     * @return {@code true} if `prefix` is a prefix of any segment in `s`, starting at an underscore;
     *         {@code false} otherwise.
     */
    static boolean checkPrefix(@NotNull String prefix, @NotNull String s) {
        for (int i = 0; !s.startsWith(prefix, i); ++i) {
            i = s.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }
}
