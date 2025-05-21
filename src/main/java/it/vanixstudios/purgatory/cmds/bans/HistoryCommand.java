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

public class HistoryCommand {

    private final BanManager banManager;

    public HistoryCommand(BanManager banManager) {
        if (banManager == null) throw new IllegalArgumentException("BanManager cannot be null");
        this.banManager = banManager;
    }

    @Command({"history","hist"})
    @Usage("history <player>")
    @CommandPermission("purgatory.history")
    public void history(BungeeCommandActor actor, String playerName) {
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
        UUID uuid = null;

        if (target != null) {
            uuid = target.getUniqueId();
        } else {
            Document doc = banManager.getBansCollection()
                    .find(eq("name", playerName))
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
            actor.reply(C.translate("&cPlayer &e" + playerName + " &cnot found or has no ban history."));
            return;
        }

        List<String> history = banManager.getBanHistory(uuid);

        if (history == null || history.isEmpty()) {
            actor.reply(C.translate("&aNo ban history found for &e" + playerName + "&a."));
            return;
        }

        actor.reply(C.translate("&7Ban history for &e" + playerName + ":"));
        for (String record : history) {
            actor.reply(C.translate("&8- &f" + record));
        }
    }

    public List<String> historyTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        String lowerPrefix = prefix == null ? "" : prefix.toLowerCase();

        return ProxyServer.getInstance().getPlayers().stream()
                .map(ProxiedPlayer::getName)
                .filter(name -> name.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
