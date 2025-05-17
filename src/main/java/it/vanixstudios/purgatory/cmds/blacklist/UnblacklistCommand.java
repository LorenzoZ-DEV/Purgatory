package it.vanixstudios.purgatory.cmds.blacklist;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;

public class UnblacklistCommand {

    private final MongoCollection<?> blacklistCollection;

    public UnblacklistCommand(MongoCollection<?> blacklistCollection) {
        this.blacklistCollection = blacklistCollection;
    }

    @Command("unblacklist")
    @Usage("unblacklist <player>")
    @CommandPermission("purgatory.unblacklist")
    @Description("Remove a player from the blacklist")
    public void unblacklist(CommandSender sender, String targetName) {
        if (targetName == null || targetName.isEmpty()) {
            sender.sendMessage(C.translate("&cUsage: /unblacklist <player>"));
            return;
        }

        var found = blacklistCollection.find(Filters.eq("name", targetName)).first();

        if (found == null) {
            sender.sendMessage(C.translate("&cPlayer not found in blacklist."));
            return;
        }

        blacklistCollection.deleteOne(Filters.eq("name", targetName));
        sender.sendMessage(C.translate("&c[S] &e" + targetName + "&ahas been unblacklisted by &e" + sender ));
    }
}
