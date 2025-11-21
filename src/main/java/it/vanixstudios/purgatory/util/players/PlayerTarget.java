package it.vanixstudios.purgatory.util.players;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.UUID;

/**
 * Rappresenta un bersaglio (player) risolto: se online contiene il riferimento
 * a ProxiedPlayer, altrimenti solo nome, uuid e ip placeholder.
 */
public class PlayerTarget {
    private final String name;
    private final UUID uuid;
    private final ProxiedPlayer online;
    private final String ip;

    public PlayerTarget(String name, UUID uuid, ProxiedPlayer online, String ip) {
        this.name = name;
        this.uuid = uuid;
        this.online = online;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ProxiedPlayer getOnline() {
        return online;
    }

    public boolean isOnline() {
        return online != null && online.isConnected();
    }

    public String getIp() {
        return ip;
    }
}
