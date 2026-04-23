package dev.doza.dozacountres.events;

import dev.doza.dozacountres.Dozacountres;
import dev.doza.dozacountres.utils.CountryUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.List;

public class PlaceFlagEvent implements Listener {
    private final CountryUtil util;

    public PlaceFlagEvent(CountryUtil util) {
        this.util = util;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItemInHand();

        if (item.getItemMeta() instanceof BannerMeta meta) {
            String playercountry = Dozacountres.getPlugin(Dozacountres.class).getConfig().getString("players." + player.getName());
            if (playercountry == null) return;

            List<String> countryPatterns = Dozacountres.getPlugin(Dozacountres.class).getConfig().getStringList("countries." + playercountry + ".flag");

            List<String> handPatterns = new ArrayList<>();
            for (org.bukkit.block.banner.Pattern p : meta.getPatterns()) {
                handPatterns.add(p.getColor().name() + ":" + org.bukkit.Registry.BANNER_PATTERN.getKey(p.getPattern()).getKey());
            }

            if (!handPatterns.isEmpty() && handPatterns.equals(countryPatterns)) {
                Chunk chunk = e.getBlock().getChunk();
                String regionId = (playercountry + "_" + chunk.getX() + "_" + chunk.getZ()).toLowerCase();

                int minX = chunk.getX() << 4;
                int minZ = chunk.getZ() << 4;
                int maxX = minX + 15;
                int maxZ = minZ + 15;

                int minY = player.getWorld().getMinHeight();
                int maxY = player.getWorld().getMaxHeight();

                util.createAndSaveRegion(
                        player.getWorld(),
                        regionId,
                        minX, minY, minZ,
                        maxX, maxY, maxZ,
                        player
                );
            }
        }
    }
    @EventHandler
    public void onBreak(org.bukkit.event.block.BlockBreakEvent e) {
        org.bukkit.block.Block block = e.getBlock();

        if (block.getType().name().contains("BANNER")) {
            org.bukkit.block.Banner banner = (org.bukkit.block.Banner) block.getState();

            List<String> brokenPatterns = new ArrayList<>();
            for (org.bukkit.block.banner.Pattern p : banner.getPatterns()) {
                String patternType = org.bukkit.Registry.BANNER_PATTERN.getKey(p.getPattern()).getKey();
                brokenPatterns.add(p.getColor().name() + ":" + patternType);
            }

            String playercountry = Dozacountres.getPlugin(Dozacountres.class).getConfig().getString("players." + e.getPlayer().getName());
            if (playercountry == null) return;

            List<String> countryPatterns = Dozacountres.getPlugin(Dozacountres.class).getConfig().getStringList("countries." + playercountry + ".flag");

            if (!brokenPatterns.isEmpty() && brokenPatterns.equals(countryPatterns)) {
                org.bukkit.Chunk chunk = block.getChunk();
                String regionId = (playercountry + "_" + chunk.getX() + "_" + chunk.getZ()).toLowerCase();

                util.removeRegion(block.getWorld(), regionId, e.getPlayer());
            }
        }
    }
}
