package it.vanixstudios.purgatoryvelocity.util.console;

import it.vanixstudios.purgatoryvelocity.util.C;
import net.md_5.bungee.api.ProxyServer;

public class Logger {
    public static void info(String message) {
        message = C.translate("&e[PURGATORY] &7 " + message);
        ProxyServer.getInstance().getConsole().sendMessage(message);
    }

    public static void warning(String message) {
        ProxyServer.getInstance().getLogger().warning("[PURGATORY] " + message);
    }

    public static void error(String message) {
        ProxyServer.getInstance().getLogger().severe("[PURGATORY] " + message);
    }

    public static void database(String message) {
        message = C.translate("&2[MONGODB] &7 " + message);
        ProxyServer.getInstance().getConsole().sendMessage(message);
    }

    public static void line() {
        ProxyServer.getInstance().getConsole().sendMessage(C.translate("&7----------------------------------------"));
    }

    public static void debug(String message) {
        message = C.translate("&4[DEBUG] &7 " + message);
        ProxyServer.getInstance().getConsole().sendMessage(message);
    }
}
