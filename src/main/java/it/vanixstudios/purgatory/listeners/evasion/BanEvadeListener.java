package it.vanixstudios.purgatory.listeners.evasion;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.model.Profile;
import it.vanixstudios.purgatory.util.strings.C;
import it.vanixstudios.purgatory.util.console.Logger;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanEvadeListener implements Listener {

    private final Purgatory purgatory;
    private final BanManager banManager = Purgatory.getInstance().getBanManager();

    public BanEvadeListener() {
        purgatory = Purgatory.getInstance();
    }

    private void calculateAlts(Profile profile) {
        CompletableFuture.runAsync(() -> {
            for (Profile profile1 : purgatory.getProfileManager().getProfileMap().values()) {
                if (profile.getName().equals(profile1.getName())) continue;
                if (profile.getAlts().contains(profile1.getUuid())) continue;

                for (String altIP : profile1.getIps()) {
                    profile.getIps().stream().filter(ip -> ip.equals(altIP))
                            .forEach(ip -> {
                                profile.getIps().add(altIP);
                                profile1.getIps().add(ip);

                                profile.getAlts().add(profile1.getUuid());
                                profile1.getAlts().add(profile.getUuid());
                            });
                }
            }
        });
    }

    @EventHandler
    public void onPlayerPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String ip = player.getAddress().getAddress().getHostAddress();
        Profile profile = purgatory.getProfileManager().getProfile(player.getUniqueId());

        if (profile == null) {
            profile = new Profile(player.getUniqueId(), player.getName());
        }

        if (!ip.equals("127.0.0.1")) {
            profile.setLastIP(ip);
            profile.getIps().add(ip);
        }

        purgatory.getProfileManager().save(profile);
        calculateAlts(profile);

        String playerIP = player.getAddress().getAddress().getHostAddress();

        if (profile == null) {
            player.disconnect(C.translate("&4&lWARNING: &cYour profile has failed to load. Please relog!"));
            return;
        }

        if (banManager.isBanned(player.getUniqueId())) {
            return;
        }

        for (UUID alt : profile.getAlts()) {
            if (!banManager.isBanned(alt)) continue;

            Document evadingBan = banManager.getBansCollection()
                    .find(Filters.eq("uuid", alt.toString()))
                    .sort(Sorts.descending("bannedAt"))
                    .first();

            if (evadingBan == null) continue;

            String bannedName = evadingBan.getString("name");
            String reason = evadingBan.getString("reason");

            banManager.ban(player.getUniqueId(), player.getName(), "Ban Evading (Alt of " + bannedName + ")", playerIP, "CONSOLE");
            banManager.sendToJail ( player );

            Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
            for (ProxiedPlayer p : players) {
                if (p.hasPermission("purgatory.staff")) {
                    String message = "";
                    try {
                        String serverName = player.getServer() != null ? player.getServer().getInfo().getName() : "connecting";
                        message = C.translate("&7(&c!&7) &7" + player.getName() + " &fwas sended to &c&lJAIL &ffor evading ban of &7" + bannedName);
                        TextComponent component = new TextComponent(message);
                        p.sendMessage(component);
                    } catch (Exception e) {
                        message = C.translate("&7(&c!&7) &7" + player.getName() + " &fwas sended to &c&lJAIL &ffor evading ban of & &7" + bannedName);
                        TextComponent component = new TextComponent(message);
                        p.sendMessage(component);
                    }
                    Logger.debug(message);
                }
            }
        
            // Esci dal loop dopo aver trovato il primo alt bannato
            break;
        }
    }
}