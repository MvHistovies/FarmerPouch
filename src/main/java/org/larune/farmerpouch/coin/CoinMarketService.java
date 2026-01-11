package com.mrhistories.farmerpouch.coin;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoinMarketService {

    private final FarmerPouchPlugin plugin;
    private final Map<Integer, String> slotToKey = new HashMap<>();

    public CoinMarketService(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory buildMarket(Player player) {
        slotToKey.clear();

        int size = plugin.getConfig().getInt("coinmarket.size", 27);
        size = Math.max(9, Math.min(54, (size / 9) * 9));

        String title = plugin.color(plugin.getConfig().getString("coinmarket.title", "&aTarımCoin Market"));
        Inventory inv = Bukkit.createInventory(null, size, title);

        // 1) Filler (cam çerçeve / boşluk doldurma)
        if (plugin.getConfig().getBoolean("coinmarket.filler.enabled", true)) {
            ItemStack filler = readItem(plugin.getConfig().getConfigurationSection("coinmarket.filler"));
            if (filler != null) {
                for (int i = 0; i < size; i++) {
                    inv.setItem(i, filler);
                }
            }
        }

        // 2) Ürünleri yerleştir
        ConfigurationSection items = plugin.getConfig().getConfigurationSection("coinmarket.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(key);
                if (sec == null) continue;

                int slot = sec.getInt("slot", -1);
                if (slot < 0 || slot >= size) continue;

                ItemStack item = readItem(sec.getConfigurationSection("item"));
                if (item == null) item = new ItemStack(Material.BARRIER);

                inv.setItem(slot, item);
                slotToKey.put(slot, key);
            }
        }

        // 3) Bakiye bilgi item’i
        if (plugin.getConfig().getBoolean("coinmarket.balance-info.enabled", true)) {
            int slot = plugin.getConfig().getInt("coinmarket.balance-info.slot", 22);
            if (slot >= 0 && slot < size) {
                ItemStack info = buildBalanceInfo(player);
                if (info != null) {
                    inv.setItem(slot, info);
                }
            }
        }

        return inv;
    }

    public String getKeyBySlot(int slot) {
        return slotToKey.get(slot);
    }

    public long getPrice(String key) {
        return plugin.getConfig().getLong("coinmarket.items." + key + ".price", 0L);
    }

    public ItemStack getBuyItem(String key) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("coinmarket.items." + key + ".item");
        return readItem(sec);
    }

    public String[] getBuyCommands(String key) {
        return plugin.getConfig().getStringList("coinmarket.items." + key + ".on_buy_commands").toArray(new String[0]);
    }

    private ItemStack buildBalanceInfo(Player player) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("coinmarket.balance-info");
        if (sec == null) return null;

        ItemStack item = readItem(sec);
        if (item == null) return null;

        long bal = plugin.getCoinManager().get(player);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Lore içindeki %balance% değişimi
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                lore = lore.stream()
                        .map(s -> plugin.color(s.replace("%balance%", String.valueOf(bal))))
                        .toList();
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack readItem(ConfigurationSection sec) {
        if (sec == null) return null;

        String mat = sec.getString("material", "STONE");
        Material material;
        try { material = Material.valueOf(mat.toUpperCase()); }
        catch (Exception e) { material = Material.STONE; }

        int amount = Math.max(1, sec.getInt("amount", 1));
        ItemStack item = new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = sec.getString("name");
            if (name != null) meta.setDisplayName(plugin.color(name));

            List<String> lore = sec.getStringList("lore");
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(plugin::color).toList());
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
