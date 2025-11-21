package it.vanixstudios.purgatory.cmds.blacklist;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.strings.C;
import org.bson.Document;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import java.util.Date;



public class BlacklistInfoCommand {
    @Command({"blacklistinfo","blinfo"})
    @Description("Shows blacklist information of a player")
    @Usage("blacklistinfo <player>")

    public void handle(BungeeCommandActor sender, @Named("player") String targetName) {
        Document doc = Purgatory.getInstance().getMongoManager().getDatabase()
                .getCollection("blacklist")
                .find(new Document("name", targetName))
                .first();

        if (doc == null) {
            sender.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("blacklist.not_blacklist","&f{target} &cis not in blacklist").replace("{target}", targetName)));
            return;
        }

        String reason = doc.getString("reason");
        String bannedBy = doc.getString("bannedBy");
        if (bannedBy == null || bannedBy.isEmpty()) bannedBy = "Unknown";

        Long time = doc.getLong("time");
        String timeStr = (time != null) ? new Date(time).toString() : "Unknown";

        sender.reply(C.translate("&aBlacklist info for &f" + targetName + "&a:"));
        sender.reply(C.translate("&7Reason: &f" + (reason != null ? reason : "Unknown")));
        sender.reply(C.translate("&7Banned by: &f" + bannedBy));
        sender.reply(C.translate("&7Time: &f" + timeStr));
    }

    public java.util.List<String> blacklistinfoTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        return it.vanixstudios.purgatory.util.players.PlayerTargets.online(prefix);
    }
}
