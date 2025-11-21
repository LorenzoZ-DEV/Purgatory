package it.vanixstudios.purgatory.cmds.blacklist;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.players.PlayerTargets;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;

public class UnblacklistCommand {

    private final MongoCollection<?> blacklistCollection;

    public UnblacklistCommand(MongoCollection<?> blacklistCollection) {
        this.blacklistCollection = blacklistCollection;
    }

    @Command({"unblacklist","unbl"})
    @Usage("unblacklist <player> [-p|-s]")
    @CommandPermission("purgatory.unblacklist")
    @Description("Remove a player from the blacklist")
    public void unblacklist(CommandSender sender, String targetName, @Optional String flags) {
        if (targetName == null || targetName.isEmpty()) {
            sender.sendMessage(C.translate("&cUsage: /unblacklist <player>"));
            return;
        }

        var found = blacklistCollection.find(Filters.eq("name", targetName)).first();

        if (found == null) {
            sender.sendMessage(C.translate(Purgatory.getConfigManager().getMessages().getString("blacklist.not_blacklist","&f{target} &cis not in blacklist").replace("{target}", targetName)));
            return;
        }

        boolean silent = true; // default silenzioso

        // Gestione flag come nel TempbanCommand
        if (flags != null && flags.contains("-p")) {
            silent = false;
        }

        blacklistCollection.deleteOne(Filters.eq("name", targetName));
        sender.sendMessage(C.translate(Purgatory.getConfigManager().getMessages().getString("blacklist.unblacklist_sender_notification","&aYou have unblacklisted &f{target}").replace("{target}", targetName)));

        String message = C.translate(Purgatory.getConfigManager().getMessages().getString("unblacklist_notification","&7{target} &ahas been unblacklisted by &7{issuer}").replace("{target}", targetName).replace("{issuer}", sender.getName()));

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.notifications"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }
    }

    public java.util.List<String> unblacklistTabComplete(revxrsal.commands.bungee.actor.BungeeCommandActor actor, @Optional String prefix) {
        return PlayerTargets.online(prefix);
    }
}