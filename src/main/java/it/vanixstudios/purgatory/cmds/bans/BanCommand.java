package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.checkerframework.framework.qual.DefaultFor;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.help.Help;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BanCommand {

    private final BanManager banManager;

    public BanCommand(BanManager banManager) {
        this.banManager = banManager;
    }


    @Command({"ban", "jail", "b"})
    @CommandPermission("purgatory.ban")
    @Description ( "Ban a player" )
    @Usage("ban <player> [reason] [-p|-s]")
    public void ban(BungeeCommandActor actor,
                    String playerName,
                    @Optional String reason) {

        ProxiedPlayer target = ProxyServer.getInstance ( ).getPlayer ( playerName );
        UUID uuid = target != null ? target.getUniqueId ( ) :
                UUID.nameUUIDFromBytes ( ("OfflinePlayer:" + playerName).getBytes ( StandardCharsets.UTF_8 ) );

        if (banManager.isBanned ( uuid )) {
            actor.reply ( C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.already_banned", "&e{target} &calready banned" ).replace ( "{target}", playerName ) ) );
            return;
        }

        if (reason == null) reason = "";

        boolean silent = true;
        if (reason.contains ( "-p" )) {
            silent = false;
        }

        String finalReason = reason.replace ( "-p", "" ).replace ( "-s", "" ).trim ( );
        if (finalReason.isEmpty ( )) finalReason = "No reason specified.";

        String ip = target != null ? target.getSocketAddress ( ).toString ( ).split ( ":" )[0].replace ( "/", "" ) : "";

        banManager.ban ( uuid, playerName, finalReason, ip );

        if (target != null) {
            banManager.sendToJail ( target );
            target.disconnect ( C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.ban_disconnect" ).replace ( "{reason}", finalReason ) ) );
        }

        actor.reply ( C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.ban_sender_notification" ).replace ( "{target}", playerName ).replace ( "{reason}", finalReason ) ) );

        String notification = String.format ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.ban_notification" ).replace ( "{target}", playerName ).replace ( "{issuer}", actor.name ( ) ).replace ( "{reason}", finalReason ) );

        if (silent) {
            notifyStaff ( "&7[Silent] " + notification );
        } else {
            ProxyServer.getInstance ( ).broadcast ( C.translate ( "&c[!] " + notification ) );
        }
    }

    private void notifyStaff(String message) {
        ProxyServer.getInstance ( ).getPlayers ( ).stream ( )
                .filter ( p -> p.hasPermission ( "purgatory.notifications" ) )
                .forEach ( p -> p.sendMessage ( C.translate ( message ) ) );
    }
}