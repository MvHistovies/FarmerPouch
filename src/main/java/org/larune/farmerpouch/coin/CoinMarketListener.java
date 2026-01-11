package com.mrhistories.farmerpouch.coin;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CoinMarketListener implements Listener {

    private final FarmerPouchPlugin plugin;

    public CoinMarketListener(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = plugin.color(plugin.getConfig().getString("coinmarket.title", "&aTarımCoin Market"));
        if (!e.getView().getTitle().equals(title)) return;

        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot < 0) return;

        String key = plugin.getCoinMarketService().getKeyBySlot(slot);
        if (key == null) return;

        long price = plugin.getCoinMarketService().getPrice(key);
        long balBefore = plugin.getCoinManager().get(player);

        if (balBefore < price) {
            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.coin.not_enough", "")
                            .replace("%balance%", String.valueOf(balBefore))
                            .replace("%price%", String.valueOf(price))
            ));
            return;
        }

        // Ödeme
        if (!plugin.getCoinManager().take(player, price)) {
            long balNow = plugin.getCoinManager().get(player);
            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.coin.not_enough", "")
                            .replace("%balance%", String.valueOf(balNow))
                            .replace("%price%", String.valueOf(price))
            ));
            return;
        }

        // Item ver
        ItemStack buy = plugin.getCoinMarketService().getBuyItem(key);
        if (buy != null) {
            var leftovers = player.getInventory().addItem(buy);
            leftovers.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
        }

        // Komut çalıştır
        for (String cmd : plugin.getCoinMarketService().getBuyCommands(key)) {
            if (cmd == null || cmd.isBlank()) continue;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }

        String displayName = (buy != null && buy.hasItemMeta() && buy.getItemMeta().hasDisplayName())
                ? buy.getItemMeta().getDisplayName()
                : key;

        long balAfter = plugin.getCoinManager().get(player);

        player.sendMessage(plugin.prefix() + plugin.color(
                plugin.getConfig().getString("messages.coin.purchased", "")
                        .replace("%name%", displayName)
                        .replace("%price%", String.valueOf(price))
                        .replace("%balance%", String.valueOf(balAfter))
        ));
    }
}
