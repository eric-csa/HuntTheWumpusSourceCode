package Minigames;

import Caves.Cave;
import Random.Random;
import RoomTasks.RoomTask;
import Rooms.Room;
import ServerData.ServerData;
import Titles.TitleSender;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static Caves.CaveManager.world;

public class EscapePuzzle extends RoomTask implements Listener {

    // Current position of the player's block in the grid
    private Location currentLocation;

    // Previous position of the player's block before moving
    private Location prevCurrentLocation;

    // Target location player needs to reach to escape
    private Location finishPoint;

    // Tracks if setup is completed (unused here but could be for future use)
    private boolean setupDone = false;

    // Remaining time in game ticks (20 ticks = 1 second)
    private int timeLeft = 120; // initially 2 mins (will be overwritten)

    // Sequence of blocks for some other purpose (unused in current code)
    private ArrayList<Location> blockSequence;

    // Index to track position in block sequence (unused)
    private int currentBlockIndex = 0;

    // Bukkit task to run timer (unused, updateTask used instead)
    private BukkitTask timer;

    // Time left converted to seconds for display
    private int timeLeftInSeconds;

    // Counts how many wrong moves the player made in a row
    private int wrongMoves = 0;

    // Starting corner of the grid area
    private Location gridStart;

    // Ending corner of the grid area
    private Location gridEnd;

    // Distance from currentLocation to finishPoint for tracking progress
    private int distanceFromTarget;

    // Constructor for the escape puzzle task, takes the room, player, and grid boundaries
    public EscapePuzzle (Room room, Player player, Location gridStart, Location gridEnd) {
        super(room, player);
        this.gridStart = gridStart;
        this.gridEnd = gridEnd;

        // Initialize the task (set starting point, finish point, show instructions)
        initializeTask();

        // Set total time in ticks (120 seconds * 20 ticks per second)
        timeLeft = 120 * 20;
    }

    // Initialize the escape puzzle task: setup start and finish, instructions, event listener
    public void initializeTask() {
        // Send player instructions for the game
        taskPlayer.sendMessage(ChatColor.GREEN + "=== WUMPUS ESCAPE GAME ===");
        taskPlayer.sendMessage(ChatColor.YELLOW + "Find the escape block to open the exit!");
        taskPlayer.sendMessage(ChatColor.YELLOW + "I'll tell you if ur getting closer or farther from target");

        // Randomly pick the starting location inside the grid
        currentLocation = Random.randomLocation(gridStart, gridEnd);

        // Randomly pick the finish location inside the grid, making sure it differs from start
        do {
            finishPoint = Random.randomLocation(gridStart, gridEnd);
        } while (locationsMatch(currentLocation, finishPoint));

        // Calculate the initial distance from start to finish
        distanceFromTarget = getDistance(currentLocation, finishPoint);

        // Register this class as an event listener to handle button clicks
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());

        // Mark the player's starting position visually with a green wool block
        world.getBlockAt(currentLocation).setType(Material.GREEN_WOOL);
    }

    // This method is called every tick (20 times per second) to update the task state
    public void updateTask () {
        // Skip if player is not online
        if (!taskPlayer.isOnline()) {
            return;
        }

        // Call superclass update (if any logic there)
        super.updateTask();

        // Decrease time left (in ticks)
        timeLeft--;

        // Convert time left from ticks to seconds for display
        timeLeftInSeconds = timeLeft / 20;

        // Every second, update the player with time left
        if (timeLeft % 20 == 0) {
            // Show time remaining as a title on the player's screen, colored red
            TitleSender.sendTitle(String.valueOf(timeLeftInSeconds), "", taskPlayer, 250, 500, 250,
                        NamedTextColor.RED, NamedTextColor.RED);

            // When time is very low, spawn smoke particles around the player to create tension
            if (timeLeftInSeconds <= 10) {
                taskPlayer.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL,
                        taskPlayer.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }

        // If time runs out, notify player and kill them to end the game
        if (timeLeft <= 0) {
            taskPlayer.sendMessage(Component.text("You failed to find the escape block in time...").color(NamedTextColor.RED));
            taskPlayer.performCommand("kill");
            finishTask();
        }
    }

    // Event handler to detect when player clicks a button to move
    @EventHandler
    public void onPlayerClickButton(PlayerInteractEvent event) {
        // Ignore if the player clicking is not the task player
        if (event.getPlayer() != taskPlayer) {
            return;
        }

        // Get the location of the block the player interacted with
        Location buttonLocation = event.getInteractionPoint();

        // Debug: send the material of the clicked block to player
        taskPlayer.sendMessage(buttonLocation.getBlock().getBlockData().getMaterial().toString());

        // If clicked block is not a stone button, ignore
        if (buttonLocation.getBlock().getBlockData().getMaterial() != Material.STONE_BUTTON) {
            return;
        }

        // Get the block under the button (used to determine direction)
        buttonLocation.setY(buttonLocation.getY() - 1);

        // Save current location before moving
        prevCurrentLocation = currentLocation.clone();

        // Check the color of the wool block below the button to determine movement direction

        if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.RED_WOOL) {
            // Moving forward (+Z direction), check grid boundary
            if ((int)currentLocation.getZ() + 1 > (int)gridEnd.getZ()) {
                taskPlayer.sendMessage(Component.text("Invalid Move: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setZ((int)currentLocation.getZ() + 1);
            updateMove();
        }
        else if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.YELLOW_WOOL) {
            // Moving backward (-Z direction), check grid boundary
            if ((int)currentLocation.getZ() - 1 < (int)gridStart.getZ()) {
                taskPlayer.sendMessage(Component.text("Invalid Move: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setZ((int)currentLocation.getZ() - 1);
            updateMove();
        }
        else if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.GREEN_WOOL) {
            // Moving left (-X direction), check grid boundary
            if ((int)currentLocation.getX() - 1 < (int)gridStart.getX()) {
                taskPlayer.sendMessage(Component.text("Invalid Move: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setX((int)currentLocation.getX() - 1);
            updateMove();
        }
        else if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.BLUE_WOOL) {
            // Moving right (+X direction), check grid boundary
            if ((int)currentLocation.getX() + 1 > (int)gridEnd.getX()) {
                taskPlayer.sendMessage(Component.text("Invalid Move: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setX((int)currentLocation.getX() + 1);
            updateMove();
        }
    }

    // Helper method to check if two locations match exactly (ignores decimal parts)
    private boolean locationsMatch(Location loc1, Location loc2) {
        return (int)loc1.getX() == (int)loc2.getX()
            && (int)loc1.getY() == (int)loc2.getY()
            && (int)loc1.getZ() == (int)loc2.getZ();
    }

    // Calculate Manhattan distance (X + Z difference) between two locations (ignores Y)
    public int getDistance (Location loc1, Location loc2) {
        return Math.abs((int)loc2.getX() - (int)loc1.getX()) + Math.abs((int)loc2.getZ() - (int)loc1.getZ());
    }

    // Update the game state after player moves
    public void updateMove () {
        // Change the block at the previous location to white wool (indicating visited)
        world.getBlockAt(prevCurrentLocation).setType(Material.WHITE_WOOL);

        // Change the block at the new current location to green wool (indicating player position)
        world.getBlockAt(currentLocation).setType(Material.GREEN_WOOL);

        // Check if player has reached the finish point
        if (locationsMatch(currentLocation, finishPoint)) {
            // Give player reward (method from superclass)
            givePlayerReward();

            // Show success title and subtitle
            TitleSender.sendTitle("You succesfully completed the escape puzzle!", "a door has opened up the exit",
                    taskPlayer, 1000, 3000, 1000, NamedTextColor.GREEN, NamedTextColor.DARK_GREEN);

            // Update player's score with remaining time
            ServerData.getPlayerData(taskPlayer).updateScore(timeLeftInSeconds);

            // End the task/game
            finishTask();
        }

        // Check if the player moved farther away from the target
        if (getDistance(currentLocation, finishPoint) > distanceFromTarget) {
            addWrongMove(); // Increment wrong moves and apply penalties if needed
            taskPlayer.sendMessage(Component.text("Getting FARTHER from the target!").color(NamedTextColor.RED));
        }
        else {
            // Player moved closer to target
            taskPlayer.sendMessage(Component.text("Getting CLOSER to the target!").color(NamedTextColor.GREEN));
        }

        // Update the stored distance for the next move
        distanceFromTarget = getDistance(currentLocation, finishPoint);
    }

    // Tracks wrong moves and applies a time penalty after 4 wrong moves
    public void addWrongMove() {
        wrongMoves++;
        if (wrongMoves >= 4) {
            // Reset wrong moves counter
            wrongMoves = 0;

            // Subtract 10 seconds worth of ticks as penalty
            timeLeft -= 200;
            if (timeLeft <= 0) {
                timeLeft = 0; // Prevent negative time
            }

            // Notify player of penalty and remaining time
            taskPlayer.sendMessage(ChatColor.RED + "Time Penalty! You kept going in the wrong direction!! " + timeLeftInSeconds + " seconds left!!");
        }
    }

}