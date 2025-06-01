package Caves;

// Import statements for various game elements, entities, room types, and utility classes.
import Mobs.WumpusMobManager;
import RoomTasks.*;
import Rooms.EscapePuzzleRoom;
import Rooms.Room;
import Rooms.RoomStorage;
import ServerData.ServerData;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.LinkedList;
import java.util.Queue;

import static Caves.CaveManager.world;

public class Cave {
    // Coordinates of the cave’s boundaries
    private Location start;
    private Location end;

    // Current room the player is in
    private Room curRoom;

    // Graph representation of cave: room connections (6 possible directions per room)
    private int caveMap[][];

    // Stores number of edges (connections) each room has
    private int deg[];

    // Tree parent for each room (used during cave generation)
    private int parent[] = new int[31];

    // Array storing 30 generated rooms (indexed from 1 to 30)
    private Room[] rooms = new Room[31];

    // Max number of edges to form a sparse graph-like cave
    private final int maxEdges = 29;

    // Arrays for visitation tracking and random room selection
    private boolean[] visited;
    private boolean[] isRoomSelected;

    // Constructor - generates the entire cave between two given coordinates
    public Cave (Location topLeft, Location bottomRight) {
        this.start = topLeft;
        this.end = bottomRight;

        caveMap = new int[31][6]; // Up to 6 directions per room
        deg = new int[31];
        visited = new boolean[31];

        // Initialize all connections as -1 (meaning no connection)
        for (int i = 0; i < caveMap.length; i++) {
            for (int j = 0; j < caveMap[0].length; j++) {
                caveMap[i][j] = -1;
            }
        }

        // Track rooms already selected to ensure uniqueness
        isRoomSelected = new boolean[RoomStorage.getRoomCount()];

        // Assign random rooms from storage
        for (int i = 1; i <= 30; i++) {
            rooms[i] = RoomStorage.getRandomRoom(isRoomSelected);
        }

        // Generate cave layout and room connections
        generateCave();
    }

    // Returns the current room the player is in
    public Room getCurRoom () {
        return curRoom;
    }

    // Returns the room object for a given room number
    public Room getRoom (int roomNum) {
        return rooms[roomNum];
    }

    // Maps a local room-relative location to a global cave-relative location
    public Location getRoomLocationInCave (Room room, Location location) {
        return new Location(world,
                start.getX() + location.getX() - room.getStartX(),
                start.getY() + location.getY() - room.getStartY(),
                start.getZ() + location.getZ() - room.getStartZ(),
                location.getYaw(),
                location.getPitch());
    }

    // Gets the spawn location of the current room in global cave coordinates
    public Location getCurrentRoomSpawnLocation () {
        return getRoomLocationInCave(curRoom, curRoom.getSpawnLocation());
    }

    // Updates the current room using a room number
    public void setCurRoom (int playerRoom) {
        curRoom = rooms[playerRoom];
    }

    // Updates the current room using a Room object
    public void setCurRoom (Room newRoom) {
        curRoom = newRoom;
    }

    // Adds an edge from the current room to a randomly selected adjacent room
    public void addEdgeToRoom (boolean[] visited, int curRoom, Queue<Integer> nextLayer) {
        int curAdjRoom;
        int curDir;

        // Find a valid unvisited room with less than 3 connections
        do {
            curAdjRoom = (int)(Math.random() * 30) + 1;
        } while (visited[curAdjRoom] || deg[curAdjRoom] >= 3 || curAdjRoom == curRoom);

        // Choose an available direction that isn't already connected
        do {
            curDir = (int)(Math.random() * 6);
        } while (caveMap[curRoom][curDir] != -1 || caveMap[curAdjRoom][oppDir(curDir)] != -1);

        // Set up bidirectional connection in the graph
        caveMap[curRoom][curDir] = curAdjRoom;
        caveMap[curAdjRoom][oppDir(curDir)] = curRoom;

        // Update degree and tree parent data
        deg[curRoom]++;
        deg[curAdjRoom]++;
        parent[curAdjRoom] = curRoom;

        // Queue the new room for future edge creation
        nextLayer.add(curAdjRoom);

        // Mark both rooms as visited
        visited[curRoom] = true;
        visited[curAdjRoom] = true;

        HuntTheWumpusPlugin.print("added " + curAdjRoom + " to " + curRoom);
    }

    // Generates the cave structure as a graph using a modified BFS approach
    public void generateCave () {
        boolean[] visited = new boolean[31];
        Queue<Integer> nextLayer = new LinkedList<>();

        nextLayer.add(1); // Start with room 1
        int adjRooms;
        int curRoom = 0;
        int edges = 0;

        // Continue generating edges until maxEdges is reached
        while (!nextLayer.isEmpty() && edges < maxEdges) {
            curRoom = nextLayer.remove();
            adjRooms = (int)(Math.random() * 3) + 1; // 1 to 3 adjacent rooms

            // Ensure total connections do not exceed limit
            if (adjRooms + deg[curRoom] > 3) {
                adjRooms = 3 - deg[curRoom];
            }

            edges += adjRooms;

            // Trim excess edges if limit exceeded
            if (edges > maxEdges) {
                adjRooms -= edges - maxEdges;
                edges = maxEdges;
            }

            for (int i = 0; i < adjRooms; i++) {
                addEdgeToRoom(visited, curRoom, nextLayer);
            }
        }

        // Ensure all rooms have at least 2 connections
        for (int i = 1; i <= 30; i++) {
            if (deg[i] < 2 || deg[i] > 3) {
                addEdgeToRoom(new boolean[31], i, nextLayer);
            }

            // Debug print to verify structure
            if (deg[i] < 2 || deg[i] > 3) {
                HuntTheWumpusPlugin.print("Invalid room count: room " + i + " has " + deg[i] + " rooms");
            } else {
                System.out.print(i + " :");
                for (int j = 0; j < 6; j++) {
                    if (caveMap[i][j] != -1) {
                        System.out.print(caveMap[i][j] + " ");
                    }
                }
                System.out.println();
            }
        }

        // Print total size of connected component
        System.out.println(findComponentSize(1));
    }

    // Returns the cave's room connection map
    public int[][] getCaveMap () {
        return caveMap;
    }

    // Recursively computes the size of the connected graph component
    public int findComponentSize(int curNode) {
        HuntTheWumpusPlugin.print("Visiting + " + curNode);
        visited[curNode] = true;
        int treeSize = 1;

        for (int i = 0; i < 6; i++) {
            if (caveMap[curNode][i] != -1 && !visited[caveMap[curNode][i]]) {
                treeSize += findComponentSize(caveMap[curNode][i]);
            }
        }

        return treeSize;
    }

    // Returns the opposite direction of a given direction (used for bi-directional graph)
    private int oppDir(int d) {
        switch (d) {
            case 0: return 5;
            case 1: return 4;
            case 2: return 3;
            case 3: return 2;
            case 4: return 1;
            case 5: return 0;
        }
        return -1; // Invalid input
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    // Checks whether a given location falls inside the cave bounds
    public boolean isLocationInCave (Location location) {
        return location.getX() >= start.getX() && location.getX() <= end.getX() &&
               location.getY() >= start.getY() && location.getY() <= end.getY() &&
               location.getZ() >= start.getZ() && location.getZ() <= end.getZ();
    }

    // Loads the current room into the player's world, setting their health and removing mobs/text
    public void renderRoom (Player player) {
        killMobs();
        clearTextDisplays();
        player.setHealth(20);
        curRoom.render(this, player);

        // Skip task if player is already in a minigame or room is completed
        if (ServerData.getPlayerData(player).getGame() != null &&
            ServerData.getPlayerData(player).getGame().playerInMinigame() ||
            curRoom.isCleared()) {
            return;
        }

        // Assign room task
        setRoomTask(player);
    }

    // Special rendering for the Wumpus room
    public void renderWumpusRoom (Player player) {
        killMobs();
        clearTextDisplays();
        curRoom = RoomStorage.getWumpusRoom();
        curRoom.render(this, player);
    }

    // Assigns a gameplay task for the current room
    public void setRoomTask (Player player) {
        if (curRoom instanceof EscapePuzzleRoom) {
            EscapePuzzleRoom escapePuzzleRoom = (EscapePuzzleRoom) curRoom;
            escapePuzzleRoom.setEscapePuzzleTask(
                player,
                getRoomLocationInCave(escapePuzzleRoom, escapePuzzleRoom.getGridStart()),
                getRoomLocationInCave(escapePuzzleRoom, escapePuzzleRoom.getGridEnd())
            );
            return;
        }

        RoomTaskInfo curRoomTask = curRoom.getRandomRoomTaskInfo();

        if (curRoomTask instanceof EscapeTaskInfo escapeTaskInfo) {
            curRoom.setEscapeTask(player,
                    getRoomLocationInCave(curRoom, escapeTaskInfo.getEscapeRegionStart()),
                    getRoomLocationInCave(curRoom, escapeTaskInfo.getEscapeRegionEnd()),
                    escapeTaskInfo.getEscapeTime());
            return;
        }

        if (curRoomTask instanceof KillTaskInfo killTaskInfo) {
            curRoom.setKillTask(player, killTaskInfo.getKillsRequired());
            return;
        }

        if (curRoomTask instanceof TargetTaskInfo targetTaskInfo) {
            curRoom.setTargetTask(player,
                    WumpusMobManager.getWumpusMob(targetTaskInfo.getTargetMobName()),
                    getRoomLocationInCave(curRoom, curRoom.getTargetSpawnLocation()));
            return;
        }

        if (curRoomTask instanceof SurviveTaskInfo surviveTaskInfo) {
            curRoom.setSurviveTask(player, surviveTaskInfo.getSurviveTime());
            return;
        }

        // Fail-safe for unhandled task types
        throw new IllegalArgumentException("Invalid Room Task Type: curRoom.getRandomRoomTaskInfo() did not return a valid room task type");
    }

    // Removes all mobs except players in a large radius from the cave origin
    public void killMobs () {
        for (LivingEntity entity : world.getNearbyLivingEntities(start, 300)) {
            if (!(entity instanceof Player)) {
                entity.damage(entity.getHealth() + 200); // Instantly kills mob
            }
        }
    }

    // Removes all text display entities (floating text) in a large radius
    public void clearTextDisplays () {
        for (Entity entity : world.getNearbyEntities(start, 150, 150, 150)) {
            if (entity instanceof TextDisplay textDisplay) {
                textDisplay.remove();
            }
        }
    }
}
