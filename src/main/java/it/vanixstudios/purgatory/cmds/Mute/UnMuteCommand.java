package it.vanixstudios.purgatory.cmds.Mute;

import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;

import revxrsal.commands.annotation.Suggest;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;

import java.util.UUID;


public class UnMuteCommand {

    private final MuteManager muteManager;

    public UnMuteCommand(MuteManager muteManager) {
        this.muteManager = muteManager;
    }
    @Command("unmute")
    @Usage("unmute <player>")
    @CommandPermission("purgatory.unmute")
    public void onUnmute(CommandActor actor,
                         @Named("player") @Suggest("player") String playerName) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target == null || !target.isConnected()) {
            actor.reply(C.translate("&cPlayer not found or not online."));
            return;
        }

        UUID uuid = target.getUniqueId();
        if (!muteManager.isMuted(uuid)) {
            actor.reply(C.translate("&cThat player is not muted."));
            return;
        }

        muteManager.unmutePlayer(uuid);
        actor.reply(C.translate("&aYou have unmuted &f" + target.getName() + "&a."));
        target.sendMessage(C.translate("&aYou have been unmuted."));

        for (ProxiedPlayer staff : ProxyServer.getInstance().getPlayers()) {
            if (staff.hasPermission("purgatory.staff")) {
                staff.sendMessage(C.translate("&c[S] &e" + target.getName() + " &ahas been unmuted by &e" + actor.name()));
            }
        }
    }
}
