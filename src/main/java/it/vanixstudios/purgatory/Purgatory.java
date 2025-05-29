package it.vanixstudios.purgatory;

import com.mongodb.client.MongoCollection;
import it.vanixstudios.purgatory.cmds.Mute.CheckMuteCommand;
import it.vanixstudios.purgatory.cmds.Mute.MuteCommand;
import it.vanixstudios.purgatory.cmds.Mute.TempMuteCommand;
import it.vanixstudios.purgatory.cmds.Mute.UnMuteCommand;
import it.vanixstudios.purgatory.cmds.admin.PurgatoryCmd;
import it.vanixstudios.purgatory.cmds.alts.AltsCommand;
import it.vanixstudios.purgatory.cmds.bans.*;
import it.vanixstudios.purgatory.cmds.blacklist.BlacklistCommand;
import it.vanixstudios.purgatory.cmds.blacklist.BlacklistInfoCommand;
import it.vanixstudios.purgatory.cmds.blacklist.UnblacklistCommand;
import it.vanixstudios.purgatory.cmds.kick.KickCommand;
import it.vanixstudios.purgatory.listeners.bans.BlacklistListener;
import it.vanixstudios.purgatory.listeners.bans.CommandBlacklistListener;
import it.vanixstudios.purgatory.listeners.bans.PlayerLoginListener;
import it.vanixstudios.purgatory.listeners.bans.ServerConnectListener;
import it.vanixstudios.purgatory.listeners.evasion.BanEvadeListener;
import it.vanixstudios.purgatory.listeners.mute.ChatListener;
import it.vanixstudios.purgatory.manager.bans.BanManager;
import it.vanixstudios.purgatory.manager.config.ConfigManager;
import it.vanixstudios.purgatory.manager.mute.MuteManager;
import it.vanixstudios.purgatory.model.ProfileManager;
import it.vanixstudios.purgatory.storage.MongoManager;
import it.vanixstudios.purgatory.tasks.BanActionBarTask;
import it.vanixstudios.purgatory.util.console.Art;
import it.vanixstudios.purgatory.util.console.Logger;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import revxrsal.commands.Lamp;
import revxrsal.commands.bungee.BungeeLamp;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public final class Purgatory extends Plugin {

    @Getter
    private static Purgatory instance;
    @Getter
    private BanManager banManager;
    @Getter
    private MuteManager muteManager;
    @Getter
    private MongoManager mongoManager;
    @Getter
    private ProfileManager profileManager;
    @Getter
    private static final ConfigManager configManager = new ConfigManager();

    @Getter
    private Path dataDirectory;


    public static Purgatory getInstance() {
        return Purgatory.instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Logger.info("&aStarting the plugin...");
        this.dataDirectory = getDataFolder().toPath();

        createStorageFile();
        loadMongoConnection();
        registerListener();
        registerCommands();
        try{
            configManager.loadMessages();
            configManager.loadConfig();
            ConfigManager.load();
            Logger.info("&aLoaded config.yml file.");
            Logger.info("&aLoaded messages.yml file.");

        } catch (Exception e){
            Logger.error("Error loading config.yml: " + e.getMessage());
            e.printStackTrace();
            Logger.info("&cShutting down plugin...");
            ProxyServer.getInstance().stop();
            return;
        }
        Logger.info("&aPlugin started successfully!");
        Art.asciiArt();
        ProxyServer.getInstance().getScheduler().schedule(
                this,
                new BanActionBarTask(banManager),
                0,
                1,
                java.util.concurrent.TimeUnit.SECONDS
        );
    }

    private void createStorageFile() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        File file = new File(getDataFolder(), "storage.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("storage.yml");
                 OutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                int length;
                while ((length = in.read(buf)) > 0) {
                    out.write(buf, 0, length);
                }
                Logger.info("&aCreated storage.yml file.");
            } catch (IOException e) {
                Logger.error("Unable to create storage.yml: " + e.getMessage());
            }
        }
    }

    private void registerListener() {
        List.of(
                new PlayerLoginListener(),
                new ServerConnectListener(),
                new CommandBlacklistListener(banManager),
                new BanEvadeListener(),
                new ChatListener(muteManager),
                new BlacklistListener ()
        ).forEach(listener -> getProxy().getPluginManager().registerListener(this, listener));
    }

    private void loadMongoConnection() {
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(new File(getDataFolder(), "storage.yml"));

            boolean useUri = config.getBoolean("MONGO.URI", false);
            String uriLink = config.getString("MONGO.URI_LINK", "");
            String host = config.getString("MONGO.HOST", "localhost");
            int port = config.getInt("MONGO.PORT", 27017);
            String database = config.getString("MONGO.DATABASE", "purgatory");

            Logger.database("&aConnecting to MongoDB...");

            mongoManager = useUri
                    ? new MongoManager(uriLink, database)
                    : new MongoManager(false, database, host, port, null);

            MongoCollection bans = mongoManager.getDatabase().getCollection("bans");

            banManager = new BanManager(bans);
            muteManager = new MuteManager(mongoManager.getDatabase());
            profileManager = new ProfileManager(this);
            profileManager.load();

            Logger.database("&aMongoDB connection successful.");
        } catch (IOException e) {
            Logger.error("Error loading storage.yml: " + e.getMessage());
        } catch (Exception e) {
            Logger.error("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void registerCommands() {
        Logger.info("&aRegistering commands...");
        Lamp<BungeeCommandActor> lamp = BungeeLamp.builder(this).build();

        if (banManager != null) {
            lamp.register(
                    new BanCommand(banManager),
                    new TempbanCommand(banManager),
                    new HistoryCommand(banManager),
                    new BanListCommand(),
                    new CheckBanCommand(),
                    new UnbanCommand(banManager),
                    new AltsCommand(),
                    new KickCommand(),
                    new PurgatoryCmd()
            );
        } else {
            Logger.error("BanManager is not initialized. Ban-related commands will not be registered.");
        }

        if (muteManager != null) {
            lamp.register(
                    new MuteCommand(muteManager),
                    new TempMuteCommand(muteManager),
                    new UnMuteCommand(muteManager),
                    new CheckMuteCommand(muteManager)
            );
        } else {
            Logger.error("MuteManager is not initialized. Mute-related commands will not be registered.");
        }

        MongoCollection blacklistCollection = mongoManager != null ?
                mongoManager.getDatabase().getCollection("blacklist") : null;

        if (blacklistCollection != null) {
            lamp.register(
                    new BlacklistCommand (),
                    new BlacklistInfoCommand(),
                    new UnblacklistCommand(blacklistCollection)
            );
        } else {
            Logger.error("Blacklist collection is not initialized. Blacklist-related commands will not be registered.");
        }

    }
    @Override
    public void onDisable() {
        Logger.info("&cShutting down plugin...");
        Art.asciiArtStop();

        try{
            if (profileManager != null) profileManager.close();

            if (mongoManager != null) {
                mongoManager.close();
                Logger.database("&cMongoDB connection closed.");
            }
        } catch (Exception e){
            Logger.error("Error shutting down plugin: " + e.getMessage());
        }

        banManager = null;
        mongoManager = null;
        instance = null;
        muteManager = null;

        Logger.info("&cPlugin disabled.");
    }
}