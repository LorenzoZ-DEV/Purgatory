package it.vanixstudios.purgatory.util.console;

import it.vanixstudios.purgatory.util.strings.C;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class Art {
    public static void asciiArt() {
        String art =
                """

                &c██████╗░██╗░░░██╗██████╗░░██████╗░░█████╗░████████╗░█████╗░██████╗░██╗░░░██╗
                &c██╔══██╗██║░░░██║██╔══██╗██╔════╝░██╔══██╗╚══██╔══╝██╔══██╗██╔══██╗╚██╗░██╔╝
                &c██████╔╝██║░░░██║██████╔╝██║░░██╗░███████║░░░██║░░░██║░░██║██████╔╝░╚████╔╝░
                &c██╔═══╝░██║░░░██║██╔══██╗██║░░╚██╗██╔══██║░░░██║░░░██║░░██║██╔══██╗░░╚██╔╝░░
                &c██║░░░░░╚██████╔╝██║░░██║╚██████╔╝██║░░██║░░░██║░░░╚█████╔╝██║░░██║░░░██║░░░
                &c╚═╝░░░░░░╚═════╝░╚═╝░░╚═╝░╚═════╝░╚═╝░░╚═╝░░░╚═╝░░░░╚════╝░╚═╝░░╚═╝░░░╚═╝░░░
                
                &rThe &cPurgatory&r is a &cVanixStudios&r project.
                &aConnected to the &eProxy&r.
                
                """;

        ProxyServer.getInstance ( ).getConsole ( ).sendMessage ( new TextComponent ( C.translate ( art ) ) );
    }

    public static void asciiArtStop() {
        String artstop =
                """

                &c██████╗░██╗░░░██╗██████╗░░██████╗░░█████╗░████████╗░█████╗░██████╗░██╗░░░██╗
                &c██╔══██╗██║░░░██║██╔══██╗██╔════╝░██╔══██╗╚══██╔══╝██╔══██╗██╔══██╗╚██╗░██╔╝
                &c██████╔╝██║░░░██║██████╔╝██║░░██╗░███████║░░░██║░░░██║░░██║██████╔╝░╚████╔╝░
                &c██╔═══╝░██║░░░██║██╔══██╗██║░░╚██╗██╔══██║░░░██║░░░██║░░██║██╔══██╗░░╚██╔╝░░
                &c██║░░░░░╚██████╔╝██║░░██║╚██████╔╝██║░░██║░░░██║░░░╚█████╔╝██║░░██║░░░██║░░░
                &c╚═╝░░░░░░╚═════╝░╚═╝░░╚═╝░╚═════╝░╚═╝░░╚═╝░░░╚═╝░░░░╚════╝░╚═╝░░╚═╝░░░╚═╝░░░
                
                &rThe &cPurgatory&r is a &cVanixStudios&r project.
                &cDisconnect to the &eProxy&r.
                &cServer is stopping...
                """;

        ProxyServer.getInstance ( ).getConsole ( ).sendMessage ( new TextComponent ( C.translate ( artstop ) ) );
    }
}
