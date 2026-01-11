package com.mrhistories.farmerpouch.pouch;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PouchItemService {

    private final NamespacedKey keyType;

    public PouchItemService(FarmerPouchPlugin plugin) {
        this.keyType = new NamespacedKey(plugin, "pouch_type");
    }

    public ItemStack createPouch(PouchRarity rarity, int amount) {
        ItemStack item = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = item.getItemMeta();

        String name = switch (rarity) {
            case POUCH_1 -> "§aFarmer Pouch I";
            case POUCH_2 -> "§eFarmer Pouch II";
            case POUCH_3 -> "§cFarmer Pouch III";
        };

        meta.setDisplayName(name);
        meta.setLore(List.of("§7Sağ tıkla aç.", "§8Type: " + rarity.name()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(keyType, PersistentDataType.STRING, rarity.name());

        item.setItemMeta(meta);
        return item;
    }

    public PouchRarity readRarity(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (!item.hasItemMeta()) return null;

        String raw = item.getItemMeta().getPersistentDataContainer().get(keyType, PersistentDataType.STRING);
        if (raw == null) return null;

        try {
            return PouchRarity.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
