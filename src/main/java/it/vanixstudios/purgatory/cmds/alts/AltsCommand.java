package it.vanixstudios.purgatory.cmds.alts;

import it.vanixstudios.purgatory.Purgatory;
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
import java.util.stream.Collectors;

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

        String ip = targetPlayer.getSocketAddress().toString();
        ip = ip.replaceFirst("^/", "").split(":")[0];

            List<Document> alts = Purgatory.getInstance().getMongoManager().getDatabase()
                .getCollection("bans")
                .find(new Document("ip", ip))
                .into(new java.util.ArrayList<>());

        if (alts.isEmpty()) {
            actor.reply(C.translate("&aNo alternate accounts found for player §f") + playerName);
            return;
        }

        actor.reply(C.translate("&eAlternate accounts sharing IP &f" + ip + ":"));
        for (Document alt : alts) {
            String name = alt.getString("name");
            String uuid = alt.getString("uuid");
            boolean banned = alt.getBoolean("banned", false);

            actor.reply(" §7- §f" + name + " §8(§e" + uuid + "§8) " + (banned ? "§c[BANNED]" : ""));
        }
    }

    public List<String> altsTabComplete(BungeeCommandActor actor, @Optional String prefix) {
        String lowerPrefix = prefix == null ? "" : prefix.toLowerCase();
        return ProxyServer.getInstance().getPlayers().stream()
                .map(ProxiedPlayer::getName)
                .filter(name -> name.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
