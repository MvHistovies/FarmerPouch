package com.mrhistories.farmerpouch.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.Collection;

public class WorldGuardHook {

    private final boolean available;

    public WorldGuardHook() {
        this.available = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean hasRegion(World world, String regionId) {
        if (!available) return false;
        if (world == null || regionId == null || regionId.isBlank()) return false;

        try {
            Object regionManager = getRegionManager(world);
            if (regionManager == null) return false;

            Method hasRegion = regionManager.getClass().getMethod("hasRegion", String.class);
            Object result = hasRegion.invoke(regionManager, regionId);
            return result instanceof Boolean b && b;

        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isInRegion(World world, Location loc, String regionId) {
        if (!available) return false;
        if (world == null || loc == null || regionId == null || regionId.isBlank()) return false;

        try {
            Object regionManager = getRegionManager(world);
            if (regionManager == null) return false;

            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Method asBlockVector = bukkitAdapter.getMethod("asBlockVector", Location.class);
            Object blockVec3 = asBlockVector.invoke(null, loc);

            Method getApplicableRegions = regionManager.getClass().getMethod(
                    "getApplicableRegions",
                    Class.forName("com.sk89q.worldedit.math.BlockVector3")
            );
            Object applicable = getApplicableRegions.invoke(regionManager, blockVec3);

            Method getRegions = applicable.getClass().getMethod("getRegions");
            Object regionsObj = getRegions.invoke(applicable);

            if (!(regionsObj instanceof Collection<?> regions)) return false;

            for (Object pr : regions) {
                Method getId = pr.getClass().getMethod("getId");
                String id = String.valueOf(getId.invoke(pr));
                if (id.equalsIgnoreCase(regionId)) return true;
            }
            return false;

        } catch (Throwable t) {
            return false;
        }
    }

    private Object getRegionManager(World world) throws Exception {
        Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
        Method getInstance = wgClass.getMethod("getInstance");
        Object wg = getInstance.invoke(null);

        Method getPlatform = wg.getClass().getMethod("getPlatform");
        Object platform = getPlatform.invoke(wg);

        Method getRegionContainer = platform.getClass().getMethod("getRegionContainer");
        Object regionContainer = getRegionContainer.invoke(platform);

        Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Method adaptWorld = bukkitAdapter.getMethod("adapt", World.class);
        Object weWorld = adaptWorld.invoke(null, world);

        Method get = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World"));
        return get.invoke(regionContainer, weWorld);
    }
}
