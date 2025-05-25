package it.vanixstudios.purgatory.listeners.bans;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.bson.Document;

public class BlacklistListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent event) {
        String playerName = event.getConnection().getName();
        String playerUUID = event.getConnection().getUniqueId().toString();
        String playerIP = event.getConnection().getAddress().getAddress().getHostAddress();

        MongoCollection<Document> blacklistCollection = Purgatory.getInstance()
                .getMongoManager()
                .getDatabase()
                .getCollection("blacklist");

        // Controlla se il giocatore è blacklistato per UUID, nome o IP
        Document blacklistEntry = blacklistCollection.find(
                Filters.or(
                        Filters.eq("uuid", playerUUID),
                        Filters.eq("name", playerName),
                        Filters.eq("ip", playerIP)
                )
        ).first();

        if (blacklistEntry != null) {
            String reason = blacklistEntry.getString("reason");
            String issuer = blacklistEntry.getString("issuer");

            if (reason == null) reason = "No specific reason provided";
            if (issuer == null) issuer = "Console";

            String kickMessage = C.translate(Purgatory.getConfigManager().getMessages().getString(
                            "blacklist.blacklist_disconnect",
                            "&cYou have been blacklisted from the server.\n&cReason: &e{reason}\n&cIssued by: &e{issuer}")
                    .replace("{reason}", reason)
                    .replace("{issuer}", issuer));

            event.setCancelled(true);
            event.setCancelReason(kickMessage);

            // Log del tentativo di accesso
            Purgatory.getInstance().getLogger().info(
                    "Blocked blacklisted player " + playerName + " (" + playerUUID + ") from IP " + playerIP
            );
        }
    }
}