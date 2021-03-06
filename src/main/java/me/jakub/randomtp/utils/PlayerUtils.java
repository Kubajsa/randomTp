package me.jakub.randomtp.utils;

import me.jakub.randomtp.Randomtp;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerUtils {

    static Randomtp plugin;

    public PlayerUtils(Randomtp plugin) {
        this.plugin = plugin;
    }

    public static void addInvincibility(Player player, int sec, int amp) {
        PotionEffect potionEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, sec * 20, amp);
        player.addPotionEffect(potionEffect);
    }

    public static void playSound(Player player) {
        try {
            String var1 = plugin.getConfig().getString("Sounds.sound");
            player.playSound(player.getLocation(), Sound.valueOf(var1), 1, 1);
        } catch (Exception e) {
            Log.log(Log.LogLevel.ERROR, "Couldn't play sound, wrong sound name was used in the config");
        }
    }

    public static void spawnParticle(Player player) {
        String var100 = plugin.getConfig().getString("Particles.particle");
        int var10 = plugin.getConfig().getInt("Particles.count");
        try {
            player.getWorld().spawnParticle(Particle.valueOf(var100), player.getLocation(), var10);
        } catch (Exception e) {
            Log.log(Log.LogLevel.ERROR, "Couldn't spawn particle, wrong particle name was used in the config");
        }
    }
}
