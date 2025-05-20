package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.TimeUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.UUID;

public class TempbanCommand {

    private final BanManager banManager;

    public TempbanCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    @Command("tempban")
    @CommandPermission("purgatory.ban")
    @Usage("tempban <player> <duration> <reason> [-p|-s]")
    public void executeTempban(BungeeCommandActor actor, String playerName, String durationArg, @Optional String reason) {
        UUID uuid = banManager.getOrCreateUUID(playerName);

        if (banManager.isBanned(uuid)) {
            actor.reply(C.translate("&cPlayer &e" + playerName + " &cis already banned."));
            return;
        }

        long duration;
        try {
            duration = TimeUtil.parseTime(durationArg);
        } catch (IllegalArgumentException e) {
            actor.reply(C.translate("&cInvalid duration. Use formats like 1s, 5m, 2h, 3d, 1w, 1mo, 1y."));
            return;
        }

        if (reason == null) reason = "";

        boolean silent = true;
        if (reason.contains("-p")) {
            silent = false;
        }

        String finalReason = reason.replace("-p", "").replace("-s", "").trim();
        if (finalReason.isEmpty()) finalReason = "No specific reason provided.";

        banManager.tempBan(uuid, playerName, duration, finalReason);

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target != null && target.isConnected()) {
            banManager.sendToJail(target);
            target.disconnect(C.translate("""
                    &c&lYour account has been temporarily suspended
                    &c&lfrom the &c&lX-Network&c.
                    &c&lYou have been banned for %duration%

                    &4Purchase an unban @ store.x-network.org

                    &7Reason: %reason%
                    """
                    .replace("%duration%", durationArg)
                    .replace("%reason%", finalReason)));
        }

        actor.reply(C.translate("&aPlayer &e" + playerName + " &ahas been tempbanned for &e" + durationArg + "&a."));

        String notification = String.format("&e%s &chas been tempbanned by &e%s &cfor &e%s&c. Reason: &e%s",
                playerName, actor.name(), durationArg, finalReason);

        if (silent) {
            notifyStaff("&7[Silent] " + notification);
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + notification));
        }
    }

    private void notifyStaff(String message) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("purgatory.notifications"))
                .forEach(p -> p.sendMessage(C.translate(message)));
    }
}
