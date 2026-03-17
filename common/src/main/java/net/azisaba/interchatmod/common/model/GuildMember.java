package net.azisaba.interchatmod.common.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class GuildMember {
    private final long guildId;
    private final @NotNull UUID uuid;
    private final @NotNull String name;
    private final @NotNull String role;
    private final @Nullable String nickname;
    private final @Nullable PresenceData presence;

    public GuildMember(long guildId, @NotNull UUID uuid, @NotNull String name, @NotNull String role, @Nullable String nickname, @Nullable PresenceData presence) {
        this.guildId = guildId;
        this.uuid = uuid;
        this.name = name;
        this.role = role;
        this.nickname = nickname;
        this.presence = presence;
    }

    @NotNull
    public static Set<GuildMember> getGuildMembersFromArray(JsonArray arr) {
        Set<GuildMember> set = new HashSet<>();
        for (JsonElement element : arr) {
            JsonObject obj = element.getAsJsonObject();
            PresenceData presence = null;
            if (obj.has("presence")) {
                presence = new PresenceData(
                        obj.get("presence").getAsJsonObject().get("server").getAsString(),
                        obj.get("presence").getAsJsonObject().get("last_seen").getAsLong()
                );
            }
            set.add(
                    new GuildMember(
                            obj.get("guild_id").getAsLong(),
                            UUID.fromString(obj.get("uuid").getAsString()),
                            obj.get("name").getAsString(),
                            obj.get("role").getAsString(),
                            obj.get("nickname").isJsonNull() ? null : obj.get("nickname").getAsString(),
                            presence
                    )
            );
        }
        return set;
    }

    public long guildId() {
        return guildId;
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String role() {
        return role;
    }

    public @Nullable String nickname() {
        return nickname;
    }

    public @Nullable PresenceData presence() {
        return presence;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        GuildMember that = (GuildMember) obj;
        return this.guildId == that.guildId &&
                Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.role, that.role) &&
                Objects.equals(this.nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, uuid, name, role, nickname);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "GuildMember[" +
                "guildId=" + guildId + ", " +
                "uuid=" + uuid + ", " +
                "name=" + name + ", " +
                "role=" + role + ", " +
                "nickname=" + nickname + ']';
    }
}
