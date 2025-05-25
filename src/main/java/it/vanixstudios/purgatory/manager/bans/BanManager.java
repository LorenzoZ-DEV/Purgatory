package it.vanixstudios.purgatory.manager.bans;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.vanixstudios.purgatory.Purgatory;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

public class BanManager {

    private final MongoCollection<Document> bans;

    public BanManager(MongoCollection<Document> bans) {
        if (bans == null) {
            throw new IllegalArgumentException("MongoCollection<Document> bans cannot be null");
        }
        this.bans = bans;
    }

    public void ban(UUID uuid, String name, String reason) {
        Document doc = new Document("uuid", uuid.toString())
                .append("name", name)
                .append("permanent", true)
                .append("until", null)
                .append("bannedAt", new Date())
                .append("reason", reason);
        bans.insertOne(doc);
    }

    public void tempBan(UUID uuid, String name, long durationMillis, String reason) {
        Date until = new Date(System.currentTimeMillis() + durationMillis);
        Document doc = new Document("uuid", uuid.toString())
                .append("name", name)
                .append("permanent", false)
                .append("until", until)
                .append("bannedAt", new Date())
                .append("reason", reason);
        bans.insertOne(doc);
    }

    public boolean isBanned(UUID uuid) {
        Document doc = bans.find(eq("uuid", uuid.toString()))
                .sort(descending("bannedAt"))
                .first();
        if (doc == null) return false;

        boolean permanent = doc.getBoolean("permanent", false);
        if (permanent) return true;

        Date until = doc.getDate("until");
        return until != null && until.after(new Date());
    }

    public void unban(UUID uuid) {
        bans.deleteMany(eq("uuid", uuid.toString()));
    }

    public void sendToJail(ProxiedPlayer player) {
        if (player != null && player.isConnected()) {
            player.connect(Purgatory.getInstance().getProxy().getServerInfo("Jail"));
        }
    }

    public void removeFromJail(ProxiedPlayer player) {
        ServerInfo mainServer = ProxyServer.getInstance().getServerInfo("Hub-01");

        if (mainServer == null) {
            System.err.println("Server 'Hub-01' non trovato! Controlla la configurazione di BungeeCord.");
            return;
        }

        player.connect(mainServer);
    }

    public String getBanReason(UUID uuid) {
        Document doc = bans.find(eq("uuid", uuid.toString()))
                .sort(descending("bannedAt"))
                .first();
        if (doc == null) return null;
        return doc.getString("reason");
    }

    public List<String> getBanHistory(UUID uuid) {
        List<String> result = new ArrayList<>();
        try (MongoCursor<Document> cursor = bans.find(eq("uuid", uuid.toString()))
                .sort(descending("bannedAt"))
                .iterator()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                boolean permanent = doc.getBoolean("permanent", false);
                Date bannedAt = doc.getDate("bannedAt");
                Date until = doc.getDate("until");
                String reason = doc.getString("reason");

                String type = permanent ? "Permanent" : "Temporary";
                String durationStr = permanent ? "-" : formatDuration(until.getTime() - bannedAt.getTime());
                String dateStr = bannedAt != null ? sdf.format(bannedAt) : "Unknown date";

                result.add(String.format("%s ban on %s for %s. Reason: %s", type, dateStr, durationStr, reason));
            }
        }
        return result;
    }
    public BanInfo getLatestBanInfo(UUID uuid) {
        Document doc = bans.find(eq("uuid", uuid.toString()))
                .sort(descending("bannedAt"))
                .first();
        if (doc == null) return null;

        boolean permanent = doc.getBoolean("permanent", false);
        Date bannedAtDate = doc.getDate("bannedAt");
        long bannedAt = bannedAtDate != null ? bannedAtDate.getTime() : 0;
        long duration;

        if (permanent) {
            duration = 0;
        } else {
            Date untilDate = doc.getDate("until");
            duration = untilDate != null ? untilDate.getTime() - bannedAt : 0;
        }

        String reason = doc.getString("reason");

        return new BanInfo(reason, bannedAt, duration);
    }


    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }
    public MongoCollection<Document> getBansCollection() {
        return this.bans;
    }
    public UUID getOrCreateUUID(String playerName) {
        ProxiedPlayer onlinePlayer = ProxyServer.getInstance().getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }

    public BanInfo getBannedBy(UUID uuid) {
        if (uuid == null) return null;

        Document doc = bans.find(eq("uuid", uuid.toString()))
                .sort(descending("bannedAt"))
                .first();
        if (doc == null) return null;

        String reason = doc.getString("reason");
        Date bannedAtDate = doc.getDate("bannedAt");
        long bannedAt = bannedAtDate != null ? bannedAtDate.getTime() : 0;
        boolean permanent = doc.getBoolean("permanent", false);
        long duration = permanent ? 0 : (doc.getDate("until") != null ? doc.getDate("until").getTime() - bannedAt : 0);

        return new BanInfo(reason, bannedAt, duration);
    }





    public static BanManager getInstance() {
        return Purgatory.getInstance().getBanManager();
    }
}
