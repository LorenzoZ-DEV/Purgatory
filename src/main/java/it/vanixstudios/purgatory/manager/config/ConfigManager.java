package it.vanixstudios.purgatory.manager.config;

import it.vanixstudios.purgatory.Purgatory;
import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigManager {

    @Getter
    private Configuration messages;
    @Getter
    private Configuration config;

    public void loadMessages() throws IOException {
        messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                new File(Purgatory.getInstance().getDataDirectory().toFile(), "messages.yml"));
    }

    public void loadConfig() throws IOException {
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                new File(Purgatory.getInstance().getDataDirectory().toFile(), "config.yml"));
    }

    public static void load() {

    }

    public void reload() throws IOException {
        messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                new File(Purgatory.getInstance().getDataDirectory().toFile(), "messages.yml"));
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                new File(Purgatory.getInstance().getDataDirectory().toFile(), "config.yml"));
    }

    public Configuration getMessages() {
        return messages;
    }

    public Configuration getConfig() {
        return config;
    }
}