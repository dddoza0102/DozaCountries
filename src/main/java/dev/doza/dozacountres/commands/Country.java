package dev.doza.dozacountres.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import dev.doza.dozacountres.utils.CountryUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class Country implements BasicCommand {
    private final CountryUtil util;
    public Country(CountryUtil util) {
        this.util = util;
    }
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) return;

        if (!player.hasPermission("dozacountries.command")) return;else{player.sendMessage(util.message("Permission"));}
        if (args.length == 0) {
            player.sendMessage(util.message("UsingCommand"));
            return;
        }

        String cmd = args[0];

        switch (cmd.toLowerCase()) {
            case "create" -> {
                Player targetPresident = Bukkit.getPlayer(args[2]);
                if (targetPresident == null) {
                    player.sendMessage(util.messagePlayer("OfflinePlayer", args[2]));
                }
                util.CreateCountry(args[1], targetPresident, player);
            }
            case "add" -> {
                Player targetPresident = Bukkit.getPlayer(args[2]);
                if (targetPresident == null) {
                    player.sendMessage(util.messagePlayer("OfflinePlayer", args[2]));
                }
                util.AddPlayer(args[1], targetPresident.getName(), player);
            }
            case "setflag" -> {
                if (args.length >= 2) {
                    util.SetFlag(args[1], player);
                }
            }
            default -> player.sendMessage(util.message("UnknownArg"));
        }
    }
}
