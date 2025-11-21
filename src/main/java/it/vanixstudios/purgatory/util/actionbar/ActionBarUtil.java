package it.vanixstudios.purgatory.util.actionbar;

import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.manager.bans.BanInfo;
import it.vanixstudios.purgatory.manager.bans.BanManager;
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
            return colorize(Purgatory.getConfigManager().getMessages().getString("ban.action_bar_permanently","&cYou have got a life sentence for {reason}.").replace("{reason}", banInfo.getReason()));
        } else {
            String durationStr = formatDurationRemaining(banInfo);
            return colorize(Purgatory.getConfigManager().getMessages().getString("ban.action_bar_temporary","&cYou have jailed for a period {duration} for {reason}").replace("{duration}", durationStr).replace("{reason}", banInfo.getReason()));
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
        if (days > 0) sb.append(days).append(" giorni ");
        if (hours > 0) sb.append(hours).append(" ore ");
        if (minutes > 0) sb.append(minutes).append(" minuti ");
        if (seconds > 0) sb.append(seconds).append(" secondi");

        return sb.toString().trim();
    }

    private static void sendActionBar(ProxiedPlayer player, String message) {
        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    private static String colorize(String message) {
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
    }
}
