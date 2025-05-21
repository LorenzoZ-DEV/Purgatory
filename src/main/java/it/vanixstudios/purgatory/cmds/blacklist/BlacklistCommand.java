package it.vanixstudios.purgatory.cmds.blacklist;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.Arrays;
import java.util.Date;

@Command({"blacklist", "bl"})
public class BlacklistCommand {

    @Usage("blacklist <player> <reason> [-p|-s]")
    @Description("Blacklists a player permanently from the network.")
    @CommandPermission("purgatory.blacklist")
    public void execute(BungeeCommandActor actor,
                        @Named("player") ProxiedPlayer target,
                        @Named("reason") String... reason) {

        StringBuilder fixedReason = new StringBuilder();
        for (String str : reason) {
            fixedReason.append(" ").append(str);
        }

        boolean silent = fixedReason.toString().contains("-s");
        Document doc = new Document("uuid", target != null ? target.getUniqueId().toString() : "unknown")
                .append("name", target.getName())
                .append("reason", fixedReason.toString())
                .append("timestamp", new Date());

        Purgatory.getInstance().getMongoManager().getDatabase()
                .getCollection("blacklist")
                .insertOne(doc);

        Logger.info("&cBlacklisted player " + target.getName() + " for: " + fixedReason);
        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString(
                        "blacklist.blacklist_sender_notification",
                        "&aYou have blacklisted &c&l{target} &afor &c&l{reason} ")
                .replace("{target}", target.getName())
                .replace("{reason}", fixedReason.toString())));

        String message = Purgatory.getConfigManager().getMessages().getString(
                        "blacklist.blacklist_notification",
                        "&7{target} &ahas been permanently blacklisted by &7{issuer}")
                .replace("{target}", target.getName())
                .replace("{issuer}", actor.name());

        if (silent) {
            notifyStaff("&7[Silent] " + message);
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }

        if (target != null) {
            String kickMessage = C.translate(Purgatory.getConfigManager().getMessages().getString(
                            "blacklist.blacklist_disconnect",
                            "&cYou have been blacklisted from the server. \n Reason: &e{reason}")
                    .replace("{reason}", fixedReason.toString()));
            target.disconnect(kickMessage);
        }
    }

    private void notifyStaff(String message) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("purgatory.notifications"))
                .forEach(p -> p.sendMessage(C.translate(message)));
    }
}
