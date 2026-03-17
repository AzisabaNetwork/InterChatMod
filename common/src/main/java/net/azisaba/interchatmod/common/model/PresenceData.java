package net.azisaba.interchatmod.common.model;

public class PresenceData {
    public final String server;
    public final long lastSeen;

    public PresenceData(String server, long lastSeen) {
        this.server = server;
        this.lastSeen = lastSeen;
    }
}
