package Mobs;

import Caves.Cave;
import Rooms.Room;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.Location;

import java.util.ArrayList;

// managing the spawning of wumpus mobs.
public class WumpusMobManager {
    public static String[] basicMobs = {"WumpusSkeleton", "HealingWumpusSkeleton", "StrongWumpusZombie", "AgileWumpusZombie", "TankWumpusZombie"};
    public static String[] miniBosses = {"Capybara", "LivingStatue", "WumpusSkeletonMarksman", "WumpusZombieNecromancer"};

    public static void spawn (String mobType) {
        if (mobType.equals("Wumpus")) {
            Wumpus wumpus = new Wumpus();
        }
    }
    public static void spawn (String mobType, Cave cave, Location spawnLocation) {
        WumpusMob spawnedMob = switch (mobType) {
            case "WumpusSkeleton" -> new WumpusSkeleton();
            case "HealingWumpusSkeleton" -> new HealingWumpusSkeleton();
            case "StrongWumpusZombie" -> new StrongWumpusZombie();
            case "AgileWumpusZombie" -> new AgileWumpusZombie();
            case "TankWumpusZombie" -> new TankWumpusZombie();
            default -> throw new IllegalArgumentException("Invalid mob: " + mobType);
        };
        if (spawnLocation == null) {
            spawnedMob.spawn(cave.getRoomLocationInCave(cave.getCurRoom(), cave.getCurRoom().getRandomMobSpawnLocation()));
            return;
        }
        spawnedMob.spawn(cave.getRoomLocationInCave(cave.getCurRoom(), spawnLocation));
    }
    public static void spawnMobs (Cave cave, Room room, int amount) {
        ArrayList<Location> spawnLocations = room.getMobSpawningLocations();
        if (amount > spawnLocations.size()) {
            return;
        }
        for (int i = 0; i < amount; i++) {
            spawnRandomMob(cave, spawnLocations.get(i));
        }
    }
    public static void spawnRandomMob (Cave cave, Location spawnLocation) {
        spawn(basicMobs[(int)(Math.random() * basicMobs.length)], cave, spawnLocation);
    }
    public static WumpusMob getWumpusMob (String mobName) {
        return switch (mobName) {
            case "WumpusSkeleton" -> new WumpusSkeleton();
            case "StrongWumpusZombie" -> new StrongWumpusZombie();
            case "Capybara" -> new Capybara();
            default -> throw new IllegalArgumentException("Invalid mob: " + mobName);
        };
    }
    public static String getRandomWumpusMinibossName () {
        return miniBosses[0];
    }
    public static WumpusMob getRandomWumpusMiniboss () {
        return getWumpusMob(miniBosses[0]);
    }
}
