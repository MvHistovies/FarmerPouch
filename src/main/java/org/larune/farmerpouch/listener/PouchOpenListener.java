package com.mrhistories.farmerpouch.listener;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import com.mrhistories.farmerpouch.pouch.PouchRarity;
import com.mrhistories.farmerpouch.util.RegionCheckUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PouchOpenListener implements Listener {

    private final FarmerPouchPlugin plugin;

    public PouchOpenListener(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        PouchRarity rarity = plugin.getPouchItemService().readRarity(item);
        if (rarity == null) return;

        if (!RegionCheckUtil.canUseSystemHere(plugin, player)) {
            player.sendMessage(plugin.prefix() + plugin.msg("messages.outside"));
            e.setCancelled(true);
            return;
        }

        if (!plugin.getOpeningSessionManager().start(player)) {
            player.sendMessage(plugin.prefix() + plugin.msg("messages.opening.already_opening"));
            e.setCancelled(true);
            return;
        }

        if (plugin.getCooldownManager().isEnabled()) {
            long rem = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), rarity);
            if (rem > 0) {
                plugin.getOpeningSessionManager().end(player);
                player.sendMessage(plugin.prefix() + plugin.color(
                        plugin.getConfig().getString("messages.cooldown.wait", "")
                                .replace("%seconds%", String.valueOf(rem))
                ));
                e.setCancelled(true);
                return;
            }
        }

        e.setCancelled(true);
        player.sendMessage(plugin.prefix() + plugin.msg("messages.opening.started"));

        int duration = Math.max(1, plugin.getConfig().getInt("opening.duration_ticks", 40));

        ItemStack displayItem = item.clone();
        displayItem.setAmount(1);

        plugin.getAnimationService().playPouchOpenAnimation(player, displayItem, duration, () -> {
            try {
                if (!player.isOnline()) {
                    plugin.getOpeningSessionManager().end(player.getUniqueId());
                    return;
                }

                ItemStack current = player.getInventory().getItemInMainHand();
                PouchRarity currentRarity = plugin.getPouchItemService().readRarity(current);

                if (currentRarity != rarity) {
                    player.sendMessage(plugin.prefix() + plugin.msg("messages.opening.cancelled"));
                    plugin.getOpeningSessionManager().end(player);
                    return;
                }

                // 1 adet eksilt
                int amt = current.getAmount();
                if (amt <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    current.setAmount(amt - 1);
                    player.getInventory().setItemInMainHand(current);
                }

                // Cooldown uygula
                plugin.getCooldownManager().apply(player.getUniqueId(), rarity);

                // Ödül ver (başarısızsa logla)
                boolean ok = plugin.getRewardService().giveRandomReward(player, rarity);
                if (!ok) {
                    plugin.getLogger().warning("[PouchOpenListener] Odul verilemedi: " + rarity.name() + " player=" + player.getName());
                }

            } catch (Exception ex) {
                plugin.getLogger().severe("[PouchOpenListener] Hata: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                plugin.getOpeningSessionManager().end(player);
            }
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getOpeningSessionManager().end(player);
            }
        }, duration + 5L);
    }
}
