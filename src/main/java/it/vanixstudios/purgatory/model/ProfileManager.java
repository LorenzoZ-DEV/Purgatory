package it.vanixstudios.purgatory.model;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.storage.MongoManager;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProfileManager {

    private final Purgatory instance;
    private final Map<UUID, Profile> profileMap = new ConcurrentHashMap<>();

    public ProfileManager(Purgatory instance) {
        this.instance = instance;
    }

    public void load() {
        MongoManager mongoManager = instance.getMongoManager();

        for (Document document : mongoManager.getDatabase().getCollection("profiles").find()) {
            Profile profile = fromBson(document);

            profileMap.put(profile.getUuid(), profile);
        }
    }

    private Profile fromBson(Document document) {
        Profile profile = new Profile(UUID.fromString(document.getString("uuid")), document.getString("name"));

        profile.setLastIP(document.getString("lastIP"));
        profile.getIps().addAll(document.getList("ips", String.class));
        List<String> altsAsStrings = document.getList("alts", String.class);
        if (altsAsStrings != null) {
            profile.getAlts().addAll(
                    altsAsStrings.stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList())
            );
        }

        return profile;
    }

    private Document toBson(Profile profile) {
        List<String> altsAsStrings = profile.getAlts().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());

        return new Document("uuid", profile.getUuid().toString())
                .append("name", profile.getName())
                .append("lastIP", profile.getLastIP())
                .append("ips", profile.getIps())
                .append("alts", altsAsStrings);
    }

    public Profile getProfile(UUID uuid) {
        return profileMap.get(uuid);
    }

    public void save(Profile profile) {
        profileMap.putIfAbsent(profile.getUuid(), profile);
    }

    public void close() {
        for (Profile profile : profileMap.values()) {
            Document document = toBson(profile);

            instance.getMongoManager().getDatabase().getCollection("profiles").updateOne(
                    Filters.eq("uuid", profile.getUuid().toString()),
                    new Document("$set", document),
                    new UpdateOptions().upsert(true)
            );
        }
    }

    public Purgatory getInstance() {
        return this.instance;
    }

    public Map<UUID, Profile> getProfileMap() {
        return this.profileMap;
    }
}
