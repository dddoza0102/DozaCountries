package dev.doza.dozacountres;

import dev.doza.dozacountres.commands.Country;
import dev.doza.dozacountres.events.PlaceFlagEvent;
import dev.doza.dozacountres.utils.CountryUtil;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Dozacountres extends JavaPlugin {
    private FileConfiguration messagesConfig;
    private File messages;

    @Override
    public void onEnable() {
        CountryUtil util = new CountryUtil();
        saveDefaultConfig();
        reloadConfig();
        final PlaceFlagEvent pfe = new PlaceFlagEvent(util);
        getServer().getPluginManager().registerEvents(pfe, this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("country", new Country(util));
        });
        createMessagesConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    private void createMessagesConfig() {
        messages = new File(getDataFolder(), "messages.yml");

        if (!messages.exists()) {
            messages.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messages);
    }

    public FileConfiguration getMessagesConfig() {
        return this.messagesConfig;
    }
}
