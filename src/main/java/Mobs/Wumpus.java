package Mobs;

import Caves.Cave;
import Effects.Effects;
import Titles.TitleSender;
import Random.Random;
import Rooms.Room;
import Rooms.RoomStorage;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static Caves.CaveManager.world;

public class Wumpus extends WumpusBoss implements Listener {
    private int timeInPhase = 0;
    private int downtime = 0;
    private boolean isConsuming = false;
    private boolean isEnraged = false;
    private Location arenaCenter = RoomStorage.getWumpusRoom().getSpawnLocation();
    private final double leapYAmount = 8;

    private boolean canDamage = true; // this boolean is used to prevent double effects
    private int explodeDamage = 6;
    private int leapDamage = 8;
    private int ramDamage = 4;
    private int shockwaveDamage = 5;

    private double ramSpeed = 1.1;
    private double explodeChance;
    private int leapWaitTime = 30;

    private final int phase2 = 60;
    private final int phase3 = 20;

    private int bombCooldown = 150;
    private int extraBombs;
    private Room wumpusArena;

    private final int bossIntroDuration = 440;
    private final int bossThemePhase1Duration = 900;
    private final int bossThemePhase2InterludeDuration = 440;
    private final int bossThemePhase2Duration = 900;
    private final int bossThemePhase3Duration = 1400;
    private int currentThemeDuration = 0;
    private int lastSoundPlay = -1;
    public Wumpus () {
        super("Wumpus");
        phase = 1;
        explodeChance = 0.5;
        wumpusArena = RoomStorage.getWumpusRoom();
        //Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
       // arenaCenter = new Location(Bukkit.getWorld("world"), );
    }
    public Wumpus (Cave cave) {
        super("Wumpus");
        phase = 1;
        explodeChance = 0.5;
        wumpusArena = new Room(
                cave.getRoomLocationInCave(RoomStorage.getWumpusRoom(), RoomStorage.getWumpusRoom().getStart()),
                        cave.getRoomLocationInCave(RoomStorage.getWumpusRoom(), RoomStorage.getWumpusRoom().getEnd()),
                cave.getRoomLocationInCave(RoomStorage.getWumpusRoom(), RoomStorage.getWumpusRoom().getSpawnLocation()));
        //Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
        arenaCenter = wumpusArena.getSpawnLocation();
    }
    public void updateAI () {
        super.updateAI();
        timeInPhase++;
        if (aliveTicks % 600 == 0) {
            target.sendMessage(Component.text("A heart has spawned!").color(NamedTextColor.DARK_PURPLE));
            new Heart(6, 300).spawn(Random.randomLocation(wumpusArena.getStart(), wumpusArena.getEnd(), wumpusArena.getSpawnY()));
        }
        if (aliveTicks % 200 == 0) { // bomb attack
            Location bombLocation = Random.randomLocation( // selecting a slight displacement from the target's location to make bomb placement less predictable
                    (int)(Math.max(target.getX() - 5, wumpusArena.getStartX() + 10)),
                    (int)(Math.min(target.getX() + 5, wumpusArena.getEndX() - 10)),
                    (int)(wumpusArena.getSpawnY()),
                    (int)(wumpusArena.getSpawnY()),
                    (int)(Math.max(target.getZ() - 5, wumpusArena.getStartZ() + 10)),
                    (int)(Math.min(target.getZ() + 5, wumpusArena.getEndZ() - 10)));
            bombLocation.setY(RoomStorage.getWumpusRoom().getSpawnY() - 10);
            bomb(bombLocation, 5);

            for (int i = 0; i < extraBombs; i++) {
                bomb(Random.randomLocation(wumpusArena.getStart(), wumpusArena.getEnd(), wumpusArena.getSpawnY() - 10), 5);
            }
        }
        if (aliveTicks > 40 && !foundTarget) { //
            livingEntity.damage(400);
            mobAI.cancel();
            System.out.println("invalid boss: no target");
            new Wumpus().spawn(RoomStorage.getWumpusRoom().getSpawnLocation());
            return;
        }
        if (aliveTicks == 41) {
            target.playSound(target, "bossintrocutscene", SoundCategory.MASTER, 1.0f, 1.0f);
            target.sendMessage("Attempting to play boss introduction music");
            lastSoundPlay = aliveTicks;
            currentThemeDuration = bossIntroDuration;
        }
        if (isEnraged) {
            Bukkit.getWorld("world").spawnParticle(Particle.FLAME, bukkitEntity.getLocation(), 5);
        }
        if (downtime > 0) {
            if (isEnraged) {
                downtime = Math.min(downtime, 30);
            }
            doDowntime();
            downtime--;
            return;
        }

        if (currentAttack != null && currentAttack.equals("Ram")) {
            Bukkit.getWorld("world").spawnParticle(Particle.SONIC_BOOM, bukkitEntity.getLocation(), 10);
        }
        if (isConsuming && aliveTicks % 10 == 0) {
            for (Entity entity : bukkitEntity.getNearbyEntities(15, 15, 15)) {
                if (entity instanceof Player) {
                    Player consumedPlayer = (Player)entity;

                    TitleSender.sendTitle("YOU ARE BEING CONSUMED", "", consumedPlayer, 100, 300, 100,
                            NamedTextColor.RED, NamedTextColor.BLUE);
                }
            }
        }
        if (livingEntity.getHealth() < 11 && phase < 2) {
            phase = 2;
            updatePhase(phase);
        }
        if (livingEntity.getHealth() < 4 && phase < 3) {
            phase = 3;
            updatePhase(phase);
        }
        if (aliveTicks > 40) {
            selectAttack();
        }
    }
    public void selectAttack () {
        if (currentAttack != null || isConsuming || this.target == null) {
            return;
        }
        attacksUsed++;
        if (attacksUsed % 10 == 0 && phase == 3) {
            enrage();
            return;
        }
        if (attacksUsed % 8 == 0 && !isEnraged) {
            consume();
            return;
        }
        Location leapLocation = target.getLocation();
        leapLocation.setY(wumpusArena.getSpawnY());
        if (bukkitEntity.getLocation().distance(target.getLocation()) > 20) {
            leap(leapLocation);
            return;
        }
        if (bukkitEntity.getLocation().distance(target.getLocation()) > 10 && Random.randomRoll(0.5)) {
            leap(leapLocation);
            return;
        }
        if (Random.randomRoll(0.3)) {
            leap(leapLocation);
            return;
        }
        ram();
    }
    public int getTimeInPhase () {
        return timeInPhase;
    }
    public int getPhase () {
        return phase;
    }
    public void updatePhase (int phase) {
        this.phase = phase;
        target.sendMessage(Component.text("New Phase: " + phase).color(NamedTextColor.DARK_BLUE));
        timeInPhase = 0;
        if (phase == 2) {
            extraBombs = 1;
            ramDamage = 5;
            leapDamage = 10;
            ramSpeed = 1.2;
            explodeChance = 0.75;

            lastSoundPlay = -1;
            return;
        }
        extraBombs = 2;
        ramDamage = 6;
        shockwaveDamage = 6;
        explodeChance = 1;
        ramSpeed = 1.3;
        attacksUsed = 8; // prepare for enrage

        lastSoundPlay = aliveTicks;
        currentThemeDuration = bossThemePhase2InterludeDuration;
       // target.sendMessage("attempting to play phase2interlude theme");
       // target.stopAllSounds();
       // target.playSound(target, "phase2interlude", SoundCategory.MASTER, 1.0f, 1.0f);
    }
    public void spawn (Location location) {
        super.spawn(location);
    }
    public void doDowntime () {
        world.spawnParticle(Particle.GLOW, bukkitEntity.getLocation(), 5);
        bukkitEntity.setVelocity(new Vector(0, 0, 0));
    }
    public void bomb (Location location, int delay) {
        if (delay <= 0) {
            return;
        }
        if (delay == 2) {
            new Bomb().spawn(new Location(Bukkit.getWorld("world"), location.getX(), location.getY() + 20, location.getZ()));
        }
        Effects.spawnParticleRing(location, 50, 2, Particle.REDSTONE, Effects.black);
        Effects.spawnParticleRing(location, 200, 4, Particle.REDSTONE, Effects.red);
        Effects.spawnParticleRing(location, 450, 6, Particle.REDSTONE, Effects.orange);
        Effects.spawnParticleRing(location, 1250,10, Particle.REDSTONE, Effects.yellow);
        new BukkitRunnable() {
            @Override
            public void run () {
                bomb(location, delay - 1);
            }
        }.runTaskLater(HuntTheWumpusPlugin.getPlugin(), 20);
    }
    public void leap (Location location) {
        currentAttack = "leap";
        useMythicAttack("Leap"); // the mythicMobs attack leap
        Location leapVertexLoc = new Location(Bukkit.getWorld("world"), (location.getX() + bukkitEntity.getX()) / 2,
                leapYAmount + bukkitEntity.getY(), (location.getZ() + bukkitEntity.getZ()) / 2);
        leapTo(bukkitEntity.getLocation(), leapVertexLoc, location);
    }
    public void leapTo (Location start, Location vertex, Location end) {
        new BukkitRunnable() {
            double velocity = 3;
            double velocityChange = 0.97;
            private Vector moveVector = new Vector(vertex.getX() - start.getX(), vertex.getY() - start.getY(), vertex.getZ() - start.getZ());
            boolean isInitalized = false;
            double moveDistance = moveVector.length();
            double distanceTraveled = 0;
            boolean isLanding = false;
            private double radius = 7;
            int waitTime = leapWaitTime; // wait time in minecraft ticks before the attack executes
            int ticks = 0;
            @Override
            public void run () {
                if (!isInitalized) {
                    initialize();
                }
                ticks++;
                if (ticks < waitTime) {
                    return;
                }
                Bukkit.getWorld("world").spawnParticle(Particle.EXPLOSION_NORMAL, bukkitEntity.getLocation(), 10);
                bukkitEntity.setVelocity(moveVector.normalize().multiply(velocity));
                distanceTraveled += velocity;
                if (distanceTraveled >= moveDistance) {
                    if (isLanding) {
                        for (Entity entity : bukkitEntity.getNearbyEntities(3.5, 3.5, 3.5)) {
                            if (bukkitEntity.getLocation().distance(entity.getLocation()) > radius) {
                                continue;
                            }
                            if (entity instanceof Player hitPlayer) {
                                hitPlayer.sendMessage(Component.text("You Took " + leapDamage + " Damage").color(NamedTextColor.DARK_GREEN));
                                hitPlayer.damage(leapDamage);
                            }
                        }
                    }
                    if (end != null) {
                        leapTo(bukkitEntity.getLocation(), end, null);
                    }
                    if (end == null) {
                        if (phase > 1) {
                            shockwave(vertex);
                        }
                        if (phase > 2) {
                            spark(vertex, 5);
                        }
                        endAttack(70);
                    }
                    this.cancel();
                }
            }
            public void initialize() {
                isInitalized = true;
                velocity = 3;
                velocityChange = 0.97;
                if (moveVector.getY() < 0) {
                    velocity = 1.6;
                    velocityChange = 1.03;
                    isLanding = true;
                    waitTime = 0;
                }
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }
    // ultimate attack where the wumpus consumes the player.
    public void consume () {
        isConsuming = true;
        bukkitEntity.teleport(arenaCenter);
        useMythicAttack("Consume"); // parts of attacks, mainly animations, will be made in mythicMobs.
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run () {
                ticks++;
                if (ticks == 1) {
                    world.playSound(target, Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.MASTER, 1, 1.0f);
                }
                Vector pullVec = new Vector(bukkitEntity.getX() - target.getX(), bukkitEntity.getY() - target.getY(), bukkitEntity.getZ() - target.getZ());

                if (ticks > 10) {
                    endConsume();
                    this.cancel();
                }
                pullVec.normalize().multiply(5);
                for (Entity entity : bukkitEntity.getNearbyEntities(15, 15, 15)) {
                    if (entity instanceof Player) {
                        Player pulledPlayer = (Player)entity;
                        if (pulledPlayer.getLocation().distance(bukkitEntity.getLocation()) > 20) {
                            continue;
                        }
                        pulledPlayer.setVelocity(pullVec);
                    }
                }
                //target.setVelocity(pullVec);
                for (Entity entity : bukkitEntity.getNearbyEntities(9, 9, 9)) {
                    if (entity instanceof Player consumedPlayer) {

                        consumedPlayer.sendMessage("you have been consumed");
                        consumedPlayer.performCommand("kill");
                        endConsume();
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 100, 1);
    }
    public void endConsume () {
        downtime = 100;
        isConsuming = false;
    }
    public void death () {
        super.death();
        HandlerList.unregisterAll(this);
        if (target != null) {
            target.stopAllSounds();
        }
    }
    public void explode () {
        useMythicAttack("WumpusExplode");

        new BukkitRunnable() {
            @Override
            public void run () {
                for (Entity entity : bukkitEntity.getNearbyEntities(3, 3, 3)) {
                    if (entity instanceof Player) {
                        Player hitPlayer = (Player)entity;

                        Vector knockback = hitPlayer.getLocation().getDirection().multiply(-4);
                        knockback.setY(0.4);
                        hitPlayer.setVelocity(knockback);
                        hitPlayer.damage(explodeDamage);
                        hitPlayer.sendMessage(Component.text("You Took " + explodeDamage + " Damage").color(NamedTextColor.DARK_GREEN));
                        this.cancel();
                    }
                }
                downtime = 70;
            }
        }.runTaskLater(HuntTheWumpusPlugin.getPlugin(), 20);
    }
    public void swipe () {

    }
    public void enrage () {
        isEnraged = true;
        bombCooldown /= 2;

        new BukkitRunnable() {
            @Override
            public void run () {
                isEnraged = false;
                bombCooldown *= 2;
                attacksUsed = 1;
            }
        }.runTaskLater(HuntTheWumpusPlugin.getPlugin(), 300);
    }
    public void shockwave (Location center) {
        new BukkitRunnable() {
            double radius = 0;
            double distanceFromCenter = 0;
            @Override
            public void run () {
                radius += 0.8;
                if (radius > 50) {
                    this.cancel();
                }
                Effects.spawnParticleRing(center, (int)(radius * 120), radius, Particle.CRIT, null);

                distanceFromCenter = (center.getX() - target.getX()) * (center.getX() - target.getX())
                        + (center.getZ() - target.getZ()) * (center.getZ() - target.getZ());
                distanceFromCenter = Math.sqrt(distanceFromCenter);

                if (target.getY() - center.getY() < 0.5 && Math.abs(distanceFromCenter - radius) < 0.2) {
                    target.sendMessage(Component.text("You Took " + shockwaveDamage + " Damage").color(NamedTextColor.DARK_GREEN));
                    target.damage(shockwaveDamage);
                }
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }
    public void spark (Location center, int projectiles) {
        double theta = 0;
        for (int i = 0; i < projectiles; i++) {
            double spawnX = center.getX() + Math.cos(theta) * 0.2;
            double spawnZ = center.getZ() + Math.sin(theta) * 0.2;
            double lookX = center.getX() + Math.cos(theta) * 60;
            double lookZ = center.getZ() + Math.sin(theta) * 60;
            new WumpusSpark(new Location(Bukkit.getWorld("world"), lookX, center.getY() + 1, lookZ)).spawn(
                    new Location(Bukkit.getWorld("world"), spawnX, center.getY() + 1, spawnZ));

            theta += (2 * Math.PI) / projectiles;
        }
    }
    // summonMinions is also used to summon in targeting mobs, which are used to dictate where an attack is being targeted.
    public void summonMinions (String mobType, int amount, Location location) {
        MythicMob minion = MythicBukkit.inst().getMobManager().getMythicMob(mobType).orElse(null);
        assert minion != null;
        minion.spawn(BukkitAdapter.adapt(location), 1);
    }
    public void ram () {
        System.out.println("Starting Ram");
        currentAttack = "Ram";
        if (target == null) {
            return;
        }
        Vector direction = new Vector(target.getX() - bukkitEntity.getX(), 0, target.getZ() - bukkitEntity.getZ());
        double xMove = Math.abs(direction.getX()); // using absolute value here for total distance traveled
        double zMove = Math.abs(direction.getY());
        direction.normalize().multiply(ramSpeed);
        if (isEnraged) {
            direction.multiply(1.3);
        }
        new BukkitRunnable() {
            int ticks = 0;
            double curXMove = 0;
            double curZMove = 0;

            @Override
            public void run () {
               // System.out.println(curXMove + " " + curZMove);
                if (ticks > 20) {
                    endRam();
                    this.cancel();
                    return;
                }
                ticks++;
                //direction.normalize();
                curXMove += Math.abs(direction.getX());
                curZMove += Math.abs(direction.getZ());
                bukkitMob.setVelocity(direction);
                for (Entity entity : bukkitEntity.getNearbyEntities(1, 1, 1)) {
                    if (entity instanceof Player) {
                        Player hitPlayer = (Player)entity;
                        hitPlayer.damage(ramDamage);
                        Vector knockback = hitPlayer.getLocation().getDirection().multiply(-2);
                        knockback.setY(0.8);
                        hitPlayer.setVelocity(knockback);
                        hitPlayer.sendMessage(Component.text("You Took " + ramDamage + " Damage").color(NamedTextColor.DARK_GREEN));
                        endRam();
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 15, 1);
    }
    public void endRam () {
        downtime = 70;
        currentAttack = null;
        if (Random.randomRoll(explodeChance)) {
            explode();
        }
    }
    public void endAttack (int downtime) {
        this.downtime = downtime;
        currentAttack = null;
    }
    @EventHandler
    public void wumpusHurtEvent (EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void wumpusHurtEvent (EntityDamageByEntityEvent event) {
        if (event.getEntity() != bukkitEntity) {
            return;
        }
        if (currentAttack != null) {
            event.getDamager().sendMessage(Component.text("Wumpus: You can only damage me after I attack!").color(NamedTextColor.DARK_AQUA));
            event.setCancelled(true);
        }
        event.setDamage(1);
        bukkitMob.setHealth((int)(Math.floor(bukkitMob.getHealth())));
        if (event.getEntity() == bukkitEntity || event.getDamager() == target) {
            downtime = Math.min(downtime, 20);
        }
    }
}
