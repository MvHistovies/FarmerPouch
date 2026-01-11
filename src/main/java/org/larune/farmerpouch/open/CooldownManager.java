package com.mrhistories.farmerpouch.open;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import com.mrhistories.farmerpouch.pouch.PouchRarity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final FarmerPouchPlugin plugin;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    // key: uuid:rarity -> epochSecondsUntil

    public CooldownManager(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    private String key(UUID uuid, PouchRarity rarity) {
        return uuid + ":" + rarity.name();
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("cooldowns.enabled", true);
    }

    public long getRemainingSeconds(UUID uuid, PouchRarity rarity) {
        long now = System.currentTimeMillis() / 1000L;
        Long until = cooldowns.get(key(uuid, rarity));
        if (until == null) return 0;
        return Math.max(0, until - now);
    }

    public boolean canUse(UUID uuid, PouchRarity rarity) {
        return getRemainingSeconds(uuid, rarity) <= 0;
    }

    public void apply(UUID uuid, PouchRarity rarity) {
        if (!isEnabled()) return;

        int sec = plugin.getConfig().getInt("cooldowns.values_seconds." + rarity.name(), 0);
        if (sec <= 0) return;

        long now = System.currentTimeMillis() / 1000L;
        cooldowns.put(key(uuid, rarity), now + sec);
    }
}
