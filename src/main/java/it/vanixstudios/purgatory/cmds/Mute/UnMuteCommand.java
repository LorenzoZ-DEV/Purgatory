package it.vanixstudios.purgatory.cmds.Mute;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;

import java.util.UUID;

public class UnMuteCommand {

    private final MuteManager muteManager;

    public UnMuteCommand(MuteManager muteManager) {
        this.muteManager = muteManager;
    }

    @Command("unmute")
    @Usage("unmute <player> [-p|-s]")
    @CommandPermission("purgatory.unmute")
    public void onUnmute(CommandActor actor,
                         @Named("player") @Suggest("player") String playerName,
                         @Optional String... flags) {

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        if (target == null || !target.isConnected()) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("general.player_not_found", "&cPlayer not found")));
            return;
        }

        UUID uuid = target.getUniqueId();
        if (!muteManager.isMuted(uuid)) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.not_muted", "&f{target} &cis not muted.").replace("{target}", target.getName())));
            return;
        }

        muteManager.unmutePlayer(uuid);

        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.unmute_sender_notification", "&aYou have been unmuted &f{target}").replace("{target}", target.getName())));
        target.sendMessage(C.translate(Purgatory.getConfigManager().getMessages().getString("mute.unmute_message_player", "&aYou have been unmuted.")));

        boolean silent = true;
        for (String flag : flags) {
            if (flag.equalsIgnoreCase("-p")) {
                silent = false;
                break;
            }
        }

        String message = C.translate(Purgatory.getConfigManager().getMessages().getString("mute.unmute_notification", "&7{target} &ahas been unmuted by &7{issuer}").replace("{target}", target.getName()).replace("{issuer}", actor.name()));

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.staff"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }
    }
}
