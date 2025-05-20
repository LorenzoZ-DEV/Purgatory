package it.vanixstudios.purgatory.cmds.bans;

import it.vanixstudios.purgatory.manager.BanManager;
import it.vanixstudios.purgatory.util.C;
import it.vanixstudios.purgatory.util.TimeUtil;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bungee.annotation.CommandPermission;

import net.md_5.bungee.api.CommandSender;
import org.bson.Document;
import revxrsal.commands.command.CommandActor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BanListCommand {

    @Command("banlist")
    @CommandPermission("purgatory.staff")
    private void banlist(CommandActor sender) {
        BanManager banManager = BanManager.getInstance();

        List<String> bannedPlayers = new ArrayList<>();
        for (Document document : banManager.getBansCollection().find()) {
            String playerName = document.getString("name");
            boolean permanent = document.getBoolean("permanent", false);
            Date bannedAt = document.getDate("bannedAt");
            Date until = document.getDate("until");
            String reason = document.getString("reason");

            String banStatus;
            if (permanent) {
                banStatus = "&7[&cActive (Permanent)&7]";
            } else if (until != null && until.after(new Date())) {
                long remainingMillis = until.getTime() - System.currentTimeMillis();
                String remainingTime = TimeUtil.formatDuration(remainingMillis);
                banStatus = "&7[&cActive for " + remainingTime + "&7]";
            } else {
                banStatus = "&7[&8Expired&7]";
            }

            bannedPlayers.add(String.format("&e%s &7- &fReason: &6%s &7- Status: %s", playerName, reason, banStatus));
        }

        if (bannedPlayers.isEmpty()) {
            sender.reply(C.translate("&cNo banned players found."));
            return;
        }

        sender.reply(C.translate("&aList of banned players:"));
        for (String player : bannedPlayers) {
            sender.reply(C.translate("§7- " + player));
        }
    }

}