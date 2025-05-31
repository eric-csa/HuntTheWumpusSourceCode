package Caves;

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
    private Location start;
    private Location end;
    private Room curRoom;
    private int caveMap[][];
    private int deg[]; // degree is the degree of the current node, which stores the number of edges this node has
    private int parent[] = new int[31]; // the parent of the current node in the tree
    private Room[] rooms = new Room[31];
    private final int maxEdges = 29;
    private boolean[] visited;
    private boolean[] isRoomSelected;
    // constructs the cave. Including: Finding the default starting room, and the map.
    public Cave (Location topLeft, Location bottomRight) {
        this.start = topLeft;
        this.end = bottomRight;
        //curRoom = RoomStorage.getRandomRoom();
        caveMap = new int[31][6];
        deg = new int[31];
        visited = new boolean[31];
        for (int i = 0; i < caveMap.length; i++) {
            for (int j = 0; j < caveMap[0].length; j++) {
                caveMap[i][j] = -1;
            }
        }
        isRoomSelected = new boolean[RoomStorage.getRoomCount()];
        for (int i = 1; i <= 30; i++) {
            rooms[i] = RoomStorage.getRandomRoom(isRoomSelected);
        }
        generateCave();
    }
    // changes the current room of the player to the new room.
    public Room getCurRoom () {
        return curRoom;
    }
    public Room getRoom (int roomNum) {
        return rooms[roomNum];
    }
    public Location getRoomLocationInCave (Room room, Location location) { // important method. Map a specific location in a room to that respective location in the cave.
        return new Location(world, start.getX() + location.getX() - room.getStartX(),
                start.getY() + location.getY() - room.getStartY(), start.getZ() + location.getZ() - room.getStartZ(),
                location.getYaw(), location.getPitch());
    }
    public Location getCurrentRoomSpawnLocation () {
        return getRoomLocationInCave(curRoom, curRoom.getSpawnLocation());
//        return new Location(world, start.getX() + curRoom.getSpawnX() - curRoom.getStartX(),
//                start.getY() + curRoom.getSpawnY() - curRoom.getStartY(), start.getZ() + curRoom.getSpawnZ() - curRoom.getStartZ());
    }
    public void setCurRoom (int playerRoom) {
        curRoom = rooms[playerRoom];
    }
    public void setCurRoom (Room newRoom) {
        curRoom = newRoom;
    }
    public void addEdgeToRoom (boolean[] visited, int curRoom, Queue<Integer> nextLayer) {
        int curAdjRoom;
        int curDir;
        do {
            curAdjRoom = (int)(Math.random() * 30) + 1;
        } while (visited[curAdjRoom] || deg[curAdjRoom] >= 3 || curAdjRoom == curRoom); // finds a not visited room, not the same as the current room.

        do {
            curDir = (int)(Math.random() * 6);
        } while(caveMap[curRoom][curDir] != -1 || caveMap[curAdjRoom][oppDir(curDir)] != -1); // finds a candidate direction for the room connection

        caveMap[curRoom][curDir] = curAdjRoom;
        caveMap[curAdjRoom][oppDir(curDir)] = curRoom;

        deg[curRoom]++;
        deg[curAdjRoom]++;
        parent[curAdjRoom] = curRoom;
        nextLayer.add(curAdjRoom);

        visited[curRoom] = true;
        visited[curAdjRoom] = true;

        HuntTheWumpusPlugin.print("added " + curAdjRoom + " to " + curRoom);
    }
    // generates the cave. It randomly generates a tree data structure first, with each node having 1-3 edges, and then adds more rooms
    // to make sure that the cave wraps around.
    public void generateCave () {
        boolean[] visited = new boolean[31];
        Queue<Integer> nextLayer = new LinkedList<>(); // find the next node in the next layer that hasn't been processed

        nextLayer.add(1);
        int adjRooms;
        int curRoom = 0;
        int edges = 0;
        int tries = 0;
        while (!nextLayer.isEmpty() && edges < maxEdges) { // keeps going until the tree is fully generated
            curRoom = nextLayer.remove();

            adjRooms = (int)(Math.random() * 3) + 1; // 1-3 adj rooms

            if (adjRooms + deg[curRoom] > 3) {
                adjRooms = 3 - deg[curRoom];
            }

            edges += adjRooms;

            if (edges > maxEdges) { //if an edge generation causes > 29 edges, delete the extra edges
                adjRooms -= edges - maxEdges;
                edges = maxEdges;
            }
            for (int i = 0; i < adjRooms; i++) {
                addEdgeToRoom(visited, curRoom, nextLayer);
            }
        }
        for (int i = 1; i <= 30; i++) {
            if (deg[i] < 2 || deg[i] > 3) {
                addEdgeToRoom(new boolean[31], i, nextLayer); // add edges so each room has at least 2 adjacent rooms
            }
            if (deg[i] < 2 || deg[i] > 3) {
                HuntTheWumpusPlugin.print("Invalid room count: room " + i + " has " + deg[i] + " rooms");
            }
            else {
                System.out.print(i + " :"); // prints out generated tree

                for (int j = 0; j < 6; j++) {
                    if (caveMap[i][j] != -1) {
                        System.out.print(caveMap[i][j] + " ");
                    }
                }
                System.out.println();
            }
        }
        System.out.println(findComponentSize(1));
    }
    public int[][] getCaveMap () {
        return caveMap;
    }
    public int findComponentSize(int curNode) { // finds the size of connected component. Checks if all the rooms are connected.
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
    private int oppDir(int d) { // opposite direction for room connection. for example, opposite of north is south
        switch (d) {
            case 0:
                return 5;
            case 1:
                return 4;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 1;
            case 5:
                return 0;
        };

        return -1;
    }
    public Location getStart() {
        return start;
    }
    public Location getEnd() {
        return end;
    }
    public boolean isLocationInCave (Location location) {
        return location.getX() >= start.getX() && location.getX() <= end.getX() &&
                location.getY() >= start.getY() && location.getY() <= end.getY() &&
                location.getZ() >= start.getZ() && location.getZ() <= end.getZ();
    }
    // renders the new room for the player into the game.
    public void renderRoom (Player player) {
        killMobs();
        clearTextDisplays();
        player.setHealth(20);
        curRoom.render(this, player);
        if (ServerData.getPlayerData(player).getGame() != null && ServerData.getPlayerData(player).getGame().playerInMinigame() ||
                curRoom.isCleared()) {

            return;
        }
       // curRoom.setEscapeTask(player, getRoomLocationInCave(curRoom, curRoom.getEscapeRegionStart()),
       //         getRoomLocationInCave(curRoom, curRoom.getEscapeRegionEnd()), 150);
//        if (curRoom instanceof EscapePuzzleRoom) {
//            EscapePuzzleRoom escapePuzzleRoom = (EscapePuzzleRoom) curRoom;
//            escapePuzzleRoom.setEscapePuzzleTask(player, getRoomLocationInCave(escapePuzzleRoom, escapePuzzleRoom.getGridStart()), getRoomLocationInCave(escapePuzzleRoom, escapePuzzleRoom.getGridEnd()));
//            return;
//        }
//        curRoom.setSurviveTask(player, 200);
        setRoomTask(player);
    }
    public void renderWumpusRoom (Player player) {
        killMobs();
        clearTextDisplays();
        curRoom = RoomStorage.getWumpusRoom();
        curRoom.render(this, player);
    }
    // set room task sets the room task for a player in a certain cave.
    public void setRoomTask (Player player) {
        if (curRoom instanceof EscapePuzzleRoom) {
            EscapePuzzleRoom escapePuzzleRoom = (EscapePuzzleRoom) curRoom;
            escapePuzzleRoom.setEscapePuzzleTask(player, getRoomLocationInCave(escapePuzzleRoom, escapePuzzleRoom.getGridStart()), getRoomLocationInCave(escapePuzzleRoom, escapePuzzleRoom.getGridEnd()));
            return;
        }
        RoomTaskInfo curRoomTask = curRoom.getRandomRoomTaskInfo();

        if (curRoomTask instanceof EscapeTaskInfo escapeTaskInfo) {
            curRoom.setEscapeTask(player, getRoomLocationInCave(curRoom, escapeTaskInfo.getEscapeRegionStart()),
                    getRoomLocationInCave(curRoom, escapeTaskInfo.getEscapeRegionEnd()), escapeTaskInfo.getEscapeTime());
            return;
        }
        if (curRoomTask instanceof KillTaskInfo killTaskInfo) {
            curRoom.setKillTask(player, killTaskInfo.getKillsRequired());
            return;
        }
        if (curRoomTask instanceof TargetTaskInfo targetTaskInfo) {
            curRoom.setTargetTask(player, WumpusMobManager.getWumpusMob(targetTaskInfo.getTargetMobName()), getRoomLocationInCave(curRoom, curRoom.getTargetSpawnLocation()));
            return;
        }
        if (curRoomTask instanceof SurviveTaskInfo surviveTaskInfo) {
            curRoom.setSurviveTask(player, surviveTaskInfo.getSurviveTime());
            return;
        }
        throw new IllegalArgumentException("Invalid Room Task Type: curRoom.getRandomRoomTaskInfo() did not return a valid room task type");
    }
    public void killMobs () {
        for (LivingEntity entity : world.getNearbyLivingEntities(start, 300)) {
            if (!(entity instanceof Player)) {
                entity.damage(entity.getHealth() + 200);
            }
        }
    }
    public void clearTextDisplays () {
        for (Entity entity : world.getNearbyEntities(start, 150, 150, 150)) {
            if (entity instanceof TextDisplay textDisplay) {
                textDisplay.remove();
            }
        }
    }
}
