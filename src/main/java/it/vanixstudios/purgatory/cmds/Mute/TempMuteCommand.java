package it.vanixstudios.purgatory.cmds.Mute;

import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.TimeUtil;
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
    public void onTempMute(CommandActor actor,
                           @Named("player") @Suggest("player") String playerName,
                           @Named("duration") String durationStr,
                           @Named("reason") String reason,
                           @Optional String... flags) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target == null || !target.isConnected()) {
            actor.reply(C.translate("&cPlayer must be online."));
            return;
        }

        if (muteManager.isMuted(target.getUniqueId())) {
            actor.reply(C.translate("&cThat player is already muted."));
            return;
        }

        long duration;
        try {
            duration = TimeUtil.parseTime(durationStr);
        } catch (IllegalArgumentException e) {
            actor.reply(C.translate("&cInvalid duration. Use formats like 10m, 1h, 2d."));
            return;
        }

        if (duration <= 0) {
            actor.reply(C.translate("&cInvalid duration."));
            return;
        }

        muteManager.tempMutePlayer(target.getUniqueId(), reason, duration);

        String formattedDuration = TimeUtil.formatDuration(duration);
        actor.reply(C.translate("&aYou temporarily muted &f" + playerName + " &afor &f" + formattedDuration + "&a. Reason: &f" + reason));
        target.sendMessage(C.translate("&cYou have been temporarily muted for &f" + formattedDuration + "&c. Reason: &f" + reason));

        boolean silent = true;
        for (String flag : flags) {
            if (flag.equalsIgnoreCase("-p")) {
                silent = false;
                break;
            }
        }

        String message = C.translate("&e" + playerName + " &chas been temporarily muted by &e" + actor.name() +
                " &cfor &f" + formattedDuration + "&c. Reason: &e" + reason);

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.staff"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[! ] " + message));
        }
    }
}
