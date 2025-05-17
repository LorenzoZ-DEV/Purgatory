package it.vanixstudios.purgatory.cmds.kick;

import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
public class KickCommand {

    @Command("kick")
    @Usage("kick <player> <reason>")
    public void execute(BungeeCommandActor sender,
                        @Named("player") String targetName,
                        @Optional @Named("reason") String reason) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetName);

        if (target == null) {
            sender.reply(C.translate("&cPlayer &e" + targetName + " &cnot found or offline."));
            return;
        }

        if (reason == null || reason.isEmpty()) {
            reason = "Kicked by an operator";
        }

        String kickMessage = C.translate("&cYou have been kicked from the server.\n&7Reason: &c" + reason);
        target.disconnect(kickMessage);

        sender.reply(C.translate("&c[S] &e" + targetName + " &cwas been kicked by &e " + sender + "&c Reason: &e" + reason));
        Logger.info(sender.name() + " kicked player " + targetName + " for: " + reason);
    }
}
