package Rooms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

// the room storage is responsible for storing all the rooms that are built in hunt the wumpus.
public class RoomStorage {
    private static ArrayList<Room> rooms;
    private static World world = Bukkit.getWorld("world");
    private static Room triviaRoom;
    private static Location lobbyLocation;
    private static Room wumpusRoom;
    public static int[] utilityRoomIndexes = {1, 2, 7}; // each utility room can only appear once
    public static void initRoomStorage () { // fully configured, later, can be moved to a configuration file
        rooms = new ArrayList<>();
        Room curRoom;

        // close combat room
//         rooms.add(new Room(new Location(world, 100, 255, 100), new Location(world,150, 270, 150),
//                new Location(world, 119, 257, 123)));
//
//         rooms.get(rooms.size() - 1).addKillTask(4);

         // sewer room : 0
        rooms.add(new Room(new Location(world, -160, 240, -160), new Location(world, -90, 280, -90),
                new Location(world, -121.5, 258, -124.5)));
        curRoom = rooms.get(rooms.size() - 1);
        curRoom.addKillTask(4);
        curRoom.addSurviveTask(500);
        curRoom.addTargetTask();

        curRoom.setNorthPortalLocation(new Location(world, -101, 260, -127, 90, 0));
        curRoom.setNorthWestPortalLocation(new Location(world, -109, 260, -148, 0, 0));
        curRoom.setNorthEastPortalLocation(new Location(world, -109, 260, -101, -180, 0));
        curRoom.setSouthPortalLocation(new Location(world, -148, 260, -127, -90, 0));
        curRoom.setSouthWestPortalLocation(new Location(world, -143, 260, -148, 0, 0));
        curRoom.setSouthEastPortalLocation(new Location(world, -143, 260, -101, -180, 0));

        //chase room : 1
        rooms.add(new Room(new Location(world, 295, 284, 295), new Location(world, 315, 315, 405),
                new Location(world, 306.5, 296, 392.5), new Location(world, 295, 300, 295), new Location(world, 315, 315, 405)));
        curRoom = rooms.get(rooms.size() - 1);

        curRoom.addEscapeTask(1600, new Location(world, 295, 300, 295), new Location(world, 315, 315, 405));

        curRoom.setNorthPortalLocation(new Location(world, 308, 303, 305, 90, 0));
        curRoom.setNorthWestPortalLocation(new Location(world, 308, 303, 316, 90, 0));
        curRoom.setNorthEastPortalLocation(new Location(world, 308, 303, 325, 90, 0));
        curRoom.setSouthPortalLocation(new Location(world, 302, 303, 305, -90, 0));
        curRoom.setSouthWestPortalLocation(new Location(world, 302, 303, 316, -90, 0));
        curRoom.setSouthEastPortalLocation(new Location(world, 302, 303, 325, -90, 0));

        curRoom.setRoomClearedPlayerSpawn(new Location(world, 305, 301, 303));

        // parkour room : 2
       rooms.add(new Room(new Location(world, 235, 290, 135), new Location(world, 355, 310, 175), // parkour room
                new Location(world, 251.5, 300, 155.5)));
       curRoom = rooms.get(rooms.size() - 1);
       curRoom.addEscapeTask(3600, new Location(world, 331, 290, 135), new Location(world, 355, 310, 175));

       curRoom.setNorthPortalLocation(new Location(world, 345, 298, 155, 90, 0));
       curRoom.setNorthWestPortalLocation(new Location(world, 344, 298, 144, 0, 0));
       curRoom.setNorthEastPortalLocation(new Location(world, 343, 298, 167, -180, 0));
       curRoom.setSouthPortalLocation(new Location(world, 339, 298, 156, -90, 0));
       curRoom.setSouthWestPortalLocation(new Location(world, 334, 298, 144, 0, 0));
       curRoom.setSouthEastPortalLocation(new Location(world, 334, 298, 167, -180, 0));

       curRoom.setRoomClearedPlayerSpawn(new Location(world, 341, 296, 155));

        // sculk room : 3
        rooms.add(new Room(new Location(world, 195, 290, 95), new Location(world, 245, 310, 145),
                new Location(world, 220.5, 296, 120.5)));
        curRoom = rooms.get(rooms.size() - 1);
        curRoom.addSurviveTask(500);
        curRoom.addKillTask(3);
        curRoom.addTargetTask();

        curRoom.setNorthPortalLocation(new Location(world, 236, 299, 120.5, 90, 0));
        curRoom.setNorthWestPortalLocation(new Location(world, 229.5, 299, 105, 0, 0));
        curRoom.setNorthEastPortalLocation(new Location(world, 229.5, 299, 136, -180, 0));
        curRoom.setSouthPortalLocation(new Location(world, 205, 299, 120.5, -90, 0));
        curRoom.setSouthWestPortalLocation(new Location(world, 215.5, 299, 105, 0, 0));
        curRoom.setSouthEastPortalLocation(new Location(world, 214.5, 299, 136, -180, 0));


        // skull room : 4
        rooms.add(new Room(new Location(world, 55, 280, 200), new Location(world, 110, 305, 260),
                new Location(world, 72.5, 288, 234.5)));
        curRoom = rooms.get(rooms.size() - 1);
        curRoom.addSurviveTask(500);
        curRoom.addKillTask(3);
        curRoom.addTargetTask();

        curRoom.setNorthPortalLocation(new Location(world, 100, 289, 239, 90, 0));
        curRoom.setNorthWestPortalLocation(new Location(world, 90, 289, 216, 0, 0));
        curRoom.setNorthEastPortalLocation(new Location(world, 93, 289, 251, -180, 0));
        curRoom.setSouthPortalLocation(new Location(world, 64, 289, 234, -90, 0));
        curRoom.setSouthWestPortalLocation(new Location(world, 72, 289, 216, 0, 0));
        curRoom.setSouthEastPortalLocation(new Location(world, 74, 289, 251, -180, 0));


        // quartz room : 5
        rooms.add(new Room(new Location(world, 200, 300, 400), new Location(world, 260, 325, 460),
                new Location(world, 229.5, 304, 431.5)));
        curRoom = rooms.get(rooms.size() - 1);

        curRoom.addSurviveTask(500);
        curRoom.addKillTask(4);
        curRoom.addTargetTask();

        curRoom.setNorthPortalLocation(new Location(world, 249, 306, 426, 90, 0));
        curRoom.setNorthWestPortalLocation(new Location(world, 242, 306, 414, 0, 0));
        curRoom.setNorthEastPortalLocation(new Location(world, 242, 306, 449, -180, 0));
        curRoom.setSouthPortalLocation(new Location(world, 214, 306, 431, -90, 0));
        curRoom.setSouthWestPortalLocation(new Location(world, 223, 306, 414, 0, 0));
        curRoom.setSouthEastPortalLocation(new Location(world, 223, 306, 449, -180, 0));



        // blackstone room : 6

        rooms.add(new Room(new Location(world, 335, 295, 195), new Location(world, 404, 325, 275),
                new Location(world, 363.5, 302, 228.5)));

        curRoom = rooms.get(rooms.size() - 1);

        curRoom.setNorthPortalLocation(new Location(world, 384, 304, 223, 90, 0));
        curRoom.setNorthWestPortalLocation(new Location(world, 375, 304, 217, 0, 0));
        curRoom.setNorthEastPortalLocation(new Location(world, 375, 304, 240, -180, 0));
        curRoom.setSouthPortalLocation(new Location(world, 347, 304, 228, -90, 0));
        curRoom.setSouthWestPortalLocation(new Location(world, 358, 304, 217, 0, 0));
        curRoom.setSouthEastPortalLocation(new Location(world, 358, 304, 240, -180, 0));

        curRoom.addSurviveTask(500);
        curRoom.addKillTask(3);
        curRoom.addTargetTask();


        // EscapePuzzleRoom : 7
        rooms.add(new EscapePuzzleRoom(new Location(world, 295, 290, 195), new Location(world, 325, 310, 230),
                new Location(world, 310.5, 296, 202.5), new Location(world, 305, 295, 206), new Location(world, 314, 295, 215))); // EscapePuzzleRoom
        curRoom = rooms.get(rooms.size() - 1);

        curRoom.setNorthPortalLocation(new Location(world, 318, 298, 209, 90, 0));
        curRoom.setNorthWestPortalLocation(new Location(world, 317, 298, 203, 0, 0));
        curRoom.setNorthEastPortalLocation(new Location(world, 317, 298, 218, -180, 0));
        curRoom.setSouthPortalLocation(new Location(world, 303, 298, 209, -90, 0));
        curRoom.setSouthWestPortalLocation(new Location(world, 306, 298, 203, 0, 0));
        curRoom.setSouthEastPortalLocation(new Location(world, 306, 298, 218, -180, 0));


        triviaRoom = new Room(new Location(world, 120, 253, 75), new Location(world, 155, 270, 105),
                new Location(world, 137.5, 261, 87.5));


        wumpusRoom = new Room(new Location(world,200, 250, 200), new Location(world, 250, 310, 250),
                new Location(world, 225.5, 284, 225.5));


        lobbyLocation = new Location(world, 0.5, 103, 0.5);
    }
    public static Room getRandomRoom () {
        return rooms.get((int)(Math.random() * rooms.size())).copyRoom();
    }

    public static int getRoomCount() {
        return rooms.size();
    }
    public static Room getRandomRoom (boolean[] isRoomSelected) { // gets a random room
        if (isRoomSelected.length != rooms.size()) {
            throw new IllegalArgumentException("Invalid Randomization: visited room length not equal to the number of available rooms");
        }
        int selectedIndex;
        do {
            selectedIndex = (int)(Math.random() * rooms.size());
        } while (isRoomSelected[selectedIndex]);

        for (int i : utilityRoomIndexes) {
            if (selectedIndex == i) {
                isRoomSelected[i] = true;

                break;
            }
        }

        return rooms.get(selectedIndex).copyRoom();
    }
    public static Room getTriviaRoom () {
        return triviaRoom;
    }
    public static Location getLobbyLocation () {
        return lobbyLocation;
    }
    public static Room getWumpusRoom () {
        return wumpusRoom;
    }
}
