package it.vanixstudios.purgatory.cmds.Mute;

import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.util.C;
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
    @Usage("mute <player> <reason>")
    @Description("Mutes a player permanently")
    public void execute(BungeeCommandActor sender,
                        @Named("player") String playerName,
                        @Named("reason") String reason) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target == null) {
            sender.reply(C.translate("&cPlayer not found."));
            return;
        }

        UUID uuid = target.getUniqueId();
        if (muteManager.isMuted(uuid)) {
            sender.reply(C.translate("&cThat player is already muted."));
            return;
        }

        muteManager.mutePlayer(uuid, reason);

        sender.reply(C.translate("&aPermanently muted &f" + target.getName() + "&a. Reason: &f" + reason));
        target.sendMessage(C.translate("&cYou have been permanently muted. Reason: &f" + reason));

        for (ProxiedPlayer staff : ProxyServer.getInstance().getPlayers()) {
            if (staff.hasPermission("purgatory.staff")) {
                staff.sendMessage(C.translate("&c[S] &e" + target.getName() + " &chas been &cpermanently muted by &e" + sender.name()));
            }
        }
    }
}
