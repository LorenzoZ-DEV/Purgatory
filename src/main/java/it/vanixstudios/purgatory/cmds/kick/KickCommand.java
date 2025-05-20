package it.vanixstudios.purgatory.cmds.kick;

import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.Arrays;

public class KickCommand {

    @Command("kick")
    @Usage("kick <player> <reason> [-p|-s]")
    @Description("Kicks a player from the server.")
    @CommandPermission("purgatory.kick")
    public void execute(BungeeCommandActor sender,
                        @Named("player") String targetName,
                        @Named("reason") String reason,
                        @Optional String... flags) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetName);

        if (target == null) {
            sender.reply(C.translate("&cPlayer &e" + targetName + " &cnot found or offline."));
            return;
        }

        if (reason == null || reason.isEmpty()) {
            reason = "Kicked by an operator";
        }

        boolean silent = true; // default silenzioso

        for (String flag : flags) {
            if (flag.equalsIgnoreCase("-p")) {
                silent = false;
            }
        }

        String kickMessage = C.translate("&cYou have been kicked from the server.\n&7Reason: &c" + reason);
        target.disconnect(kickMessage);

        String logMessage = "&e" + targetName + " &cwas kicked by &e" + sender.name() + "&c. Reason: &e" + reason;

        sender.reply(C.translate("&aYou kicked &e" + targetName + "&a for: &e" + reason));
        Logger.info(sender.name() + " kicked player " + targetName + " for: " + reason);

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.notifications"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + logMessage)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + logMessage));
        }
    }
}
