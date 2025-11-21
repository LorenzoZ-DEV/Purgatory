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

import java.util.UUID;

public class CheckMuteCommand {

    private final MuteManager muteManager;

    public CheckMuteCommand(MuteManager muteManager) {
        this.muteManager = muteManager;
    }

    @Command("checkmute")
    @CommandPermission("purgatory.staff")
    @Description("Check if a player is muted")
    @Usage("checkmute <player>")
    public void onCheckMute(CommandActor actor, @Named("player") String playerName) {
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target == null) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.player_not_found","&cPlayer not found")));
            return;
        }

        UUID uuid = target.getUniqueId();
        if (!muteManager.isMuted(uuid)) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.not_muted", "&f{target} &cis not muted.").replace("{target}", target.getName())));
            return;
        }

        String reason = muteManager.getMuteReason(uuid);
        long tempMuteEnd = muteManager.getTempMuteEnd(uuid);
        if (tempMuteEnd == 0) {
            actor.reply(C.translate("&cPlayer &f" + target.getName() + " &cis permanently muted. Reason: &f" + reason));
        } else {
            long timeLeft = tempMuteEnd - System.currentTimeMillis();
            actor.reply(C.translate("&cPlayer &f" + target.getName() + " &cis temporarily muted. Reason: &f" + reason +
                    " &cTime remaining: &f" + TimeUtil.formatDuration(timeLeft)));
        }
    }

    public java.util.List<String> checkmuteTabComplete(revxrsal.commands.bungee.actor.BungeeCommandActor actor, @Optional String prefix) {
        return it.vanixstudios.purgatory.util.players.PlayerTargets.online(prefix);
    }
}
