package com.mrhistories.farmerpouch.util;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionCheckUtil {

    public static boolean canUseSystemHere(FarmerPouchPlugin plugin, Player player) {
        RegionMode mode = RegionMode.fromConfig(plugin.getConfig().getString("region-lock.mode"));

        if (mode == RegionMode.NONE) return true;

        if (mode == RegionMode.WORLD_AND_AREA) {
            return isInArea(plugin, player);
        }

        if (mode == RegionMode.WORLDGUARD_REGION) {
            return isInWorldGuardRegion(plugin, player);
        }

        return true;
    }

    private static boolean isInArea(FarmerPouchPlugin plugin, Player player) {
        String worldName = player.getWorld().getName();
        String base = "region-lock.area.worlds." + worldName + ".";

        if (!plugin.getConfig().contains(base + "x1")) return false;

        int x1 = plugin.getConfig().getInt(base + "x1");
        int z1 = plugin.getConfig().getInt(base + "z1");
        int x2 = plugin.getConfig().getInt(base + "x2");
        int z2 = plugin.getConfig().getInt(base + "z2");

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        Location loc = player.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    private static boolean isInWorldGuardRegion(FarmerPouchPlugin plugin, Player player) {
        String worldName = player.getWorld().getName();
        String path = "region-lock.worldguard.worlds." + worldName + ".region";
        String regionName = plugin.getConfig().getString(path);

        if (regionName == null || regionName.isBlank()) return false;

        WorldGuardHook wg = plugin.getWorldGuardHook();
        if (wg == null || !wg.isAvailable()) return false;

        return wg.isInRegion(player.getWorld(), player.getLocation(), regionName);
    }
}
