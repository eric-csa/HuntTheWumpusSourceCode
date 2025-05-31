package Rooms;

import Minigames.EscapePuzzle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EscapePuzzleRoom extends Room {
    private Location gridStart;
    private Location gridEnd;
    public EscapePuzzleRoom (Location start, Location end, Location playerSpawn,
                             Location gridStart, Location gridEnd) {
        super(start, end, playerSpawn);
        taskType = "EscapePuzzle";

        this.gridStart = gridStart;
        this.gridEnd = gridEnd;
    }
    public void setEscapePuzzleTask (Player player, Location gridStart, Location gridEnd) {
        task = new EscapePuzzle(this, player, gridStart, gridEnd);
    }
    public Location getGridStart () {
        return gridStart;
    }
    public Location getGridEnd () {
        return gridEnd;
    }
    public Room copyRoom () {
        EscapePuzzleRoom escapePuzzleRoom = new EscapePuzzleRoom(start, end, playerSpawn, gridStart, gridEnd);
        escapePuzzleRoom.setNorthPortalLocation(northPortalLocation);
        escapePuzzleRoom.setNorthWestPortalLocation(northWestPortalLocation);
        escapePuzzleRoom.setNorthEastPortalLocation(northEastPortalLocation);
        escapePuzzleRoom.setSouthWestPortalLocation(southWestPortalLocation);
        escapePuzzleRoom.setSouthEastPortalLocation(southEastPortalLocation);
        escapePuzzleRoom.setSouthPortalLocation(southPortalLocation);

        return escapePuzzleRoom;
    }
}
