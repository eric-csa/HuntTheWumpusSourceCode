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
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

// this Object stores a custom room. All the rooms store critical information about spawn points, and key landmarks within a room.
// the room object also has a render method, which will copy-paste the room that is built in one part of the world into the current
// cave the player is in.
// mandatory information in the room: start,and end, the starting and ending minecraft coordinates of the cube, where the room is built
// playerSpawn, where the player is supposed to spawn upon entering the room
// optional room information: What types of monsters can spawn in the room
// locations of monsters
// locations of chests
// locations of mini-games / secrets
// location of secret trivia answers
public class Room {
    protected Location start;
    protected Location end;
    protected World world = Bukkit.getWorld("world");
    protected Location playerSpawn;
    protected Location roomClearedPlayerSpawn;
    protected boolean cleared;
    protected Player player;
    protected String taskType;
    protected RoomTask task;
    protected Location escapeRegionStart;
    protected Location escapeRegionEnd;
    protected int escapeTime;
    protected ArrayList<Location> mobSpawningLocations;
    protected Location targetSpawnLocation;
    protected String roomName;
    private ArrayList<RoomTaskInfo> possibleRoomTasks;

    protected Location northPortalLocation;
    protected Location northWestPortalLocation;
    protected Location northEastPortalLocation;
    protected Location southPortalLocation;
    protected Location southWestPortalLocation;
    protected Location southEastPortalLocation;

    public Room (Location start, Location end, Location playerSpawn) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        possibleRoomTasks = new ArrayList<>();
        setMobSpawningLocations();
    }
    public Room (Location start, Location end, Location playerSpawn, String taskType) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        this.taskType = taskType;
        possibleRoomTasks = new ArrayList<>();
        setMobSpawningLocations();
    }
    public Room (Location start, Location end, Location playerSpawn, Location escapeRegionStart, Location escapeRegionEnd) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        this.escapeRegionEnd = escapeRegionEnd;
        this.escapeRegionStart = escapeRegionStart;
        possibleRoomTasks = new ArrayList<>();
        setMobSpawningLocations();
    }
    public Room (Location start, Location end, Location playerSpawn, ArrayList<RoomTaskInfo> possibleRoomTasks) {
        this.start = start;
        this.end = end;
        this.playerSpawn = playerSpawn;
        this.possibleRoomTasks = possibleRoomTasks;
        setMobSpawningLocations();
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public void setMobSpawningLocations () {
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
                    if (world.getBlockAt(x, y, z).getBlockData().getMaterial() == Material.INFESTED_DEEPSLATE) {
                        mobSpawningLocations.add(new Location(world, x + 0.5, y + 1, z + 0.5));
                    }
                    if (world.getBlockAt(x, y, z).getBlockData().getMaterial() == Material.TARGET) {
                        targetSpawnLocation = new Location(world, x + 0.5, y + 1, z + 0.5);
                    }
                }
            }
        }
    }
    // if a room has a different spawn location for a cleared room, set it here.
    public void setRoomClearedPlayerSpawn (Location roomClearedPlayerSpawn) {
        this.roomClearedPlayerSpawn = roomClearedPlayerSpawn;
    }
    public void setNorthPortalLocation (Location location) {
        northPortalLocation = location;
    }
    public void setNorthWestPortalLocation (Location location) {
        northWestPortalLocation = location;
    }
    public void setNorthEastPortalLocation (Location location) {
        northEastPortalLocation = location;
    }
    public void setSouthPortalLocation (Location location) {
        southPortalLocation = location;
    }
    public void setSouthWestPortalLocation (Location location) {
        southWestPortalLocation = location;
    }
    public void setSouthEastPortalLocation (Location location) {
        southEastPortalLocation = location;
    }
    public Room copyRoom () {
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
    public void setSurviveTask (Player player, int surviveTime) {
        task = new SurviveTask(this, player, surviveTime);
    }
    public void setKillTask (Player player, int killsRequired) {
        task = new KillTask(this, player, killsRequired);
    }
    public void setTargetTask (Player player, WumpusMob target, Location targetSpawnLocation) {
        task = new TargetTask(this, player, target, targetSpawnLocation);
    }
    public void setEscapeTask (Player player, Location escapeRegionStart, Location escapeRegionEnd, int escapeTime) {
        task = new EscapeTask(this, player, escapeRegionStart, escapeRegionEnd, escapeTime);
    }
    public void setPlayer (Player player) {
        this.player = player;
    }
    //TODO: add way to randomize / configure mobs better
    public void addEscapeTask (int escapeTime, Location escapeRegionStart, Location escapeRegionEnd) {
        possibleRoomTasks.add(new EscapeTaskInfo(escapeTime, escapeRegionStart, escapeRegionEnd));
    }
    public void addKillTask (int killsRequired) {
        possibleRoomTasks.add(new KillTaskInfo(killsRequired));
    }
    public void addSurviveTask (int surviveTime) {
        possibleRoomTasks.add(new SurviveTaskInfo(surviveTime));
    }
    public void addTargetTask () {
        if (targetSpawnLocation == null) {
            throw new IllegalArgumentException("Cannot create target task for this room since the room does not have a valid targetSpawnLocation");
        }
        possibleRoomTasks.add(new TargetTaskInfo(WumpusMobManager.getRandomWumpusMinibossName()));
    }
    public RoomTaskInfo getRandomRoomTaskInfo () {
        if (possibleRoomTasks == null || possibleRoomTasks.size() == 0) {
            throw new IllegalArgumentException("Invalid Room Task Initialization: Room does not have task info");
        }
        return possibleRoomTasks.get((int)(Math.random() * possibleRoomTasks.size()));
    }
    public String getTaskType () {
        return taskType;
    }
    public RoomTask getRoomTask () {
        return task;
    }
    public Location getStart () {
        return start;
    }
    public Location getEnd () {
        return end;
    }
    public boolean canSpawnMobs () {
        return mobSpawningLocations != null && !mobSpawningLocations.isEmpty();
    }
    public int getRoomSpawnLocationCount () {
        if (!canSpawnMobs()) {
            return 0;
        }
        return mobSpawningLocations.size();
    }
    public ArrayList<Location> getMobSpawningLocations () {
        ArrayList<Location> mobSpawningLocations = new ArrayList<>();
        mobSpawningLocations.addAll(this.mobSpawningLocations);

        ArrayList<Location> randomizedLocations = new ArrayList<>();
        while (!mobSpawningLocations.isEmpty()) {
            randomizedLocations.add(mobSpawningLocations.remove((int)(Math.random() * mobSpawningLocations.size())));
        }
        return randomizedLocations;
    }
    public Location getRandomMobSpawnLocation () {
        if (mobSpawningLocations == null || mobSpawningLocations.isEmpty()) {
            throw new IllegalArgumentException("Invalid Access: Room has no mob spawning locations");
        }
        return mobSpawningLocations.get((int)(Math.random() * mobSpawningLocations.size()));
    }
    public Location getTargetSpawnLocation () {
        return targetSpawnLocation;
    }
    public Location getLocationInCave (Cave cave, Location location) {
        return cave.getRoomLocationInCave(this, location);
    }
    public Location getSpawnLocation () {
        return playerSpawn;
    }
    public double getStartX () {
        return start.getX();
    }
    public double getStartY () {
        return start.getY();
    }
    public double getStartZ () {
        return start.getZ();
    }
    public double getSpawnX () {
        return playerSpawn.getX();
    }
    public double getSpawnY () {
        return playerSpawn.getY();
    }
    public double getSpawnZ () {
        return playerSpawn.getZ();
    }
    public double getEndX () {
        return end.getX();
    }
    public double getEndY () {
        return end.getY();
    }
    public double getEndZ () {
        return end.getZ();
    }

    public Location getEscapeRegionStart() {
        return escapeRegionStart;
    }
    public Location getEscapeRegionEnd() {
        return escapeRegionEnd;
    }

    public void setCleared () {
        cleared = true;
    }
    public boolean isCleared () {
        return cleared;
    }
    // this function renders the room into a specific cave for a player.
    public void render (Cave cave, Player player) {
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

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    curLocation = new Location(world, x, y, z);
                    blockData = curLocation.getBlock().getBlockData();

                    //System.out.println(blockData.getAsString());
                    curCaveLocation = new Location(world, caveStart.getX() + x - startX,
                            caveStart.getY() + y - startY, caveStart.getZ() + z - startZ);
                    curCaveLocation.getBlock().setBlockData(blockData);
                }
            }
        }
        renderTeleportDisplays(cave);
        if (isCleared()) {
            TitleSender.sendTitle("You Have Cleared This Room", "Move To Another Room", player,
                    1000, 3000, 1000,
                    NamedTextColor.BLUE, NamedTextColor.DARK_BLUE);
        }
        if (isCleared() && roomClearedPlayerSpawn != null) {
            player.teleport(cave.getRoomLocationInCave(this, roomClearedPlayerSpawn));
        }
        else {
            Location playerStartingLoc = new Location(world, caveStart.getX() + playerSpawn.getX() - startX,
                    caveStart.getY() + playerSpawn.getY() - startY, caveStart.getZ() + playerSpawn.getZ() - startZ);
            player.teleport(playerStartingLoc);
        }

        System.out.println("finished rendering!");
    }
    public void renderTeleportDisplays (Cave cave) {
        if (northPortalLocation == null) {
            return;
        }
        spawnTextDisplay(Component.text("Teleport: North").color(NamedTextColor.LIGHT_PURPLE), cave.getRoomLocationInCave(this, northPortalLocation));
        spawnTextDisplay(Component.text("Teleport: NorthWest").color(NamedTextColor.RED), cave.getRoomLocationInCave(this, northWestPortalLocation));
        spawnTextDisplay(Component.text("Teleport: NorthEast").color(NamedTextColor.BLUE), cave.getRoomLocationInCave(this, northEastPortalLocation));
        spawnTextDisplay(Component.text("Teleport: SouthWest").color(NamedTextColor.YELLOW), cave.getRoomLocationInCave(this, southWestPortalLocation));
        spawnTextDisplay(Component.text("Teleport: SouthEast").color(NamedTextColor.GREEN), cave.getRoomLocationInCave(this, southEastPortalLocation));
        spawnTextDisplay(Component.text("Teleport: South").color(NamedTextColor.GOLD), cave.getRoomLocationInCave(this, southPortalLocation));
    }
    public void spawnTextDisplay (Component text, Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Invalid Text Display: location is null");
        }
        TextDisplay textDisplay = (TextDisplay) world.spawnEntity(location, EntityType.TEXT_DISPLAY);
        textDisplay.setRotation(location.getYaw(), location.getPitch());
        HuntTheWumpusPlugin.print(textDisplay.getLocation().toString());

        textDisplay.text(text);

        textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);


        textDisplay.setBackgroundColor(Color.BLACK);
    }
}
