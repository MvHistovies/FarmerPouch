package com.mrhistories.farmerpouch.listener;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import com.mrhistories.farmerpouch.pouch.PouchRarity;
import com.mrhistories.farmerpouch.util.RegionCheckUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class WheatDropListener implements Listener {

    private final FarmerPouchPlugin plugin;

    // YENİ: buğday kırma sayacı (belli aralıkla coin için)
    private final Map<UUID, Integer> wheatCounter = new ConcurrentHashMap<>();

    public WheatDropListener(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!plugin.getConfig().getBoolean("drop.enabled", true)) return;

        Player player = e.getPlayer();
        Block block = e.getBlock();

        // Bölge/alan kısıtlaması
        if (!RegionCheckUtil.canUseSystemHere(plugin, player)) return;

        // Sadece buğday
        if (block.getType() != Material.WHEAT) return;

        // Sadece tam büyümüş
        if (plugin.getConfig().getBoolean("drop.only_fully_grown_wheat", true)) {
            if (!(block.getBlockData() instanceof Ageable ageable)) return;
            if (ageable.getAge() < ageable.getMaximumAge()) return;
        }

        // 1) YENİ: Belli aralıklarla coin ver
        handleWheatCoin(player);

        // 2) Kese düşürme (eski sistem)
        double c1 = clamp01(plugin.getConfig().getDouble("drop.chances.POUCH_1", 0.0));
        double c2 = clamp01(plugin.getConfig().getDouble("drop.chances.POUCH_2", 0.0));
        double c3 = clamp01(plugin.getConfig().getDouble("drop.chances.POUCH_3", 0.0));

        PouchRarity drop = rollOne(c1, c2, c3);
        if (drop == null) return;

        ItemStack pouch = plugin.getPouchItemService().createPouch(drop, 1);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), pouch);
    }

    private void handleWheatCoin(Player player) {
        if (!plugin.getConfig().getBoolean("wheat-coin.enabled", true)) return;

        int every = Math.max(1, plugin.getConfig().getInt("wheat-coin.every_breaks", 20));
        int amount = Math.max(0, plugin.getConfig().getInt("wheat-coin.coin_amount", 1));

        UUID uuid = player.getUniqueId();
        int c = wheatCounter.getOrDefault(uuid, 0) + 1;

        if (c >= every) {
            wheatCounter.put(uuid, 0);

            if (amount > 0) {
                plugin.getCoinManager().add(player, amount);

                String msg = plugin.getConfig().getString("wheat-coin.message", "");
                if (msg != null && !msg.isBlank()) {
                    player.sendMessage(plugin.prefix() + plugin.color(
                            msg.replace("%amount%", String.valueOf(amount))
                    ));
                }
            }
        } else {
            wheatCounter.put(uuid, c);
        }
    }

    private PouchRarity rollOne(double chance1, double chance2, double chance3) {
        // Öncelik: 3 > 2 > 1
        if (roll01(chance3)) return PouchRarity.POUCH_3;
        if (roll01(chance2)) return PouchRarity.POUCH_2;
        if (roll01(chance1)) return PouchRarity.POUCH_1;
        return null;
    }

    private boolean roll01(double chance01) {
        if (chance01 <= 0.0) return false;
        double r = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
        return r < chance01;
    }

    private double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
