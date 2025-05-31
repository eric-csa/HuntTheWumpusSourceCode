package Mobs;

import Effects.Effects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// hearts that when picked up give the player health.
public class Heart extends WumpusMob {
    private int healthGain = 0;
    private int stayDuration = 0;
    public Heart (int healthGain, int stayDuration) {
        super("Heart");
        this.healthGain = healthGain;
        this.stayDuration = stayDuration;
    }
    public void updateAI () {
        super.updateAI();
        if (aliveTicks > stayDuration) {
            bukkitMob.damage(1000);
        }
        Effects.spawnParticleRing(bukkitEntity.getLocation(), 20, 1.5, Particle.HEART, null);

        for (Entity entity : bukkitEntity.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof Player) {
                Player healedPlayer = (Player)entity;
                int addtionalHealth = 0;

                if (healedPlayer.getLocation().distance(bukkitEntity.getLocation()) <= 2) {
                    // Check for Health Boost potion effect
                    if (healedPlayer.hasPotionEffect(PotionEffectType.HEALTH_BOOST)) {
                        PotionEffect boostEffect = healedPlayer.getPotionEffect(PotionEffectType.HEALTH_BOOST);
                        if (boostEffect != null) {
                            int amplifier = boostEffect.getAmplifier(); // amplifier 0 = level 1
                            addtionalHealth = (amplifier + 1) * 4;  // Each level gives 4 extra health
                        }
                    }
                    healedPlayer.sendMessage(Component.text("You Gained " + healthGain + " Health").color(NamedTextColor.LIGHT_PURPLE));
                    healedPlayer.setHealth(Math.min(20 + addtionalHealth, healedPlayer.getHealth() + healthGain));
                    mobAI.cancel();
                    bukkitMob.damage(1000);
                }
            }
        }
    }
}
