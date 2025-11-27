package it.vanixstudios.purgatory.cmds.alts;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.model.Profile;
import it.vanixstudios.purgatory.util.console.Logger;
import it.vanixstudios.purgatory.util.players.PlayerTargets;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import java.util.Set;
import java.util.UUID;

public class AltsCommand {

    @Command({"alts","dupeip"})
    @Usage("alts <player>")
    @CommandPermission("purgatory.staff.alts")
    public void alts(BungeeCommandActor actor, @Optional String playerName) {
        if (playerName == null || playerName.isBlank()) {
            actor.reply(C.translate("&cUso corretto: /alts <player>"));
            return;
        }

        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(playerName);
        Profile profile = null;
        String targetDisplayName = playerName;

        if (targetPlayer == null) {
            try {
                UUID offlineUUID = Purgatory.getInstance().getBanManager().getOrCreateUUID(playerName);
                profile = Purgatory.getInstance().getProfileManager().getProfile(offlineUUID);

                if (profile == null) {
                    actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("alts.no_alts_found", "&aNo alternate accounts found for &c{target}").replace("{target}", playerName)));
                    return;
                }

                targetDisplayName = profile.getName();
            } catch (Exception e) {
                actor.reply(C.translate("&cAn error occurred while getting player data."));
                Logger.error("Error while getting player data: " + e.getMessage());
                return;
            }
        } else {
            profile = Purgatory.getInstance().getProfileManager().getProfile(targetPlayer.getUniqueId());
            targetDisplayName = targetPlayer.getName();
        }

        if (profile == null) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("alts.no_alts_found", "&aNo alternate accounts found for &c{target}").replace("{target}", targetDisplayName)));
            return;
        }

        Set<UUID> alts = profile.getAlts();

        if (alts.isEmpty()) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("alts.no_alts_found", "&aNo alternate accounts found for &c{target}").replace("{target}", targetDisplayName)));
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

            if (index < alts.size()) builder.append("&7, ");
        }

        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("alts.alt_found", "&7{target}'s Alts &8(&aOnline, &cBanned&7, &7Offline&8):").replace("{target}", targetDisplayName)));
        actor.reply(C.translate(builder.toString()));
    }

    public java.util.List<String> altsTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        return PlayerTargets.online(prefix);
    }
}