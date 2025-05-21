package it.vanixstudios.purgatory.cmds.admin;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.strings.C;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;

import java.io.IOException;

public class PurgatoryCmd
{
    @Command("purgatory")
    @Usage("purgatory")
    public void onPurgatory(CommandActor sender){
        sender.reply(C.translate(

                """
                &7&m--------------------------------
                &7This server is using &cPurgatory 
                &7Developed with ❤️ for &3X-Network
                &r
                &7Author: &cLorenz
                &7Contributors: &bEmpireMTR
                &7&m--------------------------------
                """

        ));
    }
    @Command("purgatory reload")
    @CommandPermission("purgatory.admin")
    public void onPurgatoryReload(CommandActor sender) throws IOException {
        try{
            sender.reply(C.translate("&aReloading Purgatory..."));
            Purgatory.getConfigManager().reload();
            sender.reply(C.translate("&aPurgatory reloaded!"));
        } catch (Exception e) {
            sender.reply(C.translate("&cAn error occurred while reloading Purgatory!"));
        }

    }
}
