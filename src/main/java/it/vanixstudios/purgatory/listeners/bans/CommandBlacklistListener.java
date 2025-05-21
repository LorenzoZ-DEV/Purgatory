package it.vanixstudios.purgatory.listeners.bans;

/*
 * Author : @vanixy
 * discord.gg/vanixstudios
 */

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.C;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandBlacklistListener implements Listener {

    private final Set<String> blockedCommands = new HashSet<>(Arrays.asList(
            "hub",
            "lobby",
            "joinqueue",
            "leavequeue"
    ));

    private final BanManager banManager;

    public CommandBlacklistListener(BanManager banManager) {
        this.banManager = banManager;
    }

    @EventHandler
    public void onPlayerCommand(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage().toLowerCase();

        if (!message.startsWith("/")) return;

        String command = message.split(" ")[0].substring(1);

        if (blockedCommands.contains(command) && banManager.isBanned(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.ban_command_notallowed","&cYou are banned from the server and cannot use this command.")));
        }
    }
}
