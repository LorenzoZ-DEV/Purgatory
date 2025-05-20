package it.vanixstudios.purgatory.cmds.alts;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.model.Profile;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import java.util.Set;
import java.util.UUID;

public class AltsCommand {

    @Command("alts")
    @Usage("alts <player>")
    @CommandPermission("purgatory.staff.alts")
    public void alts(BungeeCommandActor actor, String playerName) {
        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(playerName);

        if (targetPlayer == null) {
            actor.reply("§cPlayer not found or offline.");
            return;
        }

        Profile profile = Purgatory.getInstance().getProfileManager().getProfile(targetPlayer.getUniqueId());
        Set<UUID> alts = profile.getAlts();

        if (alts.isEmpty()) {
            actor.reply(C.translate("&aNo alternate accounts found for player §f") + playerName);
            return;
        }

        StringBuilder builder = new StringBuilder();

        int index = 0;
        for (UUID altUUID : alts) {
            Profile alt = Purgatory.getInstance().getProfileManager().getProfile(altUUID);

            if (alt == null) {
                profile.getAlts().remove(altUUID);
                continue;
            }

            boolean online = ProxyServer.getInstance().getPlayer(altUUID) != null && ProxyServer.getInstance().getPlayer(altUUID).isConnected();
            boolean banned = Purgatory.getInstance().getBanManager().isBanned(altUUID);

            builder.append(banned ? "&c" : online ? "&a" : "&7").append(alt.getName());
            index++;

            if (alts.size() <= index) builder.append("&7, ");
        }

        actor.reply(C.translate("&7" + profile.getName() + "'s Alts &7(Offline, &cBanned&7, &aOnline&7):"));
        actor.reply(C.translate(builder.toString()));
    }
}