package com.mrhistories.farmerpouch.open;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OpeningSessionManager {

    private final FarmerPouchPlugin plugin;
    private final Set<UUID> opening = ConcurrentHashMap.newKeySet();

    public OpeningSessionManager(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOpening(UUID uuid) {
        return opening.contains(uuid);
    }

    public boolean start(Player player) {
        if (!plugin.getConfig().getBoolean("opening.one_at_a_time", true)) return true;
        return opening.add(player.getUniqueId());
    }

    public void end(UUID uuid) {
        opening.remove(uuid);
    }

    public void end(Player player) {
        end(player.getUniqueId());
    }

    public void endAll() {
        opening.clear();
    }

    public void cancelIfOnline(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            end(uuid);
        }
    }
}
