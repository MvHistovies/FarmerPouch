package com.mrhistories.farmerpouch.animation;

import com.mrhistories.farmerpouch.FarmerPouchPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class AnimationService {

    private final FarmerPouchPlugin plugin;

    public AnimationService(FarmerPouchPlugin plugin) {
        this.plugin = plugin;
    }

    public void playPouchOpenAnimation(Player player, ItemStack displayItem, int durationTicks, Runnable onFinish) {
        if (!plugin.getConfig().getBoolean("animation.enabled", true)) {
            // Animasyon kapalıysa direkt bitir
            onFinish.run();
            return;
        }

        // Oyuncu offline ise çık
        if (player == null || !player.isOnline()) return;

        World world = player.getWorld();

        // Başlangıç noktası (oyuncunun önü)
        Location base = calcFrontLocation(player,
                plugin.getConfig().getDouble("animation.forward_distance", 0.9));

        // ItemDisplay spawn
        ItemDisplay display = world.spawn(base, ItemDisplay.class, d -> {
            d.setItemStack(displayItem.clone());
            d.setBillboard(Display.Billboard.FIXED);
            d.setShadowStrength(0.0f);
            d.setInterpolationDuration(1);
            d.setInterpolationDelay(0);
        });

        double spinPerTick = plugin.getConfig().getDouble("animation.spin_degrees_per_tick", 18.0);
        double lift = plugin.getConfig().getDouble("animation.lift_height", 0.9);

        new BukkitRunnable() {
            int tick = 0;
            float yaw = player.getLocation().getYaw();

            @Override
            public void run() {
                // Bitirme koşulları
                if (!player.isOnline() || display.isDead() || tick >= Math.max(1, durationTicks)) {
                    cleanup(display);
                    finalEffect(player);
                    safeRun(onFinish);
                    cancel();
                    return;
                }

                // Oyuncu hareket ederse animasyon da önünde kalsın
                Location front = calcFrontLocation(player,
                        plugin.getConfig().getDouble("animation.forward_distance", 0.9));

                // Yükselme: 0..duration arası sin eğrisi (daha doğal)
                double progress = (double) tick / (double) Math.max(1, durationTicks);
                double yOffset = Math.sin(progress * Math.PI) * lift;

                Location target = front.clone().add(0, yOffset, 0);

                // Dönüş
                yaw += (float) spinPerTick;
                display.teleport(target);
                display.setRotation(yaw, 0f);

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Location calcFrontLocation(Player player, double forwardDistance) {
        Location eye = player.getEyeLocation().clone();
        Vector dir = eye.getDirection().normalize().multiply(forwardDistance);
        Location loc = eye.add(dir);
        // biraz aşağı: göz hizası yerine göğüs hizası
        loc.subtract(0, 0.35, 0);
        return loc;
    }

    private void finalEffect(Player player) {
        try {
            Location loc = player.getLocation().add(0, 1.0, 0);

            String particleName = plugin.getConfig().getString("animation.final.particle", "EXPLOSION_LARGE");
            int count = plugin.getConfig().getInt("animation.final.particle_count", 1);

            Particle particle;
            try {
                particle = Particle.valueOf(particleName.toUpperCase());
            } catch (Exception ignored) {
                particle = Particle.EXPLOSION_LARGE;
            }

            player.getWorld().spawnParticle(particle, loc, Math.max(1, count));

            String soundName = plugin.getConfig().getString("animation.final.sound", "ENTITY_GENERIC_EXPLODE");
            float vol = (float) plugin.getConfig().getDouble("animation.final.sound_volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("animation.final.sound_pitch", 1.0);

            Sound sound;
            try {
                sound = Sound.valueOf(soundName.toUpperCase());
            } catch (Exception ignored) {
                sound = Sound.ENTITY_GENERIC_EXPLODE;
            }

            player.playSound(loc, sound, vol, pitch);
        } catch (Exception ignored) {
            // efekt fail olsa bile akış bozulmasın
        }
    }

    private void cleanup(ItemDisplay display) {
        try {
            if (display != null && !display.isDead()) display.remove();
        } catch (Exception ignored) {}
    }

    private void safeRun(Runnable r) {
        try {
            if (r != null) r.run();
        } catch (Exception ignored) {}
    }
}
