package com.mrhistories.farmerpouch.coin;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CoinMarketCommand implements CommandExecutor {

    private final FarmerPouchPlugin plugin;

    public CoinMarketCommand(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncu içindir.");
            return true;
        }

        player.openInventory(plugin.getCoinMarketService().buildMarket(player));
        return true;
    }
}
