package it.vanixstudios.purgatory.manager.mute;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.UUID;

public class MuteManager {

    private final MongoCollection<Document> collection;

    public MuteManager(MongoDatabase database) {
        this.collection = database.getCollection("mutes");
    }

    public void mutePlayer(UUID uuid, String reason) {
        unmutePlayer(uuid);

        Document document = new Document("uuid", uuid.toString())
                .append("reason", reason)
                .append("expiresAt", 0L);

        collection.insertOne(document);
    }

    public void tempMutePlayer(UUID uuid, String reason, long duration) {
        unmutePlayer(uuid);

        long expiresAt = System.currentTimeMillis() + duration;

        Document document = new Document("uuid", uuid.toString())
                .append("reason", reason)
                .append("expiresAt", expiresAt);

        collection.insertOne(document);
    }

    public void unmutePlayer(UUID uuid) {
        collection.deleteOne(new Document("uuid", uuid.toString()));
    }

    public boolean isMuted(UUID uuid) {
        Document doc = collection.find(new Document("uuid", uuid.toString())).first();
        if (doc == null) return false;

        long expiresAt = doc.getLong("expiresAt");

        if (expiresAt == 0) return true; // permanent
        if (System.currentTimeMillis() > expiresAt) {
            unmutePlayer(uuid);
            return false;
        }

        return true;
    }

    public String getMuteReason(UUID uuid) {
        Document doc = collection.find(new Document("uuid", uuid.toString())).first();
        if (doc == null) return null;
        return doc.getString("reason");
    }

    public long getTempMuteEnd(UUID uuid) {
        Document doc = collection.find(new Document("uuid", uuid.toString())).first();
        if (doc == null) return 0;
        return doc.getLong("expiresAt");
    }

    public boolean isTempMuted(UUID uuid) {
        Document doc = collection.find(new Document("uuid", uuid.toString())).first();
        if (doc == null) return false;
        long expiresAt = doc.getLong("expiresAt");
        return expiresAt > 0;
    }
}
