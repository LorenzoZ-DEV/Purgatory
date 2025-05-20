package it.vanixstudios.purgatory.cmds.blacklist;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;

public class UnblacklistCommand {

    private final MongoCollection<?> blacklistCollection;

    public UnblacklistCommand(MongoCollection<?> blacklistCollection) {
        this.blacklistCollection = blacklistCollection;
    }

    @Command("unblacklist")
    @Usage("unblacklist <player> [-p|-s]")
    @CommandPermission("purgatory.unblacklist")
    @Description("Remove a player from the blacklist")
    public void unblacklist(CommandSender sender, String targetName, String... flags) {
        if (targetName == null || targetName.isEmpty()) {
            sender.sendMessage(C.translate("&cUsage: /unblacklist <player>"));
            return;
        }

        var found = blacklistCollection.find(Filters.eq("name", targetName)).first();

        if (found == null) {
            sender.sendMessage(C.translate("&cPlayer not found in blacklist."));
            return;
        }

        boolean silent = true; // default silenzioso

        for (String flag : flags) {
            if (flag.equalsIgnoreCase("-p")) silent = false;
        }

        blacklistCollection.deleteOne(Filters.eq("name", targetName));
        sender.sendMessage(C.translate("&aYou unblacklisted &e" + targetName + "&a."));

        String message = C.translate("&e" + targetName + " &ahas been unblacklisted by &e" + sender.getName() + "&a.");

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.notifications"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }
    }
}
