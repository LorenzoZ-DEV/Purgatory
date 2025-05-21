package it.vanixstudios.purgatory.cmds.kick;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.console.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

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
            sender.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.player_not_found","&cPlayer not found")));
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

        String kickMessage = C.translate(Purgatory.getConfigManager().getMessages().getString("kick.player_kick_message","&cYou have been kicked from the server. \n Reason: &e{reason}").replace("{reason}", reason));
        target.disconnect(kickMessage);

        String logMessage = Purgatory.getConfigManager().getMessages().getString("kick_notification","&7{target} &ahas been kicked by &7{sender}. &a Reason: {reason}").replace("{target}", targetName).replace("{sender}", sender.name()).replace("{reason}", reason);

        sender.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("kick.player_kick_notification","&aYou kicked &f{target} &afor: &f{reason}").replace("{target}", targetName).replace("{reason}", reason)));
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
