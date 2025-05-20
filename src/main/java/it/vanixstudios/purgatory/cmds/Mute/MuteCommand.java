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
    @Usage("mute <player> <reason> [-p|-s]")
    @Description("Mutes a player permanently")
    public void execute(BungeeCommandActor sender,
                        @Named("player") String playerName,
                        @Named("reason") String reason,
                        @Optional String... flags) {

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

        boolean silent = true;
        for (String flag : flags) {
            if (flag.equalsIgnoreCase("-p")) {
                silent = false;
                break;
            }
        }

        String message = C.translate("&e" + target.getName() + " &chas been permanently muted by &e" + sender.name() + "&c. Reason: &e" + reason);

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.staff"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }
    }
}
