package it.vanixstudios.purgatory.cmds.blacklist;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.console.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Command({"blacklist", "bl"})
public class BlacklistCommand {

    @Usage("blacklist <player> <reason> [-s]")
    @Description("Blacklists a player permanently from the network.")
    @CommandPermission("purgatory.blacklist")
    public void execute(BungeeCommandActor actor,
                        String playerName,
                        @Optional String reason) {

        if (reason == null || reason.trim ( ).isEmpty ( )) {
            actor.reply ( C.translate ( "&cYou must specify a reason for the blacklist." ) );
            return;
        }

        boolean silent = reason.contains ( "-s" );
        String cleanReason = reason.replace ( "-s", "" ).trim ( );

        if (cleanReason.isEmpty ( )) {
            actor.reply ( C.translate ( "&cYou must specify a reason for the blacklist." ) );
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance ( ).getPlayer ( playerName );
        UUID uuid = target != null ? target.getUniqueId ( ) :
                UUID.nameUUIDFromBytes ( ("OfflinePlayer:" + playerName).getBytes ( StandardCharsets.UTF_8 ) );

        Document existingDoc = Purgatory.getInstance ( ).getMongoManager ( ).getDatabase ( )
                .getCollection ( "blacklist" )
                .find ( new Document ( "uuid", uuid.toString ( ) ) )
                .first ( );

        if (existingDoc != null) {
            actor.reply ( C.translate ( "&c" + playerName + " is already blacklisted!" ) );
            return;
        }

        Document doc = new Document ( "uuid", uuid.toString ( ) )
                .append ( "name", playerName )
                .append ( "reason", cleanReason )
                .append ( "blacklistedBy", actor.name ( ) );

        Purgatory.getInstance ( ).getMongoManager ( ).getDatabase ( )
                .getCollection ( "blacklist" )
                .insertOne ( doc );

        Logger.info ( "&cBlacklisted player " + playerName + " for: " + cleanReason );
        actor.reply ( C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString (
                        "blacklist.blacklist_sender_notification",
                        "&aYou blacklisted &c&l{target} &afor &c&l{reason} " )
                .replace ( "{target}", playerName )
                .replace ( "{reason}", cleanReason ) ) );

        String message = Purgatory.getConfigManager ( ).getMessages ( ).getString (
                        "blacklist.blacklist_notification",
                        "&7{target} &ahas been permanently blacklisted by &7{issuer}" )
                .replace ( "{target}", playerName )
                .replace ( "{issuer}", actor.name ( ) );

        if (silent) {
            notifyStaff ( "&7[Silent] " + message );
        } else {
            ProxyServer.getInstance ( ).broadcast ( C.translate ( "&c[!] " + message ) );
        }

        if (target != null) {
            String kickMessage = C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString (
                            "blacklist.blacklist_disconnect",
                            "&cYou have been blacklisted from the server. \n Reason: &e{reason}" )
                    .replace ( "{reason}", cleanReason ) );
            target.disconnect ( kickMessage );
        }
    }

    private void notifyStaff(String message) {
        ProxyServer.getInstance ( ).getPlayers ( ).stream ( )
                .filter ( p -> p.hasPermission ( "purgatory.notifications" ) )
                .forEach ( p -> p.sendMessage ( C.translate ( message ) ) );
    }
}