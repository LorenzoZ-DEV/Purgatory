package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

public class CheckBanCommand {

    private final BanManager banManager;

    public CheckBanCommand(BanManager banManager) {
        if (banManager == null) throw new IllegalArgumentException("BanManager cannot be null");
        this.banManager = banManager;
    }

    @Command("checkban")
    @Usage("checkban <player>")
    @CommandPermission("purgatory.checkban")
    public void checkBan(BungeeCommandActor actor, String playerName) {
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);

        UUID uuid = null;

        if (target != null) {
            uuid = target.getUniqueId();
        } else {
            Document doc = banManager.getBansCollection().find(eq("name", playerName))
                    .sort(descending("bannedAt"))
                    .first();

            if (doc != null) {
                try {
                    uuid = UUID.fromString(doc.getString("uuid"));
                } catch (Exception e) {
                    actor.reply(C.translate("&cError: Invalid UUID stored for player &e" + playerName + "&c."));
                    return;
                }
            }
        }

        if (uuid == null) {
            actor.reply(C.translate("&cPlayer &e" + playerName + " &cnot found or has never been banned."));
            return;
        }

        if (banManager.isBanned(uuid)) {
            String reason = banManager.getBanReason(uuid);
            actor.reply(C.translate("&aPlayer &e" + playerName + " &ais banned."));
            actor.reply(C.translate("&7Reason: &f" + (reason == null ? "No reason specified" : reason)));
            actor.reply(C.translate("&fBanned by &f: " + banManager.getBannedBy(uuid)));
        } else {
            actor.reply(C.translate("&aPlayer &e" + playerName + " &ais not banned."));
        }
    }

    public List<String> checkbanTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        String lowerPrefix = prefix == null ? "" : prefix.toLowerCase();

        return ProxyServer.getInstance().getPlayers().stream()
                .map(ProxiedPlayer::getName)
                .filter(name -> name.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
