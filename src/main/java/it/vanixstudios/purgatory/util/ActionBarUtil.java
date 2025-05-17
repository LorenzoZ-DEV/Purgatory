package it.vanixstudios.purgatory.util;

import it.vanixstudios.purgatory.manager.BanInfo;
import it.vanixstudios.purgatory.manager.BanManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;

public class ActionBarUtil {

    private static final String NETWORK_NAME = "x-network";

    public static void sendJailActionBar(ProxiedPlayer player) {
        if (player == null || !player.isConnected()) return;

        BanManager banManager = BanManager.getInstance();
        if (banManager == null) return;

        if (!banManager.isBanned (player.getUniqueId())) return;

        BanInfo banInfo = banManager.getLatestBanInfo (player.getUniqueId());
        if (banInfo == null) return;

        String message = createJailMessage(banInfo);
        sendActionBar(player, message);
    }

    private static String createJailMessage(BanInfo banInfo) {
        if (banInfo.isPermanent()) {
            return colorize("&cYou have gotten a life sentence for " + banInfo.getReason() + ".");
        } else {
            String durationStr = formatDurationRemaining(banInfo);
            return colorize("&cYou have been jailed for " + durationStr + " for " + banInfo.getReason() + ".");
        }
    }

    private static String formatDurationRemaining(BanInfo banInfo) {
        long elapsed = System.currentTimeMillis() - banInfo.getBanTimestamp();
        long remaining = banInfo.getDuration() - elapsed;

        long seconds = (remaining / 1000) % 60;
        long minutes = (remaining / (1000 * 60)) % 60;
        long hours = (remaining / (1000 * 60 * 60)) % 24;
        long days = remaining / (1000 * 60 * 60 * 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" days ");
        if (hours > 0) sb.append(hours).append(" hours ");
        if (minutes > 0) sb.append(minutes).append(" minutes ");
        if (seconds > 0) sb.append(seconds).append(" seconds");

        return sb.toString().trim();
    }

    private static void sendActionBar(ProxiedPlayer player, String message) {
        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    private static String colorize(String message) {
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
    }
}
