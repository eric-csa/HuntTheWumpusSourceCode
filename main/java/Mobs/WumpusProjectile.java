package Mobs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WumpusProjectile extends WumpusMob {
    private Vector projectileSpeed;
    private Location target;
    private int projectileSpeedScale;
    private int damage;

    public WumpusProjectile (String projectile) {
        super(projectile);
    }

    public void updateAI () {
        super.updateAI();
        if (aliveTicks > 60) {
            bukkitMob.damage(1000);
        }
        bukkitMob.lookAt(target);
        projectileSpeed = new Vector(target.getX() - bukkitEntity.getX(), 0, target.getZ() - bukkitEntity.getZ()).normalize().multiply(0.5);

        bukkitEntity.setVelocity(projectileSpeed);
        Bukkit.getWorld("world").spawnParticle(Particle.ELECTRIC_SPARK, bukkitEntity.getLocation(), 45); // projectile type
        for (Entity entity : bukkitEntity.getNearbyEntities(1, 1, 1)) { // check for players in hit-range
            if (entity instanceof Player) {
                Player hitPlayer = (Player)entity;

                hitPlayer.damage(5);
                hitPlayer.sendMessage(Component.text("You Took " + 5 + " Damage").color(NamedTextColor.DARK_GREEN));
                bukkitMob.damage(1000);
            }
        }
    }
}
