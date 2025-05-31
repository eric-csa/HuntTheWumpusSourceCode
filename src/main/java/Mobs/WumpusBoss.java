package Mobs;

import Rooms.Room;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// default class for all hunt the wumpus bosses. Extends wumpus mob.
public class WumpusBoss extends WumpusMob {
    int phase;
    protected Room arenaRoom;
    public WumpusBoss (String boss) {
        super(boss);
    }
    public void updatePhase (int phase) {

    }
    public void spawn (Location location) {
        super.spawn(location);
        phase = 1;
    }
}
