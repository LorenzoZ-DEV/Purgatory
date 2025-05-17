package it.vanixstudios.purgatory.tasks;

import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.ActionBarUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BanActionBarTask implements Runnable {

    private final BanManager banManager;

    public BanActionBarTask(BanManager banManager) {
        this.banManager = banManager;
    }

    @Override
    public void run() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (banManager.isBanned(player.getUniqueId())) {
                ActionBarUtil.sendJailActionBar(player);
            }
        }
    }
}
