package com.mrhistories.farmerpouch.command;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import com.mrhistories.farmerpouch.pouch.PouchRarity;
import com.mrhistories.farmerpouch.util.RegionMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PouchCommand implements CommandExecutor, TabCompleter {

    private final FarmerPouchPlugin plugin;

    public PouchCommand(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefix() + plugin.msg("messages.only-player"));
            return true;
        }

        if (!player.hasPermission("farmerpouch.admin")) {
            player.sendMessage(plugin.prefix() + plugin.msg("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        // YENİ: /pouch reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            if (plugin.getCoinManager() != null) plugin.getCoinManager().reload();
            player.sendMessage(plugin.prefix() + plugin.msg("messages.pouch.reload"));
            return true;
        }

        // /pouch lockmode <NONE|WORLDGUARD_REGION|WORLD_AND_AREA>
        if (args.length == 2 && args[0].equalsIgnoreCase("lockmode")) {
            RegionMode mode = RegionMode.fromConfig(args[1]);
            plugin.getConfig().set("region-lock.mode", mode.name());
            plugin.saveConfig();

            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.lock.set", "")
                            .replace("%mode%", mode.name())
            ));
            return true;
        }

        // /pouch region set <region>
        if (args.length == 3 && args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("set")) {
            if (plugin.getWorldGuardHook() == null || !plugin.getWorldGuardHook().isAvailable()) {
                player.sendMessage(plugin.prefix() + plugin.msg("messages.worldguard.missing"));
                return true;
            }

            String regionName = args[2];
            if (!plugin.getWorldGuardHook().hasRegion(player.getWorld(), regionName)) {
                player.sendMessage(plugin.prefix() + plugin.msg("messages.worldguard.region-not-found"));
                return true;
            }

            String worldName = player.getWorld().getName();
            plugin.getConfig().set("region-lock.worldguard.worlds." + worldName + ".region", regionName);
            plugin.saveConfig();

            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.region.set-success", "")
                            .replace("%region%", regionName)
            ));
            return true;
        }

        // /pouch region info
        if (args.length == 2 && args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("info")) {
            String worldName = player.getWorld().getName();
            String region = plugin.getConfig().getString("region-lock.worldguard.worlds." + worldName + ".region");

            if (region == null || region.isBlank()) {
                player.sendMessage(plugin.prefix() + plugin.msg("messages.region.not-set"));
                return true;
            }

            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.region.info", "")
                            .replace("%world%", worldName)
                            .replace("%region%", region)
            ));
            return true;
        }

        // /pouch area set <x1> <z1> <x2> <z2>
        if (args.length == 6 && args[0].equalsIgnoreCase("area") && args[1].equalsIgnoreCase("set")) {
            Integer x1 = parseInt(args[2]);
            Integer z1 = parseInt(args[3]);
            Integer x2 = parseInt(args[4]);
            Integer z2 = parseInt(args[5]);

            if (x1 == null || z1 == null || x2 == null || z2 == null) {
                sendHelp(player);
                return true;
            }

            String worldName = player.getWorld().getName();
            String base = "region-lock.area.worlds." + worldName + ".";
            plugin.getConfig().set(base + "x1", x1);
            plugin.getConfig().set(base + "z1", z1);
            plugin.getConfig().set(base + "x2", x2);
            plugin.getConfig().set(base + "z2", z2);
            plugin.saveConfig();

            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.area.set-success", "")
                            .replace("%x1%", String.valueOf(x1))
                            .replace("%z1%", String.valueOf(z1))
                            .replace("%x2%", String.valueOf(x2))
                            .replace("%z2%", String.valueOf(z2))
            ));
            return true;
        }

        // /pouch area info
        if (args.length == 2 && args[0].equalsIgnoreCase("area") && args[1].equalsIgnoreCase("info")) {
            String worldName = player.getWorld().getName();
            String base = "region-lock.area.worlds." + worldName + ".";

            if (!plugin.getConfig().contains(base + "x1")) {
                player.sendMessage(plugin.prefix() + plugin.msg("messages.area.not-set"));
                return true;
            }

            int x1 = plugin.getConfig().getInt(base + "x1");
            int z1 = plugin.getConfig().getInt(base + "z1");
            int x2 = plugin.getConfig().getInt(base + "x2");
            int z2 = plugin.getConfig().getInt(base + "z2");

            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.area.info", "")
                            .replace("%world%", worldName)
                            .replace("%x1%", String.valueOf(x1))
                            .replace("%z1%", String.valueOf(z1))
                            .replace("%x2%", String.valueOf(x2))
                            .replace("%z2%", String.valueOf(z2))
            ));
            return true;
        }

        // /pouch give <1|2|3> [amount]
        if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            PouchRarity rarity = PouchRarity.fromSimple(args[1]);
            if (rarity == null) {
                sendHelp(player);
                return true;
            }

            int amount = 1;
            if (args.length >= 3) {
                Integer parsed = parseInt(args[2]);
                if (parsed != null && parsed > 0) amount = parsed;
            }

            ItemStack pouch = plugin.getPouchItemService().createPouch(rarity, amount);
            player.getInventory().addItem(pouch);

            player.sendMessage(plugin.prefix() + plugin.color(
                    plugin.getConfig().getString("messages.pouch.given", "")
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%rarity%", rarity.name())
            ));
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        List<String> lines = plugin.getConfig().getStringList("messages.usage.main");
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines) {
                player.sendMessage(plugin.prefix() + plugin.color(line));
            }
        } else {
            player.sendMessage(plugin.prefix() + "§eKullanım: /pouch lockmode ...");
        }
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1) {
            return filter(Arrays.asList("region", "lockmode", "area", "give", "reload"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("region")) {
            return filter(Arrays.asList("set", "info"), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("area")) {
            return filter(Arrays.asList("set", "info"), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("lockmode")) {
            return filter(Arrays.asList("NONE", "WORLDGUARD_REGION", "WORLD_AND_AREA"), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(Arrays.asList("1", "2", "3"), args[1]);
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("area") && args[1].equalsIgnoreCase("set")) {
            return filter(Arrays.asList("-100", "0", "100", "200"), args[args.length - 1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filter(Arrays.asList("1", "2", "3", "5", "10", "64"), args[2]);
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String typed) {
        List<String> out = new ArrayList<>();
        String t = typed.toLowerCase();
        for (String s : options) {
            if (s.toLowerCase().startsWith(t)) out.add(s);
        }
        return out;
    }
}
