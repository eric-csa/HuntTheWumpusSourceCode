package GameLocations;

import GameManager.GameManager;
import ServerData.ServerData;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

// This class manages and updates player-related location data and events for the Hunt the Wumpus game.
// It tracks the player's room and coordinates, handles movement detection, teleportation triggers,
// and detects arrow hits on Wumpus rooms.
public class GameLocations implements Listener {

    // The current room the player is in
    private int playerRoom = 0;

    // The current room the Wumpus is in
    private int wumpusRoom = 0;

    // A repeating task that checks the player’s location
    private BukkitTask locationChecker;

    // The player being tracked
    private Player player;

    // The current block location of the player
    private Location playerLocation;

    // The block type the player is standing on
    private Material playerBlock;

    // Reference to the Minecraft world
    private World world = Bukkit.getWorld("world");

    // Reference to the game manager
    private GameManager game;

    /**
     * Constructor: initializes tracking for a player, including their room and Wumpus room.
     * Also starts the location checker and registers event listeners.
     */
    public GameLocations(Player player, int playerRoom, int wumpusRoom) {
        this.playerRoom = playerRoom;
        this.wumpusRoom = wumpusRoom;
        this.player = player;

        startLocationChecker();
        game = ServerData.getPlayerData(player).getGame();
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
    }

    // Allows reassignment of the tracked player object
    public void updatePlayer(Player player) {
        this.player = player;
    }

    /**
     * Begins a repeating task that updates the player’s location every 5 game ticks (~0.25 seconds).
     */
    public void startLocationChecker() {
        locationChecker = new BukkitRunnable() {
            @Override
            public void run() {
                if (game == null) {
                    game = ServerData.getPlayerData(player).getGame();
                }
                updateLocations();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 5);
    }

    /**
     * Stops the location tracking task and unregisters this class’s event listeners.
     */
    public void destroy() {
        System.out.println("Unregistered Location Tracker");
        locationChecker.cancel();
        HandlerList.unregisterAll(this);
    }

    // Returns the block the player is currently standing on
    public Material getPlayerBlock() {
        return playerBlock;
    }

    /**
     * Checks the player's current position and determines if they stepped on a teleport trigger.
     * If so, it instructs the GameManager to move the player in the corresponding direction.
     */
    public void updateLocations() {
        playerLocation = player.getLocation();
        playerBlock = world.getBlockData(new Location(world,
                playerLocation.getX(), playerLocation.getY() - 1, playerLocation.getZ())).getMaterial();

        if (isMovingNorth()) {
            System.out.println("A Player Moved North!");
            game.movePlayer(0);
        }
        if (isMovingNorthWest()) {
            System.out.println("A Player Moved NorthWest!");
            game.movePlayer(1);
        }
        if (isMovingNorthEast()) {
            System.out.println("A Player Moved NorthEast!");
            game.movePlayer(2);
        }
        if (isMovingSouthWest()) {
            System.out.println("A Player Moved SouthWest!");
            game.movePlayer(3);
        }
        if (isMovingSouthEast()) {
            System.out.println("A Player Moved SouthEast!");
            game.movePlayer(4);
        }
        if (isMovingSouth()) {
            System.out.println("A Player Moved South!");
            game.movePlayer(5);
        }
    }

    // The following 6 methods check whether the player is standing on a specific ore block.
    // Each block represents movement in a specific direction.

    public boolean isMovingNorth() {
        return playerBlock == Material.DEEPSLATE_COAL_ORE;
    }

    public boolean isMovingNorthWest() {
        return playerBlock == Material.DEEPSLATE_COPPER_ORE;
    }

    public boolean isMovingNorthEast() {
        return playerBlock == Material.DEEPSLATE_DIAMOND_ORE;
    }

    public boolean isMovingSouthWest() {
        return playerBlock == Material.DEEPSLATE_EMERALD_ORE;
    }

    public boolean isMovingSouthEast() {
        return playerBlock == Material.DEEPSLATE_GOLD_ORE;
    }

    public boolean isMovingSouth() {
        return playerBlock == Material.DEEPSLATE_IRON_ORE;
    }

    /**
     * Checks if a location is within a given cuboid range. Always returns true currently (placeholder).
     */
    public boolean isInRange(Location location, Location start, Location end) {
        return true;

        // Uncomment and use this code if bounding box checks are needed:
        /*
        return start.getX() <= location.getX() && location.getX() <= end.getX()
                && start.getY() <= location.getY() && location.getY() <= end.getY()
                && start.getZ() <= location.getZ() && location.getZ() <= end.getZ();
        */
    }

    /**
     * Listens for projectile hits (e.g. arrows). If the player shot it and it hits a valid block,
     * the method checks whether the Wumpus is hit based on the ore block hit.
     */
    @EventHandler
    public void onPlayerShoot(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() == player) {
            HuntTheWumpusPlugin.getPlugin().getLogger().info("Player Is Shooting Wumpus");
        }

        Block block = event.getHitBlock();
        System.out.println(block.getBlockData().getMaterial());

        if (block == null) {
            event.setCancelled(true);
            return;
        }

        if (isInRange(block.getLocation(), game.getCave().getStart(), game.getCave().getEnd())) {
            Material blockMaterial = block.getBlockData().getMaterial();

            if (blockMaterial == Material.DEEPSLATE_COAL_ORE) {
                game.checkForWumpusHit(0);
            }
            if (blockMaterial == Material.DEEPSLATE_COPPER_ORE) {
                game.checkForWumpusHit(1);
            }
            if (blockMaterial == Material.DEEPSLATE_DIAMOND_ORE) {
                game.checkForWumpusHit(2);
            }
            if (blockMaterial == Material.DEEPSLATE_EMERALD_ORE) {
                game.checkForWumpusHit(3);
            }
            if (blockMaterial == Material.DEEPSLATE_GOLD_ORE) {
                game.checkForWumpusHit(4);
            }
            if (blockMaterial == Material.DEEPSLATE_IRON_ORE) {
                game.checkForWumpusHit(5);
            }
        }

        // Remove the projectile after impact
        event.getEntity().remove();
    }

    /**
     * Prevents default Nether portal teleportation for players, which could break the game state.
     */
    @EventHandler
    public void onPlayerUsePortal(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents arrows from teleporting through portals, which would break mechanics.
     */
    @EventHandler
    public void arrowTeleportEvent(EntityTeleportEvent event) {
        if (event.getEntity().getType() == EntityType.ARROW) {
            event.setCancelled(true);
        }
    }
}
