package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.ProxyServer;
import org.bson.Document;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
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

    @Command({"unban","unjail","unb"})
    @Description("Unban a player")
    @Usage("unban <player> [-p|-s]")
    @CommandPermission("purgatory.ban")
    public void unban(BungeeCommandActor actor, String playerName, @Optional String... args) {
        Document doc = banManager.getBansCollection().find(eq("name", playerName))
                .sort(descending("bannedAt"))
                .first();

        if (doc == null) {
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.no_ban_found", "&cNo ban found for &e{player}").replace("{player}", playerName)));
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
            actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.no_ban_found", "&cNo ban found for &e{player}").replace("{player}", playerName)));
            return;
        }

        banManager.unban(uuid);
        actor.reply(C.translate(Purgatory.getConfigManager().getMessages().getString("ban.unban_sender_notification","&aYou have unbanned &f{target}").replace("{target}", playerName)));

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
        if (target != null && target.isConnected()) {
            banManager.removeFromJail(target);
        }

        boolean silent = true; // default
        for (String arg : args) {
            if (arg != null && arg.equalsIgnoreCase("-p")) {
                silent = false;
            }
        }


        String message = C.translate(Purgatory.getConfigManager().getMessages().getString("ban.unban_notification","&7{target} &ahas been unbanned by &7{issuer}.").replace("{target}", playerName).replace("{issuer}", actor.name()));

        if (silent) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(p -> p.hasPermission("purgatory.notifications"))
                    .forEach(p -> p.sendMessage(C.translate("&7[Silent] " + message)));
        } else {
            ProxyServer.getInstance().broadcast(C.translate("&c[!] " + message));
        }
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
