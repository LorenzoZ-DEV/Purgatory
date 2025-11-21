package it.vanixstudios.purgatory.util.players;

import com.mongodb.client.MongoCollection;
import it.vanixstudios.purgatory.Purgatory;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility centrale per risolvere giocatori (online/offline) e generare suggerimenti.
 */
public final class PlayerTargets {

    private PlayerTargets() {}

    public static UUID offlineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static List<String> online(String prefix) {
        String lp = prefix == null ? "" : prefix.toLowerCase();
        return ProxyServer.getInstance().getPlayers().stream()
                .map(ProxiedPlayer::getName)
                .filter(name -> name.toLowerCase().startsWith(lp))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * Lista di suggerimenti unificata: online + bans + mutes + blacklist + profili
     */
    public static List<String> suggest(String prefix) {
        String lp = prefix == null ? "" : prefix.toLowerCase();
        LinkedHashSet<String> names = new LinkedHashSet<>();

        // Online
        ProxyServer.getInstance().getPlayers().forEach(p -> names.add(p.getName()));

        // Bans
        MongoCollection<Document> bans = Purgatory.getInstance().getBanManager().getBansCollection();
        for (Document d : bans.find()) {
            String n = d.getString("name");
            if (n != null) names.add(n);
        }

        // Mutes
        MongoCollection<Document> mutes = Purgatory.getInstance().getMongoManager().getDatabase().getCollection("mutes");
        for (Document d : mutes.find()) {
            String uuidStr = d.getString("uuid");
            if (uuidStr != null && uuidStr.length() == 36) {
                try {
                    UUID u = UUID.fromString(uuidStr);
                    var prof = Purgatory.getInstance().getProfileManager().getProfileMap().get(u);
                    if (prof != null) names.add(prof.getName());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Blacklist
        MongoCollection<Document> bl = Purgatory.getInstance().getMongoManager().getDatabase().getCollection("blacklist");
        for (Document d : bl.find()) {
            String n = d.getString("name");
            if (n != null) names.add(n);
        }

        // Profili
        Purgatory.getInstance().getProfileManager().getProfileMap().values().forEach(p -> names.add(p.getName()));

        return names.stream()
                .filter(n -> n != null && n.toLowerCase().startsWith(lp))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
