package it.vanixstudios.purgatory.cmds.admin;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ChatColor;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.help.Help;

import java.io.IOException;

public class PurgatoryCmd {
    @Command("purgatory")
    @Usage("purgatory")
    public void onPurgatory(CommandActor sender) {
        sender.reply ( C.translate ("&7This Server is running &c&lPurgatory &7- &ev" + Purgatory.getInstance ( ).getDescription ( ).getVersion ( ) ) );
        sender.reply(C.translate("&7Developed by &cLorenzzz &7and Helped by: &bEmpireMTR"));
    }

    @Command("purgatory reload")
    @CommandPermission("purgatory.admin")
    public void onPurgatoryReload(CommandActor sender) throws IOException {
        try {
            sender.reply ( C.translate ( "&aReloading Purgatory..." ) );
            Purgatory.getConfigManager ( ).reload ( );
            sender.reply ( C.translate ( "&aPurgatory reloaded!" ) );
        } catch (Exception e) {
            sender.reply ( C.translate ( "&cAn error occurred while reloading Purgatory!" ) );
        }

    }

    @Command("purgatory help")
    @Description("Shows the list of available Purgatory commands.")
    @CommandPermission("purgatory.staff")
    @Usage("purgatory help [page]")
    public void help(BungeeCommandActor actor, @Default("1") @Range(min = 1) int page, Help.RelatedCommands<BungeeCommandActor> commands) {
        int ENTRIES_PER_PAGE = 7;
        var list = commands.paginate ( page, ENTRIES_PER_PAGE );
        actor.reply (C.translate( "&cPurgatory Help (Page " + page + "):" ));

        for (var entry : list) {
            String usage = ChatColor.RED + "- /" + entry.usage ( );
            String desc = ChatColor.GRAY + "  → " + entry.description ( );
            actor.reply ( usage );
            actor.reply ( desc );
        }
    }
}