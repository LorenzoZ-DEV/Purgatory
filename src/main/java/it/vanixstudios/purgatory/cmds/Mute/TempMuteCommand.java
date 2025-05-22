package it.vanixstudios.purgatory.cmds.Mute;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.duration.TimeUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;

public class TempMuteCommand {

    private final MuteManager muteManager;

    public TempMuteCommand(MuteManager muteManager) {
        this.muteManager = muteManager;
    }

    @Command("tempmute")
    @Usage("tempmute <player> <duration> <reason> [-p|-s]")
    @CommandPermission("purgatory.tempmute")
    public void onTempMute(CommandActor actor, String[] args) {

        if (args.length < 3) {
            actor.reply(C.translate("&cUsage: /tempmute <player> <duration> <reason> [-p|-s]"));
            return;
        }

        String playerName = args[0];
        String durationStr = args[1];

        // Ricostruisce la reason e cerca i flag
        StringBuilder reasonBuilder = new StringBuilder();
        boolean silent = true;

        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("-p")) {
                silent = false;
            } else if (args[i].equals("-s")) {
                silent = true;
            } else {
                if (reasonBuilder.length() > 0) {
                    reasonBuilder.append(" ");
                }
                reasonBuilder.append(args[i]);
            }
        }

        String cleanReason = reasonBuilder.toString();
        if (cleanReason.isEmpty()) {
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

        muteManager.tempMutePlayer(target.getUniqueId(), cleanReason, duration);

        String formattedDuration = TimeUtil.formatDuration(duration);
        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.tempmute_sender_notification","&aYou temporarily muted &f{target} &afor period {duration} for reason: {reason}").replace("{target}", target.getName()).replace("{duration}", formattedDuration).replace("{reason}", cleanReason)));
        target.sendMessage(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.tempmute_message_player","&cYou have been temporarily muted for {duration}. Reason: &f {reason} ").replace("{duration}", formattedDuration).replace("{reason}", cleanReason)));

        // Fix: correzione del typo "duratiom" -> "duration"
        String message = C.translate(Purgatory.getConfigManager().getMessages().getString("mute.tempmute_notification","&7{target} &ahas been temporally muted by &7{issuer} &ffor &f{duration} &aReason: &e{reason}").replace("{target}", target.getName()).replace("{issuer}", actor.name()).replace("{duration}", formattedDuration).replace("{reason}", cleanReason));

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.staff"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[! ] " + message));
        }
    }
}