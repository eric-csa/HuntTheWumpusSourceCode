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

    private Location currentLocation; // current location of the block in the game
    private Location prevCurrentLocation;
    private Location finishPoint; // where the player needs to move
    private boolean setupDone = false;
    private int timeLeft = 120; // 2 mins to escape!!
    private ArrayList<Location> blockSequence;
    private int currentBlockIndex = 0;
    private BukkitTask timer;
    private int timeLeftInSeconds;
    private int wrongMoves = 0;
    private Location gridStart;
    private Location gridEnd;
    private int distanceFromTarget;

    public EscapePuzzle (Room room, Player player, Location gridStart, Location gridEnd) {
        super(room, player);
        this.gridStart = gridStart;
        this.gridEnd = gridEnd;
        initializeTask();
        timeLeft = 120 * 20;
    }
    // start a game for a player
    public void initializeTask() {

        // tell player what to do
        taskPlayer.sendMessage(ChatColor.GREEN + "=== WUMPUS ESCAPE GAME ===");
        taskPlayer.sendMessage(ChatColor.YELLOW + "Find the escape block to open the exit!");
        taskPlayer.sendMessage(ChatColor.YELLOW + "I'll tell you if ur getting closer or farther from target");

        currentLocation = Random.randomLocation(gridStart, gridEnd);

        do {
            finishPoint = Random.randomLocation(gridStart, gridEnd);
        } while (locationsMatch(currentLocation, finishPoint));

        distanceFromTarget = getDistance(currentLocation, finishPoint);
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
        world.getBlockAt(currentLocation).setType(Material.GREEN_WOOL);
    }
    public void updateTask () {
        if (!taskPlayer.isOnline()) {
            return;
        }
        super.updateTask();
        timeLeft--;
        timeLeftInSeconds = timeLeft / 20;

        if (timeLeft % 20 == 0) {
            // tell player time
            TitleSender.sendTitle(String.valueOf(timeLeftInSeconds), "", taskPlayer, 250, 500, 250,
                        NamedTextColor.RED, NamedTextColor.RED);

            // make scary effects when time low
            if (timeLeftInSeconds <= 10) {
                // cave rumbling effect
                taskPlayer.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, taskPlayer.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        if (timeLeft <= 0) {
            taskPlayer.sendMessage(Component.text("You failed to find the escape block in time...").color(NamedTextColor.RED));
            taskPlayer.performCommand("kill");
            finishTask();
        }
    }

    // handle button clicks to move the player's location in the grid
    @EventHandler
    public void onPlayerClickButton(PlayerInteractEvent event) {
        if (event.getPlayer() != taskPlayer) {
            return;
        }
        Location buttonLocation = event.getInteractionPoint();
        taskPlayer.sendMessage(buttonLocation.getBlock().getBlockData().getMaterial().toString());
        if (buttonLocation.getBlock().getBlockData().getMaterial() != Material.STONE_BUTTON) {
            return;
        }
        buttonLocation.setY(buttonLocation.getY() - 1);
        prevCurrentLocation = currentLocation.clone();

        if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.RED_WOOL) {
            if ((int)currentLocation.getZ() + 1 > (int)gridEnd.getZ()) {
                taskPlayer.sendMessage(Component.text("Invalid Movie: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setZ((int)currentLocation.getZ() + 1);
            updateMove();
        }
        else if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.YELLOW_WOOL) {
            if ((int)currentLocation.getZ() - 1 < (int)gridStart.getZ()) {
                taskPlayer.sendMessage(Component.text("Invalid Movie: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setZ((int)currentLocation.getZ() - 1);
            updateMove();
        }
        else if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.GREEN_WOOL) {
            if ((int)currentLocation.getX() - 1 < (int)gridStart.getX()) {
                taskPlayer.sendMessage(Component.text("Invalid Movie: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setX((int)currentLocation.getX() - 1);
            updateMove();
        }
        else if (world.getBlockAt(buttonLocation).getBlockData().getMaterial() == Material.BLUE_WOOL) {
            if ((int)currentLocation.getX() + 1 > (int)gridEnd.getX()) {
                taskPlayer.sendMessage(Component.text("Invalid Movie: Cannot go in that direction").color(NamedTextColor.RED));
                return;
            }
            currentLocation.setX((int)currentLocation.getX() + 1);
            updateMove();
        }

    }
    private boolean locationsMatch(Location loc1, Location loc2) {
        return (int)loc1.getX() == (int)loc2.getX() && (int)loc1.getY() == (int)loc2.getY() && (int)loc1.getZ() == (int)loc2.getZ();
    }
    public int getDistance (Location loc1, Location loc2) {
        return Math.abs((int)loc2.getX() - (int)loc1.getX()) + Math.abs((int)loc2.getZ() - (int)loc1.getZ());
    }
    public void updateMove () {
        world.getBlockAt(prevCurrentLocation).setType(Material.WHITE_WOOL);
        world.getBlockAt(currentLocation).setType(Material.GREEN_WOOL);

        if (locationsMatch(currentLocation, finishPoint)) {
            givePlayerReward();
            TitleSender.sendTitle("You succesfully completed the escape puzzle!", "a door has opened up the exit", taskPlayer,
                    1000, 3000, 1000, NamedTextColor.GREEN, NamedTextColor.DARK_GREEN);
            ServerData.getPlayerData(taskPlayer).updateScore(timeLeftInSeconds);
            finishTask();
        }
        if (getDistance(currentLocation, finishPoint) > distanceFromTarget) {
            addWrongMove();
            taskPlayer.sendMessage(Component.text("Getting FARTHER from the target!").color(NamedTextColor.RED));
        }
        else taskPlayer.sendMessage(Component.text("Getting CLOSER to the target!").color(NamedTextColor.GREEN));

        distanceFromTarget = getDistance(currentLocation, finishPoint);
    }
    public void addWrongMove() {
        wrongMoves++;
        if (wrongMoves >= 4) {
            // penalty for 4 wrong moves
            wrongMoves = 0;
            timeLeft -= 200;
            if (timeLeft <= 0) {
                timeLeft = 0;
            }
            taskPlayer.sendMessage(ChatColor.RED + "Time Penalty! You kept going in the wrong direction!! " + timeLeftInSeconds + " seconds left!!");
        }
    }

}

