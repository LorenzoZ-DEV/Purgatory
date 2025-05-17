package it.vanixstudios.purgatory.cmds.blacklist;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.Logger;
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

@Command("blacklist")
public class BlacklistCommand {

    @Usage("blacklist <player> <reason>")
    @Description("Blacklists a player permanently from the network.")
    @CommandPermission("purgatory.blacklist")
    public void execute(BungeeCommandActor actor,
                        @Named("player") String playerName,
                        @Named("reason") String reasonRaw) {

        // Unire gli argomenti nel caso ci sia stato un errore di parsing
        String reason = String.join(" ", reasonRaw.split(" "));

        ProxiedPlayer target = Purgatory.getInstance().getProxy().getPlayer(playerName);

        Document doc = new Document("uuid", target != null ? target.getUniqueId().toString() : "users")
                .append("name", playerName)
                .append("reason", reason)
                .append("timestamp", new Date());

        Purgatory.getInstance().getMongoManager().getDatabase()
                .getCollection("blacklist")
                .insertOne(doc);

        Logger.info("&cBlacklisted player " + playerName + " for: " + reason);

        if (target != null) {
            String message = C.translate(
                    "&c&lYour account was Blacklisted from X-NETWORK\n\n" +
                            "&c&lThis punishment is not contestable\n" +
                            "&c&lIf you think this is a mistake, please contact support.\n\n" +
                            "&7Reason: &c" + reason
            );
            target.disconnect(message);
        }

        actor.reply("§c[S] §e " + playerName + " §4&lwas blacklisted by §e" + actor.name());
    }
}
