package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.duration.TimeUtil;
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

    @Command({"tempban","tempjail","tb"})
    @CommandPermission("purgatory.ban")
    @Usage("tempban <player> <duration> <reason> [-p|-s]")
    public void executeTempban(BungeeCommandActor actor, ProxiedPlayer playerName, String durationArg, @Optional String reason) {
        UUID uuid = banManager.getOrCreateUUID(playerName.getName());

        if (banManager.isBanned(uuid)) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.already_banned","&e{target} &calready banned").replace("{target}", playerName.getName())));
            return;
        }

        long duration;
        try {
            duration = TimeUtil.parseTime(durationArg);
        } catch (IllegalArgumentException e) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.invalid_duration","&cInvalid duration. Use formats like 10m, 1h, 2d.")));
            return;
        }

        if (reason == null) reason = "";

        boolean silent = true;
        if (reason.contains("-p")) {
            silent = false;
        }

        String finalReason = reason.replace("-p", "").replace("-s", "").trim();
        if (finalReason.isEmpty()) finalReason = "No specific reason provided.";

        banManager.tempBan(uuid, playerName.getName(), duration, finalReason);

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName.getName());
        if (target != null && target.isConnected()) {
            banManager.sendToJail(target);
            target.disconnect(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.tempban_disconnect").replace("{reason}", finalReason).replace("{duration}", durationArg)));
        }

        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.tempban_sender_notification").replace("{target}", playerName.getName()).replace("{duration}", durationArg).replace("{reason}", finalReason)));

        String notification = String.format(Purgatory.getConfigManager().getMessages().getString("ban.tempban_notification","&7{target} &ahas been temporarily banned by &7{issuer}. &afor {duration} &aReason: &e{reason}").replace("{target}", playerName.getName()).replace("{issuer}", actor.name()).replace("{duration}", durationArg).replace("{reason}", finalReason));

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
