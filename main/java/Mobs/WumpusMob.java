package Mobs;

import ServerData.ServerData;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

// default class called wumpusMob for all huntTheWumpus Mobs. The class name is not mob, because that is a built-in interface used by minecraft.
// all mobs use mythic mobs
public class WumpusMob implements Listener {
    protected MythicMob mob;
    protected String mobName;
    protected Entity bukkitEntity;
    protected Mob bukkitMob;
    protected LivingEntity livingEntity;
    protected ActiveMob activeMob;
    protected BukkitTask mobAI;
    protected Player target;
    protected int aliveTicks = 0;
    protected int attacksUsed = 0;
    protected String currentAttack;
    protected boolean foundTarget = false;
    public int onKillHealthGain;
    protected String nameTagDisplay;
    protected final String nameDisplayColor = "§c";
    protected final String maxHealthDisplayColor = "§a";
    protected final String healthyDisplayColor = "§a";
    protected final String lowerHealthDisplayColor = "§e";
    protected final String veryLowHealthDisplayColor = "§c";
    protected final String heart = "§c❤";
    protected int maxHealth = 1;
    protected int healthPercentage = 0;
    // constructor only constructs the mob type
    public WumpusMob (String mob) {
        mobName = mob;
        this.mob = MythicBukkit.inst().getMobManager().getMythicMob(mob).orElse(null);
        if (this.mob == null) {
            HuntTheWumpusPlugin.getPlugin().getLogger().info("Invalid Mob: " + mob + " does not exist");
        }
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
    }
    public WumpusMob (String mob, int onKillHealthGain) {
        mobName = mob;
        this.mob = MythicBukkit.inst().getMobManager().getMythicMob(mob).orElse(null);
        if (this.mob == null) {
            HuntTheWumpusPlugin.getPlugin().getLogger().info("Invalid Mob: " + mob + " does not exist");
        }
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());

        this.onKillHealthGain = onKillHealthGain;
    }
    // spawns the mob, sets the active mob to spawn the mob
    public void updateDisplay () {
        if (this instanceof Wumpus) {
            return;
        }
        if (livingEntity == null) {
            return;
        }
        healthPercentage = (int)(livingEntity.getHealth() * 100 / maxHealth);
        if (healthPercentage <= 20) {
            nameTagDisplay = nameDisplayColor + mobName + " " + veryLowHealthDisplayColor + (int)livingEntity.getHealth() + "§7/" + maxHealthDisplayColor + maxHealth + heart;
        }
        else if (healthPercentage <= 50) {
            nameTagDisplay = nameDisplayColor + mobName + " " + lowerHealthDisplayColor + (int)livingEntity.getHealth() + "§7/" + maxHealthDisplayColor + maxHealth + heart;
        }
        else nameTagDisplay = nameDisplayColor + mobName + " " + healthyDisplayColor + (int)livingEntity.getHealth() + "§7/" + maxHealthDisplayColor + maxHealth + heart;

        bukkitEntity.setCustomName(nameTagDisplay); // using old method for multiple color display
        bukkitEntity.setCustomNameVisible(true);

    }
    public void spawn (Location location) {
        activeMob = mob.spawn(BukkitAdapter.adapt(location), 1);
        bukkitEntity = activeMob.getEntity().getBukkitEntity();
        bukkitMob = (Mob) bukkitEntity;
        livingEntity = (LivingEntity) bukkitEntity;
        mobAI =  new BukkitRunnable() {
            @Override
            public void run () {
                updateAI();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }
    public void removeAllModifiers (Attribute attribute) {
        AttributeInstance attributeInstance = livingEntity.getAttribute(attribute);

        if (attributeInstance == null) {
            throw new IllegalArgumentException("Invalid Attribute Removal: " + mobName + " does not have attribute " + attribute.getTranslationKey());
        }

        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
            attributeInstance.removeModifier(modifier);
        }
    }
    public Entity getBukkitEntity () {
        return bukkitEntity;
    }
    public void updateAI () {
        updateDisplay();
        aliveTicks++;
        target = (Player) bukkitMob.getTarget();
        if (target != null) {
            foundTarget = true;
        }
    }
    // this function sends a signal to the mob. All special attacks made in mythicmobs will be executed on a signal by the plugin. CASE SENSITIVE
    public void useMythicAttack (String attack) {
        activeMob.signalMob(null, attack);
    }
    public void death () {

    }
    @EventHandler
    public void onMobDeath (EntityDeathEvent event) {
        if (event.getEntity() == target) {
            mobAI.cancel();
            HandlerList.unregisterAll(this);
        }
        if (event.getEntity() == bukkitEntity) {
            if (this instanceof Wumpus) {
                if (target != null) {
                    ServerData.getPlayerData(target).getGame().playerWin();
                }
            }
            if (event.getEntity().getKiller() != null) {
                event.getEntity().getKiller().setHealth(Math.min(20, event.getEntity().getKiller().getHealth() + onKillHealthGain));
            }
            death();
            System.out.println("A + " + mobName + " has died");
            mobAI.cancel();
            HandlerList.unregisterAll(this);
        }
        //HandlerList.unregisterAll(this);
    }
    @EventHandler
    public void onMobHit (EntityDamageByEntityEvent event) {
        if (this instanceof Capybara && event.getEntity() == bukkitEntity && event.getDamager() == target) { // capybara takes extra knockback
            Vector kbVector = target.getLocation().getDirection().multiply(3);
            kbVector.setY(2);
            bukkitEntity.setVelocity(kbVector);
        }
        if (this instanceof AgileWumpusZombie && event.getEntity() == bukkitEntity && event.getDamager() == target) { // agile wumpus zombie takes extra knockback
            Vector kbVector = target.getLocation().getDirection().multiply(1.5);
            kbVector.setY(0.5);
            bukkitEntity.setVelocity(kbVector);
        }
    }
}
