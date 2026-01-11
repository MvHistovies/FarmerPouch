package com.mrhistories.farmerpouch.coin;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CoinManager {

    private final FarmerPouchPlugin plugin;
    private final File file;
    private YamlConfiguration data;

    public CoinManager(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "coins.yml");
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException ignored) {}
    }

    private String path(UUID uuid) {
        return "coins." + uuid.toString();
    }

    public long get(OfflinePlayer p) {
        return data.getLong(path(p.getUniqueId()), 0L);
    }

    public void set(OfflinePlayer p, long amount) {
        data.set(path(p.getUniqueId()), Math.max(0L, amount));
        save();
    }

    public void add(OfflinePlayer p, long amount) {
        set(p, get(p) + Math.max(0L, amount));
    }

    public boolean take(OfflinePlayer p, long amount) {
        long cur = get(p);
        if (amount <= 0) return true;
        if (cur < amount) return false;
        set(p, cur - amount);
        return true;
    }
}
