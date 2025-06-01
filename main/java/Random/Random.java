package Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;

// helper class for random number generating functionalities.
public class Random {
    // returns true if the number rolled is less than the chance, where chance must be a real number from 0-1.
    // used for random chance actions.
    public static boolean randomRoll (double chance) {
        return Math.random() < chance;
    }
    public static int randomIntInRange (int start, int end) {
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        int rangeSize = end - start + 1;
        return (int)(Math.random() * rangeSize) + start;
    }
    public static Location randomLocation (int startX, int endX, int startY, int endY, int startZ, int endZ) {
        return new Location(Bukkit.getWorld("world"), randomIntInRange(startX, endX),
                randomIntInRange(startY, endY), randomIntInRange(startZ, endZ));
    }
    public static Location randomLocation (Location start, Location end) {
        return randomLocation((int)(start.getX()), (int)(end.getX()), (int)(start.getY()), (int)(end.getY()), (int)(start.getZ()), (int)(end.getZ()));
    }
    public static Location randomLocation (Location start, Location end, double y) {
        return randomLocation((int)(start.getX()), (int)(end.getX()), (int)y, (int)y, (int)(start.getZ()), (int)(end.getZ()));
    }
}
