package RoomTasks;

import org.bukkit.Location;

public class EscapeTaskInfo extends RoomTaskInfo {
    int escapeTime;
    Location escapeRegionStart;
    Location escapeRegionEnd;

    public EscapeTaskInfo (int escapeTime, Location escapeRegionStart, Location escapeRegionEnd) {
        this.escapeTime = escapeTime;
        this.escapeRegionStart = escapeRegionStart;
        this.escapeRegionEnd = escapeRegionEnd;
    }
    public int getEscapeTime () {
        return escapeTime;
    }
    public Location getEscapeRegionStart () {
        return escapeRegionStart;
    }
    public Location getEscapeRegionEnd () {
        return escapeRegionEnd;
    }
}
