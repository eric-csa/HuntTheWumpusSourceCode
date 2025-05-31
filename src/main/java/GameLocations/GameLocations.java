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


// game locations class updates and keeps track of : the player's room number in the cave, the wumpus room number, but also,
// the x, y, z, coordinate of the player in minecraft. This is used to detect if a player is currently standing on a room teleporter,
// the player gets teleported to an adjacent room if they are standing on a teleporter block.
public class GameLocations implements Listener {
    private int playerRoom = 0;
    private int wumpusRoom = 0;
    private BukkitTask locationChecker;
    private Player player;
    private Location playerLocation; // the location of the player
    private Material playerBlock; // the block the player is standing on
    private World world = Bukkit.getWorld("world");
    private GameManager game;
    public GameLocations (Player player, int playerRoom, int wumpusRoom) { // constructor for the initial game location
        this.playerRoom = playerRoom;
        this.wumpusRoom = wumpusRoom;
        this.player = player;

        startLocationChecker();
        game = ServerData.getPlayerData(player).getGame();
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
    }
    public void updatePlayer (Player player) {
        this.player = player;
    }
    // function creates a new runnable task, which checks if the player has moved into another room every 5 in game ticks,
    // or once every 20 milliseconds.
    public void startLocationChecker () {
        locationChecker = new BukkitRunnable() {
            @Override
            public void run () {
                if (game == null) {
                    game = ServerData.getPlayerData(player).getGame();
                }
                updateLocations();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 5);
    }
    // function destroys all active tasks
    public void destroy () {
        System.out.println("Unregistered Location Tracker");
        locationChecker.cancel();
        HandlerList.unregisterAll(this);
    }
    public Material getPlayerBlock () {
        return playerBlock;
    }
    // the function that updates the player and wumpus location every single in game tick. Sends signal to game
    // manager after it detects that the player is moving into a new room
    public void updateLocations () {
        playerLocation = player.getLocation();
        playerBlock = world.getBlockData(new Location(world, playerLocation.getX(),
                playerLocation.getY() - 1, playerLocation.getZ())).getMaterial();
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
//    // the following 6 methods check if the player is standing on teleporting blocks. If it equals to that
    // specified block, (which will have its texture changed in the resource pack) then it returns true
    public boolean isMovingNorth () {
        return playerBlock == Material.DEEPSLATE_COAL_ORE;
    }
    public boolean isMovingNorthWest () {
        return playerBlock == Material.DEEPSLATE_COPPER_ORE;
    }
    public boolean isMovingNorthEast () {
        return playerBlock == Material.DEEPSLATE_DIAMOND_ORE;
    }
    public boolean isMovingSouthWest () {
        return playerBlock == Material.DEEPSLATE_EMERALD_ORE;
    }
    public boolean isMovingSouthEast () {
        return playerBlock == Material.DEEPSLATE_GOLD_ORE;
    }
    public boolean isMovingSouth () {
        return playerBlock == Material.DEEPSLATE_IRON_ORE;
    }
    public boolean isInRange (Location location, Location start, Location end) {
        return true;
        //return start.getX() <= location.getX() && location.getX() <= end.getX()
         //      && start.getY() <= location.getY() && location.getY() <= end.getY()
        //       && start.getZ() <= location.getZ() && location.getZ() <= end.getZ();
    }
    // the following event listener checks whether a player has successfully shot the wumpus.
    @EventHandler
    public void onPlayerShoot (ProjectileHitEvent event) {
        if (event.getEntity().getShooter() == player) {
            HuntTheWumpusPlugin.getPlugin().getLogger().info("Player Is Shooting Wumpus");
        }
        Block block = event.getHitBlock();
        System.out.println(block.getBlockData().getMaterial());
        if (block == null) {
            event.setCancelled(true);
            return;
        }
        if (isInRange(block.getLocation(), game.getCave().getStart(),
                game.getCave().getEnd())) {
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

        event.getEntity().remove();
    }
    // this event listener prevents the default minecraft nether portal teleportation from happening.
    @EventHandler
    public void onPlayerUsePortal (PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            event.setCancelled(true);
        }
    }
    // this event listener prevents arrows from teleporting to another dimension if it enters a portal.
    @EventHandler
    public void arrowTeleportEvent (EntityTeleportEvent event) {
        if (event.getEntity().getType() == EntityType.ARROW) {
            event.setCancelled(true);
        }
    }
}
