package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.util.duration.TimeUtil;
import it.vanixstudios.purgatory.util.players.PlayerTargets;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.UUID;

/**
 * Command dedicated to IP-based bans.
 */
public class BanIpCommand {

    private final BanManager banManager;

    public BanIpCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    @Command({"banip", "ipban"})
    @CommandPermission("purgatory.ban.ip")
    @Description("Ban a player and store their IP")
    @Usage("banip <player> [reason] [-p|-s]")
    public void banIp(BungeeCommandActor actor,
                      @Named("player") String playerName,
                      @Optional String durationOrReason,
                      @Optional String reason) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        UUID uuid = target != null ? target.getUniqueId() : PlayerTargets.offlineUUID(playerName);

        if (banManager.isBanned(uuid)) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.already_banned", "&e{target} &calready banned").replace("{target}", playerName)));
            return;
        }

        long duration = 0L;
        String parsedReason = reason;
        if (durationOrReason != null && durationOrReason.matches("\\d+[smhd]")) {
            try {
                duration = TimeUtil.parseTime(durationOrReason);
            } catch (IllegalArgumentException e) {
                actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.invalid_duration", "&cInvalid duration. Use formats like 10m, 1h, 2d.")));
                return;
            }
        } else {
            parsedReason = durationOrReason;
        }

        if (parsedReason == null || parsedReason.isEmpty()) parsedReason = duration > 0 ? "Temporary ban" : "No reason specified.";

        boolean silent = parsedReason.contains("-p");
        String finalReason = parsedReason.replace("-p", "").replace("-s", "").trim();
        if (finalReason.isEmpty()) finalReason = duration > 0 ? "Temporary ban" : "No reason specified.";

        String ip = target != null ? extractIp(target) : "";
        String actorName = actor.name();

        if (duration > 0) {
            banManager.tempBan(uuid, playerName, duration, finalReason, ip, actorName);
        } else {
            banManager.ban(uuid, playerName, finalReason, ip, actorName);
        }

        if (target != null) {
            if (duration > 0) {
                target.disconnect(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.tempban_disconnect").replace("{reason}", finalReason).replace("{duration}", TimeUtil.formatDuration(duration))));
            } else {
                target.disconnect(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.ban_disconnect").replace("{reason}", finalReason)));
            }
        }

        String senderMessageKey = duration > 0 ? "ban.tempban_sender_notification" : "ban.ban_sender_notification";
        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString(senderMessageKey)
                .replace("{target}", playerName)
                .replace("{duration}", duration > 0 ? TimeUtil.formatDuration(duration) : "PERMANENT")
                .replace("{reason}", finalReason)));

        String notificationTemplate = duration > 0
                ? Purgatory.getConfigManager().getMessages().getString("ban.tempban_notification", "&7{target} &ahas been temporarily banned by &7{issuer}. &afor {duration} &aReason: &e{reason}")
                : Purgatory.getConfigManager().getMessages().getString("ban.ban_notification");

        String notification = notificationTemplate
                .replace("{target}", playerName)
                .replace("{issuer}", actorName)
                .replace("{duration}", duration > 0 ? TimeUtil.formatDuration(duration) : "PERMANENT")
                .replace("{reason}", finalReason);

        if (silent) {
            notifyStaff("&7[Silent] " + notification);
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + notification));
        }
    }

    private String extractIp(ProxiedPlayer player) {
        String raw = player.getSocketAddress().toString();
        int colon = raw.indexOf(':');
        if (colon == -1) return raw.replace("/", "");
        return raw.substring(0, colon).replace("/", "");
    }

    private void notifyStaff(String message) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("purgatory.notifications"))
                .forEach(p -> p.sendMessage(C.translate(message)));
    }

    public java.util.List<String> banipTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        return PlayerTargets.online(prefix);
    }
}
