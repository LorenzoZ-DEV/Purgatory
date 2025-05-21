package it.vanixstudios.purgatory.cmds.blacklist;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.Arrays;
import java.util.Date;

@Command({"blacklist","bl"})
public class BlacklistCommand {

    @Usage("blacklist <player> <reason> [-p|-s]")
    @Description("Blacklists a player permanently from the network.")
    @CommandPermission("purgatory.blacklist")
    public void execute(BungeeCommandActor actor,
                        @Named("player") String playerName,
                        @Named("reason") String reasonRaw,
                        String... flags) {

        // Estrai la reason completa e i flag
        String reason = String.join(" ", reasonRaw.split(" "));
        boolean silent = true; // default silenzioso

        for (String flag : flags) {
            if (flag.equalsIgnoreCase("-p")) silent = false;
        }

        ProxiedPlayer target = Purgatory.getInstance().getProxy().getPlayer(playerName);

        Document doc = new Document("uuid", target != null ? target.getUniqueId().toString() : "users")
                .append("name", playerName)
                .append("reason", reason)
                .append("timestamp", new Date());

        Purgatory.getInstance().getMongoManager().getDatabase()
                .getCollection("blacklist")
                .insertOne(doc);

        Logger.info("&cBlacklisted player " + playerName + " for: " + reason);
        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("blacklist.blacklist_sender_notification","&aYou have blacklisted &c&l{target} &afor &c&l{reason} ").replace("{target}", playerName).replace("{reason}", reason)));

        String message = String.format(Purgatory.getConfigManager().getMessages().getString("blacklist.blacklist_notification","&7{target} &ahas been permanently blacklisted by &7{issuer}").replace("{target}", playerName).replace("{issuer}", actor.name()));

        if (silent) {
            notifyStaff("&7[Silent] " + message);
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }

        if (target != null) {
            String kickMessage = C.translate(Purgatory.getConfigManager().getMessages().getString("blacklist.blacklist_disconnect","&cYou have been blacklisted from the server. \n Reason: &e{reason}").replace("{reason}", reason));
            target.disconnect(kickMessage);
        }
    }

    private void notifyStaff(String message) {
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("purgatory.notifications"))
                .forEach(p -> p.sendMessage(C.translate(message)));
    }
}
