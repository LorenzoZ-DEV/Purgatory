package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

public class IpBanCmd {

    private final BanManager banManager;

    public IpBanCmd(BanManager banManager) {
        this.banManager = banManager;
    }

    @Command({"ipban", "banip","ipjail"})
    @CommandPermission("purgatory.ipban")
    @Usage("ipban <player> <reason> [-p|-s]")
    public void executeIpBan(BungeeCommandActor actor, ProxiedPlayer playerName, @Optional String reason) {
        String ip = playerName.getSocketAddress ( ).toString ( );

        if (banManager.isIpBanned ( ip )) {
            actor.reply ( C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.ip_already_banned", "&cThis IP is already banned." ).replace ( "{target}", playerName.getName ( ) ) ) );
            return;
        }

        if (reason == null) reason = "";

        boolean silent = true;
        if (reason.contains ( "-p" )) {
            silent = false;
        }

        String finalReason = reason.replace ( "-p", "" ).replace ( "-s", "" ).trim ( );
        if (finalReason.isEmpty ( )) finalReason = "No specific reason provided.";

        banManager.ipban ( ip, finalReason );
        ProxiedPlayer target = ProxyServer.getInstance ( ).getPlayer ( playerName.getName ( ) );
        if (target != null && target.isConnected ( )) {
            target.disconnect ( C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.ipban_disconnect" ).replace ( "{reason}", finalReason ) ) );
        }

        actor.reply ( C.translate ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.ipban_sender_notification" ).replace ( "{target}", playerName.getName ( ) ).replace ( "{reason}", finalReason ) ) );

        String notification = String.format ( Purgatory.getConfigManager ( ).getMessages ( ).getString ( "ban.ipban_notification", "&7{target} &ahas been IP-banned by &7{issuer}. &aReason: &e{reason}" ).replace ( "{target}", playerName.getName ( ) ).replace ( "{issuer}", actor.name ( ) ).replace ( "{reason}", finalReason ) );

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
