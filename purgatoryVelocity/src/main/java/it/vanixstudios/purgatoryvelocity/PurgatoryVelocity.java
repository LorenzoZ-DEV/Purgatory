package it.vanixstudios.purgatoryvelocity;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import it.vanixstudios.purgatoryvelocity.manager.BanManager;
import it.vanixstudios.purgatoryvelocity.manager.ConfigManager;
import it.vanixstudios.purgatoryvelocity.storage.MongoManager;
import it.vanixstudios.purgatoryvelocity.util.console.Art;
import it.vanixstudios.purgatoryvelocity.util.console.Logger;
import lombok.Getter;
import revxrsal.commands.Lamp;
import revxrsal.commands.velocity.VelocityLamp;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "purgatoryvelocity",
        name = "purgatoryVelocity",
        version = BuildConstants.VERSION
)
public class PurgatoryVelocity {

    @Getter
    private final Logger logger;
    @Getter
    public static ProxyServer server;
    @Getter
    private static final ConfigManager configManager = new ConfigManager();
    @Getter
    private final Path dataDirectory;
    @Getter
    private BanManager banManager;
    @Inject
    private MongoManager mongoManager;
    @Getter
    private static PurgatoryVelocity instance;

    PurgatoryVelocity instance() {
        return this;
    }
    public PurgatoryVelocity(ProxyServer server, Path dataDirectory , Logger logger) {
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.dataDirectory.toFile().mkdirs();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        Logger.info("&aStarting PurgatoryVelocity!");
        try{
            Art.asciiart();
        } catch (Exception e) {
            Logger.error("&cFatal error when starting PurgatoryVelocity!");
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        Logger.info("&cShutting down PurgatoryVelocity!");
        instance = null;
    }

    private void loadMongoConnection() {
        try {
            YamlDocument config = YamlDocument.create(
                    new File(getDataDirectory().toFile(), "storage.yml"),
                    getClass().getResourceAsStream("/storage.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config_version")).build()
            );

            boolean useUri = config.getBoolean("MONGO.URI", false);
            String uriLink = config.getString("MONGO.URI_LINK", "");
            String host = config.getString("MONGO.HOST", "localhost");
            int port = config.getInt("MONGO.PORT", 27017);
            String database = config.getString("MONGO.DATABASE", "purgatory");

            Logger.database("&aConnecting to MongoDB...");

            mongoManager = useUri
                    ? new MongoManager(uriLink, database)
                    : new MongoManager(false, database, host, port, uriLink);

            MongoCollection bans = mongoManager.getDatabase().getCollection("bans");

            banManager = new BanManager(bans);
            muteManager = new MuteManager(mongoManager.getDatabase());
            profileManager = new ProfileManager(this);
            profileManager.load();

            Logger.database("&aMongoDB connection successful.");
        } catch (IOException e) {
            Logger.error("Error loading storage.yml: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Logger.error("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void registerCommands(){
        Logger.info("&aRegistering commands...");
        Lamp<VelocityCommandActor> lamp = VelocityLamp.builder(this).registerCommands().build();

    }

    public static PurgatoryVelocity getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }
}
