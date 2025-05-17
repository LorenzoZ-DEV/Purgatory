package it.vanixstudios.purgatory.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Logger {

    public static void info(String message) {
        message = ChatColor.translateAlternateColorCodes ( '&', message );
        message = "&e[PURGATORY] &7 " + message;
        ProxyServer.getInstance ( ).getConsole ( ).sendMessage ( C.translate ( message ) );
    }

    public static void warning(String message) {
        ProxyServer.getInstance ( ).getLogger ( ).warning ( "[PURGATORY] " + message );
    }

    public static void error(String message) {
        ProxyServer.getInstance ( ).getLogger ( ).severe ( "[PURGATORY] " + message );
    }

    public static void database(String message) {
        message = ChatColor.translateAlternateColorCodes ( '&', message );
        message = "&2[MONGODB] &7 " + message;
        ProxyServer.getInstance ( ).getConsole ( ).sendMessage ( C.translate ( message ) );
    }

    public static void line() {
        ProxyServer.getInstance ( ).getConsole ( ).sendMessage ( C.translate ( "&7----------------------------------------" ) );
    }

    public static void debug(String message) {
        message = ChatColor.translateAlternateColorCodes ( '&', message );
        message = "&4[DEBUG] &7 " + message;
        ProxyServer.getInstance ( ).getConsole ( ).sendMessage ( C.translate ( message ) );
    }
}

