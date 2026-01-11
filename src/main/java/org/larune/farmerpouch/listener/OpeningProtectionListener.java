package com.mrhistories.farmerpouch.listener;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;

public class OpeningProtectionListener implements Listener {

    private final FarmerPouchPlugin plugin;

    public OpeningProtectionListener(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isOpening(Player p) {
        return plugin.getOpeningSessionManager().isOpening(p.getUniqueId());
    }

    private void warn(Player p) {
        p.sendMessage(plugin.prefix() + plugin.msg("messages.protection.blocked"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_commands", true)) return;
        if (!isOpening(e.getPlayer())) return;
        e.setCancelled(true);
        warn(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_teleport", true)) return;
        if (!isOpening(e.getPlayer())) return;
        e.setCancelled(true);
        warn(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_inventory_move", true)) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!isOpening(p)) return;
        e.setCancelled(true);
        warn(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInvDrag(InventoryDragEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_inventory_move", true)) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!isOpening(p)) return;
        e.setCancelled(true);
        warn(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeldChange(PlayerItemHeldEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_inventory_move", true)) return;
        if (!isOpening(e.getPlayer())) return;
        e.setCancelled(true);
        warn(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_inventory_move", true)) return;
        if (!isOpening(e.getPlayer())) return;
        e.setCancelled(true);
        warn(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_item_drop", true)) return;
        if (!isOpening(e.getPlayer())) return;
        e.setCancelled(true);
        warn(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_item_pickup", true)) return;
        if (!isOpening(e.getPlayer())) return;
        e.setCancelled(true);
        warn(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_damage", false)) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (!isOpening(p)) return;
        e.setCancelled(true);
    }

    // YENİ: saldırı engeli (kese açarken vuramasın)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_attack", true)) return;
        if (!(e.getDamager() instanceof Player damager)) return;
        if (!isOpening(damager)) return;
        e.setCancelled(true);
        warn(damager);
    }

    // YENİ: block break engeli (kese açarken block kıramasın)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!plugin.getConfig().getBoolean("opening.protect.block_block_break", true)) return;
        if (!isOpening(e.getPlayer())) return;
        e.setCancelled(true);
        warn(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getOpeningSessionManager().end(e.getPlayer());
    }
}
