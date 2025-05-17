package it.vanixstudios.purgatory.listeners.evasion;

import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.Purgatory;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.Collection;

public class BanEvadeListener implements Listener {

    private final BanManager banManager = Purgatory.getInstance().getBanManager();

    @EventHandler
    public void onPlayerPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        String playerIP = player.getSocketAddress().toString();

        if (banManager.isBanned(player.getUniqueId())) {
            return;
        }

        Document query = new Document("ip", playerIP)
                .append("banned", true)
                .append("uuid", new Document("$ne", playerUUID));

        Document evadingBan = Purgatory.getInstance().getMongoManager().getDatabase()
                .getCollection("bans")
                .find(query)
                .first();

        if (evadingBan != null) {
            String bannedUUID = evadingBan.getString("uuid");
            String bannedName = evadingBan.getString("name");
            String reason = evadingBan.getString("reason");

            Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
            for (ProxiedPlayer p : players) {
                if (p.hasPermission("purgatory.staff")) {
                    p.sendMessage("§c[ALERT] Ban Evade detected! Player §f" + player.getName() +
                            " §chas IP §f" + playerIP + " §cused by banned player §f" + bannedName +
                            " §cfor reason: §f" + reason);
                }
            }
        }
    }
}
