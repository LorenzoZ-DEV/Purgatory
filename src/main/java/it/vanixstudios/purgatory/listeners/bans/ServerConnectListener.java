package it.vanixstudios.purgatory.listeners.bans;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.util.console.Logger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.event.EventHandler;

public class ServerConnectListener implements Listener {

    private final BanManager banManager = BanManager.getInstance();

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (banManager.isBanned(player.getUniqueId())) {
            ServerInfo jailServer = Purgatory.getConfigManager().getConfig().getString("JAILSYSTEM.server-jail","Jail").equalsIgnoreCase("") ? null : ProxyServer.getInstance().getServerInfo("Jail");
            if (jailServer != null) {
                event.setTarget(jailServer);
            } else {
                Logger.warning("WARNING: Server 'Jail' was not found in BungeeCord's configuration!");
            }
        }
    }
}
