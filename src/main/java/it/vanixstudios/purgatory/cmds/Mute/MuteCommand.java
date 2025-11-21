package it.vanixstudios.purgatory.cmds.Mute;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.util.duration.TimeUtil;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.UUID;

public class MuteCommand {

    private final MuteManager muteManager;

    public MuteCommand(MuteManager muteManager) {
        this.muteManager = muteManager;
    }

    @Command("mute")
    @CommandPermission("purgatory.mute")
    @Usage("mute <player> <reason> [-p|-s]")
    @Description("Mutes a player permanently")
    public void execute(BungeeCommandActor sender,
                        @Named("player") String playerName,
                        @Optional String durationOrReason,
                        @Optional String reason,
                        @Optional String... flags) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target == null) {
            sender.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.player_not_found","&cPlayer not found")));
            return;
        }

        UUID uuid = target.getUniqueId();
        if (muteManager.isMuted(uuid)) {
            sender.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.already_muted","&cPlayer already muted")));
            return;
        }

        long duration = 0L;
        String parsedReason = reason;
        if (durationOrReason != null && durationOrReason.matches("\\d+[smhd]")) {
            try {
                duration = TimeUtil.parseTime(durationOrReason);
            } catch (IllegalArgumentException e) {
                sender.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.invalid_duration","&cInvalid duration. Use formats like 10m, 1h, 2d.")));
                return;
            }
        } else {
            parsedReason = durationOrReason;
        }

        if (parsedReason == null || parsedReason.isEmpty()) {
            parsedReason = duration > 0 ? "Temporarily muted" : "No reason specified";
        }

        boolean silent = true;
        for (String flag : flags) {
            if (flag.equalsIgnoreCase("-p")) {
                silent = false;
                break;
            }
        }

        if (duration > 0) {
            muteManager.tempMutePlayer(uuid, parsedReason, duration);
        } else {
            muteManager.mutePlayer(uuid, parsedReason);
        }

        String senderMessageKey = duration > 0 ? "mute.tempmute_sender_notification" : "mute.mute_sender_notification";
        String senderMessage = Purgatory.getConfigManager().getMessages().getString(senderMessageKey,
                duration > 0
                        ? "&aYou temporarily muted &f{target} &afor period {duration} for reason: {reason}"
                        : "&aYou permamently muted &f{target} &afor period FOREVER for reason: {reason}");

        sender.reply(C.translate(senderMessage
                .replace("{target}", target.getName())
                .replace("{duration}", duration > 0 ? TimeUtil.formatDuration(duration) : "FOREVER")
                .replace("{reason}", parsedReason)));

        String playerMessageKey = duration > 0 ? "mute.tempmute_message_player" : "mute.mute_message_player";
        String playerMessage = Purgatory.getConfigManager().getMessages().getString(playerMessageKey,
                duration > 0
                        ? "&cYou have been temporarily muted for {duration}. Reason: &f {reason}"
                        : "&cYou have been permanently muted. Reason: &f {reason} ");
        target.sendMessage(C.translate(playerMessage
                .replace("{duration}", duration > 0 ? TimeUtil.formatDuration(duration) : "FOREVER")
                .replace("{reason}", parsedReason)));

        String broadcastKey = duration > 0 ? "mute.tempmute_notification" : "mute.mute_notification";
        String message = C.translate(Purgatory.getConfigManager().getMessages().getString(broadcastKey,
                duration > 0
                        ? "&7{target} &ahas been temporally muted by &7{issuer} &ffor &f{duration} &aReason: &e{reason}"
                        : "&7{target} &ahas been permanently muted by &7{issuer} &aReason: &e{reason}")
                .replace("{target}", target.getName())
                .replace("{issuer}", sender.name())
                .replace("{duration}", duration > 0 ? TimeUtil.formatDuration(duration) : "FOREVER")
                .replace("{reason}", parsedReason));

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.staff"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }
    }

    public java.util.List<String> muteTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        return it.vanixstudios.purgatory.util.players.PlayerTargets.online(prefix);
    }
}
