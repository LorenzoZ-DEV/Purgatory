package it.vanixstudios.purgatory.listeners.bans;

import com.mongodb.client.MongoCollection;
import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.actionbar.ActionBarUtil;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.console.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

public class PlayerLoginListener implements Listener {

    private final BanManager banManager = BanManager.getInstance();
    private final MongoCollection<Document> blacklistCollection =
            Purgatory.getInstance().getMongoManager().getDatabase().getCollection("blacklist");

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // Check blacklist
        Document doc = blacklistCollection.find(new Document("uuid", player.getUniqueId().toString())).first();
        if (doc != null) {
            String reason = doc.getString("reason");
            player.disconnect( C.translate(Purgatory.getConfigManager().getMessages().getString("blacklist.blacklist_disconnect","&cYou have been blacklisted from the server. \n Reason: &e{reason}").replace("{reason}", reason)));
            return;
        }

        if (banManager != null && banManager.isBanned(player.getUniqueId())) {
            ActionBarUtil.sendJailActionBar(player);
        }
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (banManager != null && banManager.isBanned(player.getUniqueId())) {
            ServerInfo jailServer = Purgatory.getConfigManager().getConfig().getString("JAILSYSTEM.server-jail","Jail").equalsIgnoreCase("") ? null : ProxyServer.getInstance().getServerInfo("Jail");
            if (jailServer != null) {
                Logger.debug("Redirecting banned player " + player.getName() + " to Jail server.");
                event.setTarget(jailServer);
            } else {
                Logger.warning("WARNING: Server 'Jail' non trovato nella configurazione!");
            }
        }
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (banManager.isBanned(player.getUniqueId())) {
            ServerInfo jailServer = Purgatory.getConfigManager().getConfig().getString("JAILSYSTEM.server-jail","").equalsIgnoreCase("") ? null : ProxyServer.getInstance().getServerInfo("Jail");

            if (jailServer != null && !player.getServer().getInfo().getName().equalsIgnoreCase("Jail")) {
                Logger.warning("Banned player " + player.getName() + " tried to switch to " +
                        player.getServer().getInfo().getName() + ". Redirecting back to Jail.");
                player.connect(jailServer);
            }
        }
    }
}
