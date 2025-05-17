package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.ProxyServer;
import org.bson.Document;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.descending;

public class UnbanCommand {

    private final BanManager banManager;

    public UnbanCommand(BanManager banManager) {
        if (banManager == null) throw new IllegalArgumentException("BanManager cannot be null");
        this.banManager = banManager;
    }

    @Command("unban")
    @Usage("unban <player>")
    @CommandPermission("purgatory.ban")
    public void unban(BungeeCommandActor actor, String playerName) {
        Document doc = banManager.getBansCollection().find(eq("name", playerName))
                .sort(descending("bannedAt"))
                .first();

        if (doc == null) {
            actor.reply(C.translate("&cNo ban found for player &e" + playerName + "&c."));
            return;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(doc.getString("uuid"));
        } catch (Exception e) {
            actor.reply(C.translate("&cError: Invalid UUID stored for player &e" + playerName + "&c."));
            return;
        }

        if (!banManager.isBanned(uuid)) {
            actor.reply(C.translate("&cPlayer &e" + playerName + " &cis not currently banned."));
            return;
        }

        banManager.unban(uuid);
        actor.reply(C.translate("&aYou unbanned &e" + playerName + " &asuccessfully."));

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
        if (target != null && target.isConnected()) {
            banManager.removeFromJail(target);
        }

        String notification = C.translate("&c[S] &e" + playerName + " &cwas unbanned by &e" + actor.name() + ".");
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("purgatory.notifications"))
                .forEach(p -> p.sendMessage(notification));
    }

    public List<String> unbanTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        String lowerPrefix = prefix == null ? "" : prefix.toLowerCase();

        return banManager.getBansCollection()
                .find()
                .map(doc -> doc.getString("name"))
                .into(new java.util.ArrayList<>())
                .stream()
                .filter(name -> name != null && name.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
