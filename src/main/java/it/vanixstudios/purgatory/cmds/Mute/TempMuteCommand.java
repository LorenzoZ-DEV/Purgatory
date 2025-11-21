package it.vanixstudios.purgatory.cmds.Mute;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.duration.TimeUtil;
import it.vanixstudios.purgatory.util.players.PlayerTargets;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

public class TempMuteCommand {

    private final MuteManager muteManager;

    public TempMuteCommand(MuteManager muteManager) {
        this.muteManager = muteManager;
    }

    @Command("tempmute")
    @Usage("tempmute <player> <duration> <reason> [-p|-s]")
    @Description("Temporarily mutes a player for a specified duration with an optional reason.")
    @CommandPermission("purgatory.tempmute")
    public void onTempMute(BungeeCommandActor actor,
                           String playerName,
                           String durationStr,
                           @Optional String reason,
                           @Optional String... flags) {

        if (reason == null) {
            actor.reply(C.translate("&cYou must provide a reason!"));
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target == null || !target.isConnected()) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.player_must_be_online","&cPlayer must be online")));
            return;
        }

        if (muteManager.isMuted(target.getUniqueId())) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.muted_already","&cPlayer already muted")));
            return;
        }

        long duration;
        try {
            duration = TimeUtil.parseTime(durationStr);
        } catch (IllegalArgumentException e) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.invalid_duration","&cInvalid duration. Use formats like 10m, 1h, 2d.")));
            return;
        }

        if (duration <= 0) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.invalid_duration","&cInvalid duration. Use formats like 10m, 1h, 2d.")));
            return;
        }

        boolean silent = true;
        for (String f : flags) {
            if (f.equalsIgnoreCase("-p")) silent = false;
        }

        String cleanReason = reason.replace("-p"," ").replace("-s"," ").trim();
        if (cleanReason.isEmpty()) cleanReason = reason;

        muteManager.tempMutePlayer(target.getUniqueId(), cleanReason, duration);

        String formattedDuration = TimeUtil.formatDuration(duration);
        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.tempmute_sender_notification","&aYou temporarily muted &f{target} &afor period {duration} for reason: {reason}").replace("{target}", target.getName()).replace("{duration}", formattedDuration).replace("{reason}", cleanReason)));
        target.sendMessage(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.tempmute_message_player","&cYou have been temporarily muted for {duration}. Reason: &f {reason} ").replace("{duration}", formattedDuration).replace("{reason}", cleanReason)));

        String message = C.translate(Purgatory.getConfigManager().getMessages().getString("mute.tempmute_notification","&7{target} &ahas been temporally muted by &7{issuer} &ffor &f{duration} &aReason: &e{reason}").replace("{target}", target.getName()).replace("{issuer}", actor.name()).replace("{duration}", formattedDuration).replace("{reason}", cleanReason));

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.staff"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[! ] " + message));
        }
    }

    public java.util.List<String> tempmuteTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        return it.vanixstudios.purgatory.util.players.PlayerTargets.online(prefix);
    }
}