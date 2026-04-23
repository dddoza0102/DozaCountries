package dev.doza.dozacountres;

import com.mojang.brigadier.Command;
import dev.doza.dozacountres.commands.Country;
import dev.doza.dozacountres.events.PlaceFlagEvent;
import dev.doza.dozacountres.utils.CountryUtil;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Dozacountres extends JavaPlugin {

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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
