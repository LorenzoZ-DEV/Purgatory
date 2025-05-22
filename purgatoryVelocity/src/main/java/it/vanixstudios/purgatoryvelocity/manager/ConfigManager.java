package it.vanixstudios.purgatoryvelocity.manager;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import it.vanixstudios.purgatoryvelocity.PurgatoryVelocity;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigManager {

    @Getter
    private YamlDocument messages;
    private YamlDocument config;

    public void loadMessages() {
        try {
            // Changed versioning key from "config-version" to "config_version" to match your YAML
            messages = YamlDocument.create(
                    new File(PurgatoryVelocity.getInstance().getDataDirectory().toFile(), "messages.yml"),
                    getClass().getResourceAsStream("/messages.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config_version")).build()
            );
            messages.update();
            messages.save();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            // Changed versioning key from "config-version" to "config_version" to match your YAML
            config = YamlDocument.create(
                    new File(PurgatoryVelocity.getInstance().getDataDirectory().toFile(), "config.yml"),
                    getClass().getResourceAsStream("/config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config_version")).build()
            );
            config.update();
            config.save();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void load() {
        // Removed redundant loading here to avoid circular loading
        // The main class already calls both loadConfig and loadMessages
    }

    public void reload() throws IOException {
        messages.reload();
        config.reload();
    }
}