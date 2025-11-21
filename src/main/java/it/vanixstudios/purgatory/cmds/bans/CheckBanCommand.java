package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.util.players.PlayerTargets;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.descending;
public class CheckBanCommand {


    @Command({"checkban","checkjail"})
@Description ( "Check if the player is jailed" )
@Usage("checkban <player>")
@CommandPermission("purgatory.checkban")
public void checkBan(BungeeCommandActor actor, String playerName) {
    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);


    UUID uuid = null;

    if (target != null) {
        uuid = target.getUniqueId();
    } else {
        Document doc = Purgatory.getInstance ().getBanManager ().getBansCollection().find(eq("name", playerName))
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

    if (Purgatory.getInstance ().getBanManager ().isBanned(uuid)) {
        String reason = Purgatory.getInstance ().getBanManager ().getBanReason(uuid);
        Object bannedByObject = Purgatory.getInstance ().getBanManager ().getBannedBy(uuid);
        String bannedBy = "Unknown";
        
        if (bannedByObject != null) {
            if (bannedByObject instanceof String) {
                bannedBy = (String) bannedByObject;
            } else {
                bannedBy = bannedByObject.toString();
            }
        }
        
        actor.reply(C.translate("&aPlayer &e" + playerName + " &ais banned."));
        actor.reply(C.translate("&7Reason: &f" + (reason == null ? "No reason specified" : reason)));
        actor.reply(C.translate("&fBanned by &f: " + bannedBy));
    } else {
        actor.reply(C.translate("&aPlayer &e" + playerName + " &ais not banned."));
    }
}

    public java.util.List<String> checkbanTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        return PlayerTargets.online(prefix);
    }
}