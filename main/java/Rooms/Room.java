package Rooms;

import Caves.Cave;
import Mobs.WumpusMob;
import Mobs.WumpusMobManager;
import RoomTasks.*;
import Titles.TitleSender;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;

/**
 * Represents a custom room in the HuntTheWumpusPlugin game.
 * Each room defines a cuboid region in the Minecraft world with
 * designated spawn points, portals, monster spawn locations, and
 * associated tasks or challenges.
 * 
 * The room can be rendered inside a cave, effectively copying a predefined
 * template region into a new location.
 */
public class Room {
    // Coordinates defining the cuboid bounds of the room in the world
    protected Location start;   // Start corner of the room (min corner)
    protected Location end;     // End corner of the room (max corner)
    protected World world = Bukkit.getWorld("world");  // The Minecraft world this room exists in
    
    // Player spawn locations for this room
    protected Location playerSpawn;              // Player spawn location when entering the room
    protected Location roomClearedPlayerSpawn;   // Optional spawn location if the room is cleared
    
    // Room state
    protected boolean cleared;                    // Flag if the room has been cleared by the player
    
    // Player currently in this room
    protected Player player;
    
    // Task-related fields
    protected String taskType;                    // String representing the type of task in this room
    protected RoomTask task;                      // The current task associated with this room
    
    // Escape task region and timing
    protected Location escapeRegionStart;        // Start location of escape region (for Escape tasks)
    protected Location escapeRegionEnd;          // End location of escape region
    protected int escapeTime;                     // Time limit for escape tasks
    
    // Locations where mobs can spawn inside this room
    protected ArrayList<Location> mobSpawningLocations;
    
    // Specific location for target mobs (used for Target tasks)
    protected Location targetSpawnLocation;
    
    // Name identifier for this room
    protected String roomName;
    
    // List of possible tasks that can be randomly assigned to this room
    private ArrayList<RoomTaskInfo> possibleRoomTasks;
    
    // Locations of portals within the room to neighboring rooms or regions
    protected Location northPortalLocation;
    protected Location northWestPortalLocation;
    protected Location northEastPortalLocation;
    protected Location southPortalLocation;
    protected Location southWestPortalLocation;
    protected Location southEastPortalLocation;

    // Constructors to initialize the room with various parameters and defaults
    public Room(Location start, Location end, Location playerSpawn) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        possibleRoomTasks = new ArrayList<>();
        setMobSpawningLocations();
    }

    public Room(Location start, Location end, Location playerSpawn, String taskType) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        this.taskType = taskType;
        possibleRoomTasks = new ArrayList<>();
        setMobSpawningLocations();
    }

    public Room(Location start, Location end, Location playerSpawn, Location escapeRegionStart, Location escapeRegionEnd) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        this.escapeRegionStart = escapeRegionStart;
        this.escapeRegionEnd = escapeRegionEnd;
        possibleRoomTasks = new ArrayList<>();
        setMobSpawningLocations();
    }

    public Room(Location start, Location end, Location playerSpawn, ArrayList<RoomTaskInfo> possibleRoomTasks) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        this.possibleRoomTasks = possibleRoomTasks;
        setMobSpawningLocations();
    }

    /**
     * Sets the type of task for this room.
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    /**
     * Scans the room region for specific blocks that define mob spawning locations.
     * Adds locations where mobs can spawn, e.g. where INFESTED_DEEPSLATE blocks exist.
     * Also detects target spawn location if a TARGET block is found.
     */
    public void setMobSpawningLocations() {
        mobSpawningLocations = new ArrayList<>();
        int startX = (int) start.getX();
        int endX = (int) end.getX();
        int startY = (int) start.getY();
        int endY = (int) end.getY();
        int startZ = (int) start.getZ();
        int endZ = (int) end.getZ();

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Material material = world.getBlockAt(x, y, z).getBlockData().getMaterial();
                    if (material == Material.INFESTED_DEEPSLATE) {
                        // Add mob spawning location slightly above the block center
                        mobSpawningLocations.add(new Location(world, x + 0.5, y + 1, z + 0.5));
                    }
                    if (material == Material.TARGET) {
                        // Set target spawn location for special target task mobs
                        targetSpawnLocation = new Location(world, x + 0.5, y + 1, z + 0.5);
                    }
                }
            }
        }
    }

    /**
     * Sets the player spawn location for when the room is cleared.
     */
    public void setRoomClearedPlayerSpawn(Location roomClearedPlayerSpawn) {
        this.roomClearedPlayerSpawn = roomClearedPlayerSpawn;
    }

    // Setters for each portal location within the room.
    public void setNorthPortalLocation(Location location) {
        northPortalLocation = location;
    }

    public void setNorthWestPortalLocation(Location location) {
        northWestPortalLocation = location;
    }

    public void setNorthEastPortalLocation(Location location) {
        northEastPortalLocation = location;
    }

    public void setSouthPortalLocation(Location location) {
        southPortalLocation = location;
    }

    public void setSouthWestPortalLocation(Location location) {
        southWestPortalLocation = location;
    }

    public void setSouthEastPortalLocation(Location location) {
        southEastPortalLocation = location;
    }

    /**
     * Creates and returns a shallow copy of this room with the same properties.
     */
    public Room copyRoom() {
        Room room = new Room(start, end, playerSpawn, possibleRoomTasks);
        room.setNorthPortalLocation(northPortalLocation);
        room.setNorthWestPortalLocation(northWestPortalLocation);
        room.setNorthEastPortalLocation(northEastPortalLocation);
        room.setSouthWestPortalLocation(southWestPortalLocation);
        room.setSouthEastPortalLocation(southEastPortalLocation);
        room.setSouthPortalLocation(southPortalLocation);
        room.setRoomClearedPlayerSpawn(roomClearedPlayerSpawn);

        return room;
    }

    // Convenience methods to assign various tasks to this room with required parameters.

    /**
     * Assigns a SurviveTask to this room.
     */
    public void setSurviveTask(Player player, int surviveTime) {
        task = new SurviveTask(this, player, surviveTime);
    }

    /**
     * Assigns a KillTask to this room.
     */
    public void setKillTask(Player player, int killsRequired) {
        task = new KillTask(this, player, killsRequired);
    }

    /**
     * Assigns a TargetTask with a specific WumpusMob target.
     */
    public void setTargetTask(Player player, WumpusMob target, Location targetSpawnLocation) {
        task = new TargetTask(this, player, target, targetSpawnLocation);
    }

    /**
     * Assigns an EscapeTask with specified region and time limit.
     */
    public void setEscapeTask(Player player, Location escapeRegionStart, Location escapeRegionEnd, int escapeTime) {
        task = new EscapeTask(this, player, escapeRegionStart, escapeRegionEnd, escapeTime);
    }

    /**
     * Sets the player currently in this room.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    // TODO: Add more advanced mob spawn randomization and configuration here

    // Methods to add possible tasks that this room can randomly select from

    public void addEscapeTask(int escapeTime, Location escapeRegionStart, Location escapeRegionEnd) {
        possibleRoomTasks.add(new EscapeTaskInfo(escapeTime, escapeRegionStart, escapeRegionEnd));
    }

    public void addKillTask(int killsRequired) {
        possibleRoomTasks.add(new KillTaskInfo(killsRequired));
    }

    public void addSurviveTask(int surviveTime) {
        possibleRoomTasks.add(new SurviveTaskInfo(surviveTime));
    }

    public void addTargetTask() {
        if (targetSpawnLocation == null) {
            throw new IllegalArgumentException("Cannot create target task for this room since the room does not have a valid targetSpawnLocation");
        }
        possibleRoomTasks.add(new TargetTaskInfo(WumpusMobManager.getRandomWumpusMinibossName()));
    }

    /**
     * Returns a random task info from the possible tasks configured for this room.
     */
    public RoomTaskInfo getRandomRoomTaskInfo() {
        if (possibleRoomTasks == null || possibleRoomTasks.size() == 0) {
            throw new IllegalArgumentException("Invalid Room Task Initialization: Room does not have task info");
        }
        return possibleRoomTasks.get((int)(Math.random() * possibleRoomTasks.size()));
    }

    // Getter methods for task information and room boundaries

    public String getTaskType() {
        return taskType;
    }

    public RoomTask getRoomTask() {
        return task;
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    /**
     * Checks if the room has any mob spawning locations available.
     */
    public boolean canSpawnMobs() {
        return mobSpawningLocations != null && !mobSpawningLocations.isEmpty();
    }

    /**
     * Returns the count of mob spawning locations in the room.
     */
    public int getRoomSpawnLocationCount() {
        if (!canSpawnMobs()) {
            return 0;
        }
        return mobSpawningLocations.size();
    }

    /**
     * Returns a randomized list of mob spawning locations.
     */
    public ArrayList<Location> getMobSpawningLocations() {
        ArrayList<Location> mobSpawningLocationsCopy = new ArrayList<>(this.mobSpawningLocations);

        ArrayList<Location> randomizedLocations = new ArrayList<>();
        while (!mobSpawningLocationsCopy.isEmpty()) {
            randomizedLocations.add(mobSpawningLocationsCopy.remove((int)(Math.random() * mobSpawningLocationsCopy.size())));
        }
        return randomizedLocations;
    }

    /**
     * Returns a random mob spawn location from the available spawn locations.
     */
    public Location getRandomMobSpawnLocation() {
        if (mobSpawningLocations == null || mobSpawningLocations.isEmpty()) {
            throw new IllegalArgumentException("Invalid Access: Room has no mob spawning locations");
        }
        return mobSpawningLocations.get((int)(Math.random() * mobSpawningLocations.size()));
    }

    /**
     * Returns the target mob spawn location for this room.
     */
    public Location getTargetSpawnLocation() {
        return targetSpawnLocation;
    }

    /**
     * Given a location inside the room, returns the corresponding location in the specified cave.
     */
    public Location getLocationInCave(Cave cave, Location location) {
        return cave.getRoomLocationInCave(this, location);
    }

    /**
     * Returns the player's spawn location for this room.
     */
    public Location getSpawnLocation() {
        return playerSpawn;
    }

    // Convenience getters for coordinates of room bounds and spawn locations

    public double getStartX() {
        return start.getX();
    }

    public double getStartY() {
        return start.getY();
    }

    public double getStartZ() {
        return start.getZ();
    }

    public double getSpawnX() {
        return playerSpawn.getX();
    }

    public double getSpawnY() {
        return playerSpawn.getY();
    }

    public double getSpawnZ() {
        return playerSpawn.getZ();
    }

    public double getEndX() {
        return end.getX();
    }

    public double getEndY() {
        return end.getY();
    }

    public double getEndZ() {
        return end.getZ();
    }

    /**
     * Gets the start location of the escape region.
     */
    public Location getEscapeRegionStart() {
        return escapeRegionStart;
    }

    /**
     * Gets the end location of the escape region.
     */
    public Location getEscapeRegionEnd() {
        return escapeRegionEnd;
    }

    /**
     * Marks the room as cleared.
     */
    public void setCleared() {
        cleared = true;
    }

    /**
     * Returns true if the room has been cleared.
     */
    public boolean isCleared() {
        return cleared;
    }

    /**
     * Renders (copies) the room blocks into the specified cave region for a player,
     * effectively teleporting the player to the room's spawn location inside the cave.
     */
    public void render(Cave cave, Player player) {
        int startX = (int) start.getX();
        int endX = (int) end.getX();
        int startY = (int) start.getY();
        int endY = (int) end.getY();
        int startZ = (int) start.getZ();
        int endZ = (int) end.getZ();

        Location caveStart = cave.getStart();
        BlockData blockData;
        Location curLocation;
        Location curCaveLocation;

        // Copy each block's data from the original room location into the cave's target location
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    curLocation = new Location(world, x, y, z);
                    blockData = curLocation.getBlock().getBlockData();

                    curCaveLocation = new Location(world, caveStart.getX() + x - startX,
                            caveStart.getY() + y - startY, caveStart.getZ() + z - startZ);
                    curCaveLocation.getBlock().setBlockData(blockData);
                }
            }
        }

        // Spawn portal teleport text displays in the cave
        renderTeleportDisplays(cave);

        // If the room is cleared, notify the player with a title
        if (isCleared()) {
            TitleSender.sendTitle("You Have Cleared This Room", "Move To Another Room", player,
                    1000, 3000, 1000,
                    NamedTextColor.BLUE, NamedTextColor.DARK_BLUE);
        }

        // Teleport the player to the cleared spawn location if defined, else to default spawn location
        if (isCleared() && roomClearedPlayerSpawn != null) {
            player.teleport(cave.getRoomLocationInCave(this, roomClearedPlayerSpawn));
        } else {
            Location playerStartingLoc = new Location(world, caveStart.getX() + playerSpawn.getX() - startX,
                    caveStart.getY() + playerSpawn.getY() - startY, caveStart.getZ() + playerSpawn.getZ() - startZ);
            player.teleport(playerStartingLoc);
        }

        System.out.println("finished rendering!");
    }

    /**
     * Renders text displays (floating text) for all portal locations in the cave.
     */
    public void renderTeleportDisplays(Cave cave) {
        if (northPortalLocation == null) {
            return;
        }
        spawnTextDisplay(Component.text("Teleport: North").color(NamedTextColor.LIGHT_PURPLE), cave.getRoomLocationInCave(this, northPortalLocation));
        spawnTextDisplay(Component.text("Teleport: NorthWest").color(NamedTextColor.LIGHT_PURPLE), cave.getRoomLocationInCave(this, northWestPortalLocation));
        spawnTextDisplay(Component.text("Teleport: NorthEast").color(NamedTextColor.LIGHT_PURPLE), cave.getRoomLocationInCave(this, northEastPortalLocation));
        spawnTextDisplay(Component.text("Teleport: South").color(NamedTextColor.LIGHT_PURPLE), cave.getRoomLocationInCave(this, southPortalLocation));
        spawnTextDisplay(Component.text("Teleport: SouthWest").color(NamedTextColor.LIGHT_PURPLE), cave.getRoomLocationInCave(this, southWestPortalLocation));
        spawnTextDisplay(Component.text("Teleport: SouthEast").color(NamedTextColor.LIGHT_PURPLE), cave.getRoomLocationInCave(this, southEastPortalLocation));
    }

    /**
     * Spawns a floating text display at a location with the given text.
     */
    private void spawnTextDisplay(Component text, Location location) {
        TextDisplay textDisplay = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        textDisplay.setText(text);
        textDisplay.setBillboard(TextDisplay.TextDisplayBillboard.CENTER);
        textDisplay.setShadowed(true);
        textDisplay.setBrightness(15);
        textDisplay.setRotation(location.getYaw(), location.getPitch());
    }
}
