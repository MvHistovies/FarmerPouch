package com.mrhistories.farmerpouch.reward;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import com.mrhistories.farmerpouch.pouch.PouchRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RewardService {

    private final FarmerPouchPlugin plugin;

    public RewardService(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean giveRandomReward(Player player, PouchRarity rarity) {
        String path = "rewards.pouches." + rarity.name();

        List<Map<?, ?>> rawList = plugin.getConfig().getMapList(path);

        if (rawList == null || rawList.isEmpty()) {
            // Config okunamadı / yanlış path / rarity uyuşmuyor
            plugin.getLogger().warning("[RewardService] Odul listesi bulunamadi veya bos: " + path);
            player.sendMessage(plugin.prefix() + "§cÖdül listesi bulunamadı. (Config kontrol edin: " + path + ")");
            return false;
        }

        RewardEntry chosen = pickWeighted(rawList);
        if (chosen == null) {
            plugin.getLogger().warning("[RewardService] Odul secilemedi (weight/toplam hatasi): " + path);
            player.sendMessage(plugin.prefix() + "§cÖdül seçilemedi. (Weight ayarlarını kontrol edin)");
            return false;
        }

        String type = chosen.type;

        if ("COIN".equals(type)) {
            long amount = Math.max(0L, chosen.amount);
            plugin.getCoinManager().add(player, amount);

            String msg = plugin.getConfig().getString("rewards.messages.received_coin", "&a+%amount% TarımCoin");
            player.sendMessage(plugin.prefix() + plugin.color(msg.replace("%amount%", String.valueOf(amount))));
            return true;
        }

        if ("COMMAND".equals(type)) {
            if (chosen.commands != null) {
                for (String c : chosen.commands) {
                    if (c == null || c.isBlank()) continue;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", player.getName()));
                }
            }
            String msg = plugin.getConfig().getString("rewards.messages.received_command", "&aÖdül verildi.");
            player.sendMessage(plugin.prefix() + plugin.color(msg));
            return true;
        }

        if ("ITEM".equals(type)) {
            ItemStack item = buildItem(chosen.item);
            if (item == null) {
                plugin.getLogger().warning("[RewardService] ITEM odulu olusturulamadi (item map bos): " + path);
                player.sendMessage(plugin.prefix() + "§cÖdül oluşturulamadı. (Config item alanını kontrol edin)");
                return false;
            }

            boolean dropIfFull = "DROP".equalsIgnoreCase(plugin.getConfig().getString("rewards.inventory_full_behavior", "DROP"));

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);

            String itemName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                    ? item.getItemMeta().getDisplayName()
                    : item.getType().name();

            if (!leftover.isEmpty()) {
                if (dropIfFull) {
                    leftover.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
                    String msgFull = plugin.getConfig().getString("rewards.messages.inventory_full",
                            "&cEnvanterin dolu! Ödül yere düşürüldü.");
                    player.sendMessage(plugin.prefix() + plugin.color(msgFull));

                    // Ayrıca hangi item olduğu bilgisi
                    String msgItem = plugin.getConfig().getString("rewards.messages.received_item", "&aÖdül: &e%item%");
                    player.sendMessage(plugin.prefix() + plugin.color(msgItem.replace("%item%", itemName)));
                    return true;
                } else {
                    // CANCEL modunda: burada en sağlıklısı ödülü iptal etmekti;
                    // fakat item zaten 'leftover' oldu. Biz yine güvenli şekilde drop yapıp uyarıyoruz.
                    leftover.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
                    String msgFull = plugin.getConfig().getString("rewards.messages.inventory_full",
                            "&cEnvanterin dolu! Ödül verilemedi.");
                    player.sendMessage(plugin.prefix() + plugin.color(msgFull));
                    return true;
                }
            }

            String msgItem = plugin.getConfig().getString("rewards.messages.received_item", "&aÖdül: &e%item%");
            player.sendMessage(plugin.prefix() + plugin.color(msgItem.replace("%item%", itemName)));
            return true;
        }

        plugin.getLogger().warning("[RewardService] Bilinmeyen odul tipi: " + type + " path=" + path);
        player.sendMessage(plugin.prefix() + "§cBilinmeyen ödül tipi: " + type);
        return false;
    }

    private RewardEntry pickWeighted(List<Map<?, ?>> rawList) {
        List<RewardEntry> entries = new ArrayList<>();
        int total = 0;

        for (Map<?, ?> m : rawList) {
            RewardEntry e = RewardEntry.fromMap(m);
            if (e == null) continue;
            if (e.weight <= 0) continue;
            total += e.weight;
            entries.add(e);
        }

        if (entries.isEmpty() || total <= 0) return null;

        int r = ThreadLocalRandom.current().nextInt(total);
        int cur = 0;
        for (RewardEntry e : entries) {
            cur += e.weight;
            if (r < cur) return e;
        }
        return entries.get(entries.size() - 1);
    }

    private ItemStack buildItem(Map<String, Object> itemMap) {
        if (itemMap == null || itemMap.isEmpty()) return null;

        String mat = String.valueOf(itemMap.getOrDefault("material", "STONE"));
        Material material;
        try {
            material = Material.valueOf(mat.toUpperCase());
        } catch (Exception e) {
            material = Material.STONE;
        }

        int amount = 1;
        try {
            amount = Math.max(1, Integer.parseInt(String.valueOf(itemMap.getOrDefault("amount", "1"))));
        } catch (Exception ignored) {}

        ItemStack item = new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Object name = itemMap.get("name");
            if (name != null) meta.setDisplayName(plugin.color(String.valueOf(name)));

            Object loreObj = itemMap.get("lore");
            if (loreObj instanceof List<?> loreList && !loreList.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (Object o : loreList) lore.add(plugin.color(String.valueOf(o)));
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private static class RewardEntry {
        String type; // ITEM / COMMAND / COIN
        int weight;

        long amount; // COIN
        List<String> commands = new ArrayList<>();
        Map<String, Object> item; // ITEM

        static RewardEntry fromMap(Map<?, ?> m) {
            if (m == null) return null;
            RewardEntry e = new RewardEntry();

            Object t = m.get("type");
            if (t == null) return null;
            e.type = String.valueOf(t).toUpperCase();

            e.weight = toInt(m.get("weight"), 0);
            e.amount = toLong(m.get("amount"), 0L);

            Object cmds = m.get("commands");
            if (cmds instanceof List<?> list) {
                for (Object o : list) e.commands.add(String.valueOf(o));
            }

            Object itemObj = m.get("item");
            if (itemObj instanceof Map<?, ?> map) {
                Map<String, Object> tmp = new HashMap<>();
                for (var ent : map.entrySet()) tmp.put(String.valueOf(ent.getKey()), ent.getValue());
                e.item = tmp;
            }

            return e;
        }

        static int toInt(Object o, int def) {
            try { return Integer.parseInt(String.valueOf(o)); } catch (Exception ex) { return def; }
        }

        static long toLong(Object o, long def) {
            try { return Long.parseLong(String.valueOf(o)); } catch (Exception ex) { return def; }
        }
    }
}
