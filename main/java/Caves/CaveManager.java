package Caves;

import ServerData.ServerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;

// purpose of caveManager: Assigns a player into a new empty cave in the minecraft world when they query to start a new game.
// ensures that no two players are in the same exact cave, unless they choose to play together
public class CaveManager {
    private static boolean[] isAvailable = new boolean[25]; // number of arenas
    private static final double xDistanceApart = 500;
    public static final World world = Bukkit.getWorld("world");
    private static final double defaultY = 256;
    public static final double arenaSize = 300;
    private static final Location defaultBottomLeft = new Location(world, 0, defaultY, 0);
    private static final Location defaultTopRight = new Location(world, arenaSize, defaultY, arenaSize);
    public static Cave[] caves = new Cave[25];

    public static void initCaveManager () { // initializes all of the caves as empty
        Arrays.fill(isAvailable, true);
        caves = new Cave[25];
    }
    public static Cave createCave(Player curPlayer) { //creates a cave for the player
        int caveNum = 0;

        for (int i = 0; i < isAvailable.length; i++) {
            if (isAvailable[i]) {
                caveNum = i;
                break;
            }
        }
        if (caveNum == isAvailable.length) {
            curPlayer.sendMessage("Could not start game: All caves full");
        }

        // uses locations in the world to separate players.
        Location bottomLeft = new Location(world, defaultBottomLeft.x() + xDistanceApart * caveNum, defaultBottomLeft.y(), defaultBottomLeft.z());
        Location topRight = new Location(world, defaultTopRight.x() + xDistanceApart * caveNum, defaultTopRight.y(), defaultTopRight.z());

        caves[caveNum] = new Cave(bottomLeft, topRight);

        isAvailable[caveNum] = false;
        ServerData.getPlayerData(curPlayer).setPlayerCave(caveNum);

        return caves[caveNum];
    }
    public static void clearCave (int caveNum) { // clears the cave whe na player is done with it.
        isAvailable[caveNum] = true;
    }
}
