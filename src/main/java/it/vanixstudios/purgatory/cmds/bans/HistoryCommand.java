package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.manager.bans.BanManager;
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

public class HistoryCommand {

    private final BanManager banManager;

    public HistoryCommand(BanManager banManager) {
        if (banManager == null) throw new IllegalArgumentException("BanManager cannot be null");
        this.banManager = banManager;
    }

    @Command({"history","hist"})
    @Description("View a player's punishments history")
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

    public java.util.List<String> historyTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        String lp = prefix == null ? "" : prefix.toLowerCase();
        return net.md_5.bungee.api.ProxyServer.getInstance().getPlayers().stream()
                .map(net.md_5.bungee.api.connection.ProxiedPlayer::getName)
                .filter(n -> n.toLowerCase().startsWith(lp))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(java.util.stream.Collectors.toList());
    }
}
