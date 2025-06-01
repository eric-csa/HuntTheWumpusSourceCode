package Mobs;

import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import static Caves.CaveManager.world;

public class Capybara extends WumpusMob {
    private final double maxDamageGain = 4;
    private final double maxSpeedGain = 0.5;
    private double damage;
    private double speed;
    private int particleAmount;
    public Capybara () {
        super("Capybara");

        damage = 0;
        speed = 0.25;
        maxHealth = 40;
    }
    public void updateAI () { // Capybara Mechanic: The capbyara gains damage and speed the lower its health becomes.
        super.updateAI();

        if (aliveTicks < 20) {
            return;
        }

        if (aliveTicks % 20 == 0) {
            removeAllModifiers(Attribute.GENERIC_ATTACK_DAMAGE);
            removeAllModifiers(Attribute.GENERIC_MOVEMENT_SPEED);

            damage = ((100 - healthPercentage) * maxDamageGain) / 100.0;
            speed = ((100 - healthPercentage) * maxSpeedGain) / 100.0;
            particleAmount = (int)((100 - healthPercentage) * 0.4);

            livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(new AttributeModifier("capybaraDamage", damage, AttributeModifier.Operation.ADD_NUMBER));
            livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier("capybaraSpeed", speed, AttributeModifier.Operation.ADD_NUMBER));
        }

        world.spawnParticle(Particle.CRIT, livingEntity.getLocation(), particleAmount);
    }
}
