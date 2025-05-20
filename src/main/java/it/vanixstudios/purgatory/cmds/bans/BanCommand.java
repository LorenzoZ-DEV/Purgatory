package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.UUID;

public class BanCommand {

    private final BanManager banManager;

    public BanCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    @Command("ban")
    @CommandPermission("purgatory.ban")
    @Usage("ban <player> [reason] [-p|-s]")
    public void ban(BungeeCommandActor actor,
                    ProxiedPlayer target,
                    @Optional String reason,
                    @Optional String flag) {

        if (target == null) {
            actor.reply(C.translate("&cTarget player not found."));
            return;
        }

        UUID uuid = target.getUniqueId();
        String playerName = target.getName();

        if (banManager.isBanned(uuid)) {
            actor.reply(C.translate("&cPlayer &e" + playerName + " &cis already banned."));
            return;
        }

        String finalReason = (reason == null || reason.startsWith("-")) ? "No reason specified." : reason;

        boolean publicBan = "-p".equalsIgnoreCase(flag);
        boolean silentBan = "-s".equalsIgnoreCase(flag) || !publicBan;

        banManager.ban(uuid, playerName, finalReason);
        banManager.sendToJail(target);

        target.disconnect(C.translate("""
                &c&lYour account has been suspended
                &c&lfrom the &c&lX-Network&c.
                
                &4Purchase an unban @ store.x-network.org
                
                &7Reason: """ + finalReason));

        actor.reply(C.translate("&aPlayer &e" + playerName + " &ahas been permanently banned."));

        String broadcastMessage = C.translate(String.format(
                "&7[Silent] &e%s &chas been permanently banned by &e%s&c. Reason: &e%s",
                playerName, actor.name(), finalReason
        ));

        if (publicBan) {
            ProxyServer.getInstance().broadcast(broadcastMessage);
        } else {
            notifyStaff(broadcastMessage);
        }
    }

    private void notifyStaff(String message) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("purgatory.notifications"))
                .forEach(p -> p.sendMessage(C.translate(message)));
    }
}
