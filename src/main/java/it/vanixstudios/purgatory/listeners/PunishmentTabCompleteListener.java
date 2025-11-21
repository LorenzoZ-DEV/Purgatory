package it.vanixstudios.purgatory.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class PunishmentTabCompleteListener implements Listener {

    private static final Set<String> TARGET_COMMANDS = Set.of(
            "ban", "tempban", "unban", "history", "blacklist",
            "blacklistinfo", "unblacklist", "mute", "tempmute",
            "unmute", "checkmute", "checkban", "kick"
    );

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (event.isCancelled()) return;

        String cursor = event.getCursor();
        if (cursor == null || cursor.isEmpty() || !cursor.startsWith("/")) return;

        String trimmed = cursor.substring(1);
        String[] parts = trimmed.split(" ", -1);
        if (parts.length == 0) return;

        String label = parts[0].toLowerCase(Locale.ROOT);
        if (!TARGET_COMMANDS.contains(label)) return;

        boolean hasSpace = trimmed.contains(" ");
        if (!hasSpace) return; // still typing command label

        if (parts.length > 2) return; // only handle first argument

        String prefix = parts.length == 1 ? "" : parts[1];
        if (cursor.endsWith(" ")) {
            prefix = "";
        }

        Collection<String> suggestions = event.getSuggestions();
        suggestions.clear();
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);

        ProxyServer.getInstance().getPlayers().stream()
                .map(ProxiedPlayer::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEachOrdered(suggestions::add);
    }
}

