package it.vanixstudios.purgatory.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Profile {
    private final UUID uuid;
    private final String name;
    private final Set<String> ips = new HashSet<>();
    private final Set<UUID> alts = new HashSet<>();

    private String lastIP = "127.0.0.1";

    public Profile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public Set<String> getIps() {
        return this.ips;
    }

    public Set<UUID> getAlts() {
        return this.alts;
    }

    public String getLastIP() {
        return this.lastIP;
    }

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }
}