package com.mrhistories.farmerpouch.coin;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TarimCoinCommand implements CommandExecutor {

    private final FarmerPouchPlugin plugin;

    public TarimCoinCommand(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncu içindir.");
            return true;
        }

        // /tarimcoin
        if (args.length == 0) {
            long bal = plugin.getCoinManager().get(player);
            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.coin.balance", "")
                            .replace("%amount%", String.valueOf(bal))
            ));
            return true;
        }

        // Admin alt komutlar
        if (!player.hasPermission("tarimcoin.admin")) {
            player.sendMessage(plugin.prefix() + plugin.msg("messages.no-permission"));
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            Long amount = parseLong(args[2]);
            if (amount == null || amount <= 0) return false;

            plugin.getCoinManager().add(target, amount);
            player.sendMessage(plugin.prefix() + "§a+ " + amount + " TarımCoin → §e" + target.getName());
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            Long amount = parseLong(args[2]);
            if (amount == null || amount < 0) return false;

            plugin.getCoinManager().set(target, amount);
            player.sendMessage(plugin.prefix() + "§a= " + amount + " TarımCoin → §e" + target.getName());
            return true;
        }

        player.sendMessage(plugin.prefix() + "§eKullanım: /tarimcoin | /tarimcoin add <oyuncu> <miktar> | /tarimcoin set <oyuncu> <miktar>");
        return true;
    }

    private Long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return null; }
    }
}
