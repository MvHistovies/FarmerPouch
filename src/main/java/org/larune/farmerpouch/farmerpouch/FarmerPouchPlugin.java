package com.mrhistories.farmerpouch;

import com.mrhistories.farmerpouch.animation.AnimationService;
import com.mrhistories.farmerpouch.coin.CoinManager;
import com.mrhistories.farmerpouch.coin.CoinMarketCommand;
import com.mrhistories.farmerpouch.coin.CoinMarketListener;
import com.mrhistories.farmerpouch.coin.CoinMarketService;
import com.mrhistories.farmerpouch.coin.TarimCoinCommand;
import com.mrhistories.farmerpouch.command.PouchCommand;
import com.mrhistories.farmerpouch.listener.OpeningProtectionListener;
import com.mrhistories.farmerpouch.listener.PouchOpenListener;
import com.mrhistories.farmerpouch.listener.WheatDropListener;
import com.mrhistories.farmerpouch.open.CooldownManager;
import com.mrhistories.farmerpouch.open.OpeningSessionManager;
import com.mrhistories.farmerpouch.pouch.PouchItemService;
import com.mrhistories.farmerpouch.reward.RewardService;
import com.mrhistories.farmerpouch.util.WorldGuardHook;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class FarmerPouchPlugin extends JavaPlugin {

    private PouchItemService pouchItemService;
    private WorldGuardHook worldGuardHook;

    private OpeningSessionManager openingSessionManager;
    private CooldownManager cooldownManager;

    private AnimationService animationService;

    // ADIM 4
    private CoinManager coinManager;
    private CoinMarketService coinMarketService;
    private RewardService rewardService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.pouchItemService = new PouchItemService(this);
        this.worldGuardHook = new WorldGuardHook();

        this.openingSessionManager = new OpeningSessionManager(this);
        this.cooldownManager = new CooldownManager(this);

        this.animationService = new AnimationService(this);

        // ADIM 4 servisleri
        this.coinManager = new CoinManager(this);
        this.coinMarketService = new CoinMarketService(this);
        this.rewardService = new RewardService(this);

        if (worldGuardHook.isAvailable()) {
            getLogger().info("WorldGuard bulundu (opsiyonel entegrasyon aktif).");
        } else {
            getLogger().info("WorldGuard bulunamadı (plugin WorldGuard olmadan da çalışır).");
        }

        // /pouch
        PouchCommand pouchCommand = new PouchCommand(this);
        getCommand("pouch").setExecutor(pouchCommand);
        getCommand("pouch").setTabCompleter(pouchCommand);

        // /coinmarket
        if (getCommand("coinmarket") != null) {
            getCommand("coinmarket").setExecutor(new CoinMarketCommand(this));
        }

        // /tarimcoin
        if (getCommand("tarimcoin") != null) {
            getCommand("tarimcoin").setExecutor(new TarimCoinCommand(this));
        }

        // Listenerlar
        getServer().getPluginManager().registerEvents(new WheatDropListener(this), this);
        getServer().getPluginManager().registerEvents(new PouchOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new OpeningProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CoinMarketListener(this), this);

        getLogger().info("FarmerPouch enabled.");
    }

    @Override
    public void onDisable() {
        if (openingSessionManager != null) openingSessionManager.endAll();
        if (coinManager != null) coinManager.save();
    }

    public PouchItemService getPouchItemService() {
        return pouchItemService;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    public OpeningSessionManager getOpeningSessionManager() {
        return openingSessionManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public AnimationService getAnimationService() {
        return animationService;
    }

    // ADIM 4 getterlar
    public CoinManager getCoinManager() {
        return coinManager;
    }

    public CoinMarketService getCoinMarketService() {
        return coinMarketService;
    }

    public RewardService getRewardService() {
        return rewardService;
    }

    public String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String msg(String path) {
        return color(getConfig().getString(path, ""));
    }

    public String prefix() {
        return msg("messages.prefix");
    }
}
