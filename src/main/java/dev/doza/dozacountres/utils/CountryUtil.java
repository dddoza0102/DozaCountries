package dev.doza.dozacountres.utils;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import dev.doza.dozacountres.Dozacountres;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CountryUtil {
    public void AddPlayer(String nameCountry, String playername, Player playerd){
        Player player = Bukkit.getPlayer(playername);
        List<String> PopulationNames = Dozacountres.getPlugin(Dozacountres.class).getConfig().getStringList("countries."+nameCountry+".PopulationNames");
        PopulationNames.add(player.getName());
        Dozacountres.getPlugin(Dozacountres.class).getConfig().set("players",PopulationNames);
        playerd.sendMessage(ChatColor.GREEN + "Вы успешно добавили игрока "+playername+" в страну "+nameCountry);
    }
    private final Dozacountres plugin = Dozacountres.getPlugin(Dozacountres.class);

    public void CreateCountry(String countryName, Player president, Player creator) {
        FileConfiguration config = Dozacountres.getPlugin(Dozacountres.class).getConfig();
        config.set("countries." + countryName + ".President", president.getName());
        config.set("countries." + countryName + ".PopulationNames", List.of(president.getName()));

        config.set("players." + president.getName(), countryName);

        ItemStack item = president.getInventory().getItemInMainHand();
        if (item.getItemMeta() instanceof BannerMeta meta) {
            List<String> serializedPatterns = new ArrayList<>();
            for (org.bukkit.block.banner.Pattern p : meta.getPatterns()) {
                String color = p.getColor().name();
                String pattern = org.bukkit.Registry.BANNER_PATTERN.getKey(p.getPattern()).getKey();
                serializedPatterns.add(color + ":" + pattern);
            }
            config.set("countries." + countryName + ".flag", serializedPatterns);
        }

        Dozacountres.getPlugin(Dozacountres.class).saveConfig();
        creator.sendMessage("§aСтрана " + countryName + " успешно создана!");
    }

    public void SetFlag(String countryName, Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!(item.getItemMeta() instanceof BannerMeta meta)) {
            player.sendMessage("§cОшибка: Вы должны держать флаг в основной руке!");
            return;
        }

        List<String> serializedPatterns = new ArrayList<>();
        for (org.bukkit.block.banner.Pattern p : meta.getPatterns()) {
            String color = p.getColor().name();
            String pattern = org.bukkit.Registry.BANNER_PATTERN.getKey(p.getPattern()).getKey();
            serializedPatterns.add(color + ":" + pattern);
        }

        FileConfiguration config = Dozacountres.getPlugin(Dozacountres.class).getConfig();
        config.set("countries." + countryName + ".flag", serializedPatterns);

        Dozacountres.getPlugin(Dozacountres.class).saveConfig();

        player.sendMessage("§aНовый флаг для страны §f" + countryName + " §aуспешно установлен!");
    }

    public void saveBannerToConfig(BannerMeta meta, String path, FileConfiguration config) {
        List<String> patternStrings = new ArrayList<>();
        for (Pattern pattern : meta.getPatterns()) {
            String entry = pattern.getColor().name() + ":" + pattern.getPattern().toString();
            patternStrings.add(entry);
        }
        config.set(path, patternStrings);
        plugin.saveConfig();
    }
    public void createAndSaveRegion(World world, String id, int x1, int y1, int z1, int x2, int y2, int z2, Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) return;

        if (regions.hasRegion(id)) {
            player.sendMessage("§cЭтот чанк уже занят вашей страной!");
            return;
        }

        BlockVector3 min = BlockVector3.at(x1, y1, z1);
        BlockVector3 max = BlockVector3.at(x2, y2, z2);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(id, min, max);

        // Берем данные страны из конфига
        String playercountry = Dozacountres.getPlugin(Dozacountres.class).getConfig().getString("players." + player.getName());
        List<String> players = Dozacountres.getPlugin(Dozacountres.class).getConfig().getStringList("countries."+playercountry+".PopulationNames");
        List<UUID> uuids = new ArrayList<>();

        List<String> owners = Dozacountres.getPlugin(Dozacountres.class).getConfig().getStringList("countries." + playercountry + ".President");
        DefaultDomain regionOwners = region.getOwners();
        for (String name : owners) {
            regionOwners.addPlayer(Bukkit.getOfflinePlayer(name).getUniqueId());
        }
        List<UUID> ownersuuids = new ArrayList<>();

        List<String> members = Dozacountres.getPlugin(Dozacountres.class).getConfig().getStringList("countries." + playercountry + ".PopulationNames");
        DefaultDomain regionMembers = region.getMembers();
        for (String name : members) {
            regionMembers.addPlayer(Bukkit.getOfflinePlayer(name).getUniqueId());
        }

        if (regions != null) {
            regions.addRegion(region);
            addUuidsToRegion(world, region.getId(), uuids);
            addOwnersToRegion(world, region.getId(),ownersuuids);
            region.setFlag(Flags.PVP, StateFlag.State.DENY);
            region.setFlag(Flags.PVP.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.INTERACT, StateFlag.State.DENY);
            region.setFlag(Flags.INTERACT.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
            region.setFlag(Flags.BLOCK_BREAK.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
            region.setFlag(Flags.BLOCK_PLACE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.USE, StateFlag.State.ALLOW);
            region.setFlag(Flags.USE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
            region.setFlag(Flags.CHEST_ACCESS.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.SLEEP, StateFlag.State.DENY);
            region.setFlag(Flags.SLEEP.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.TNT, StateFlag.State.ALLOW);
            region.setFlag(Flags.TNT.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.LIGHTER, StateFlag.State.ALLOW);
            region.setFlag(Flags.LIGHTER.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.ITEM_FRAME_ROTATE, StateFlag.State.ALLOW);
            region.setFlag(Flags.ITEM_FRAME_ROTATE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
            region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
            try {
                regions.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void addUuidsToRegion(World world, String regionId, List<UUID> uuids) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions != null) {
            ProtectedRegion region = regions.getRegion(regionId);

            if (region != null) {
                DefaultDomain members = region.getMembers();
                uuids.forEach(members::addPlayer);
            }
        }
    }
    public void addOwnersToRegion(World world, String regionId, List<UUID> uuids) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions != null) {
            ProtectedRegion region = regions.getRegion(regionId);
            if (region != null) {
                DefaultDomain owners = region.getOwners();
                for (UUID uuid : uuids) {
                    owners.addPlayer(uuid);
                }
            }
        }
    }
    public void removeRegion(World world, String id, Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions != null && regions.hasRegion(id)) {
            regions.removeRegion(id);
            try {
                regions.save();
                player.sendMessage("§aФлаг сломан: территория чанка теперь свободна.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
