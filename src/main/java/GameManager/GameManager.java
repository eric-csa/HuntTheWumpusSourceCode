package GameManager;

import Caves.Cave;
import Caves.CaveManager;
import GUIs.ActionBar.ActionBarManager;
import GUIs.Inventory.InventoryManager;
import Mobs.Wumpus;
import RoomTasks.RoomTask;
import Titles.TitleColors;
import Titles.TitleSender;
import GameLocations.GameLocations;
import Rooms.RoomStorage;
import ServerData.ServerData;
import Trivia.Trivia;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static Caves.CaveManager.world;

// the main object that manages the overall flow of a HuntTheWumpus hunt. Coordinates all the other game objects
public class GameManager implements Listener {
    private static boolean triviaModeOn = false; //checks if trivia is enabled
    private static boolean wumpusToggleModeOn = true; // testing: this means that the wumpus can be shot from wherever
    private static int playerRoom; // stores player's current room
    private static int wumpusRoom; // stores wumpus's current room
    private static int[] batRooms = new int[5]; //stores the bat rooms
    private static int[] pitRooms = new int[3]; // stores the pit rooms
    private Player player; // stores the player object of the player who is currently playing
    private boolean inMinigame = false; // checks if the player is in a minigame
    private GameLocations gameLocations; // gameLocation game object
    private boolean[] isRoomRendered; // this array checks if a player has already completed that room and prevents the task from happening again.
    private Trivia curTrivia; // trivia game object
    private Cave cave; // cave object
    private Wumpus wumpus;
    // hard-coded cave, placeholder to test if gameplay works. Not used.
    private boolean wumpusNearby = false;
    private boolean pitNearby = false;
    private boolean batNearby = false;
    private boolean initialized;
    private BukkitTask actionBarUpdator;
    private final String[] directions = {"North", "NorthWest", "NorthEast", "SouthWest", "SouthEast", "South"};
    static int[][] rooms = {
            {20, 1, 11, 10, 9, 20},
            {21, 22, 2, 11, 10, 30},
            {22, 3, 13, 12, 11, 1},
            {23, 24, 4, 13, 2, 22},
            {24, 5, 15, 14, 13, 3},
            {25, 26, 6, 15, 4, 24},
            {26, 7, 17, 16, 15, 5},
            {27, 28, 8, 17, 6, 26},
            {28, 9, 19, 18, 17, 7},
            {29, 30, 10, 19, 8, 28},
            {30, 1, 11, 20, 19, 9},
            {1, 2, 12, 21, 20, 10},
            {2, 13, 23, 22, 21, 11},
            {3, 4, 14, 23, 12, 2},
            {4, 15, 25, 24, 23, 13},
            {5, 6, 16, 25, 14, 4},
            {6, 17, 27, 26, 25, 15},
            {7, 8, 18, 27, 16, 6},
            {8, 19, 29, 28, 27, 17},
            {9, 10, 20, 29, 18, 8},
            {10, 11, 21, 30, 29, 19},
            {11, 12, 22, 1, 30, 20},
            {12, 23, 3, 2, 1, 21},
            {13, 14, 24, 3, 22, 12},
            {14, 25, 5, 4, 3, 23},
            {15, 16, 26, 5, 24, 14},
            {16, 27, 7, 6, 5, 25},
            {17, 18, 28, 7, 26, 16},
            {18, 29, 9, 8, 7, 27},
            {19, 20, 30, 9, 28, 18},
            {20, 21, 1, 10, 9, 29}
    };


    static int c = 0;
    // two colors which messages will be sent in.
    static NamedTextColor[] allColors = {
            NamedTextColor.DARK_AQUA,
            NamedTextColor.BLUE,
    };
    // the constructor will start the game and initialize data.
    public GameManager (Player player) {
        this.player = player;
        this.cave = CaveManager.createCave(player);
        initialized = false;
        isRoomRendered = new boolean[31];
        startGame(player, cave);
        //cave.setCurRoom(RoomStorage.getRandomRoom());
        gameLocations = new GameLocations(player, playerRoom, wumpusRoom);
        inMinigame = false;


    }
    // this function updates the player data to the new player instance when the said player logs out and rejoins in the middle of a hunt.
    public void updatePlayer (Player player) {
        this.player = player;

        gameLocations.updatePlayer(player);

        RoomTask curPlayerRoomTask = cave.getCurRoom().getRoomTask();

        if (curPlayerRoomTask != null) {
            curPlayerRoomTask.updatePlayer(player);
        }

        player.sendMessage(Component.text("welcome back to your hunt. This hunt will close if it is not finished within 7 real life days")
                .color(NamedTextColor.DARK_AQUA));
    }
    public Cave getCave () {
        return cave;
    }
    public Material getPlayerBlock () {
        return gameLocations.getPlayerBlock();
    }
    public int getPlayerRoom () {
        return playerRoom;
    }
    public int[] getAdjPlayerRooms () {
        return rooms[playerRoom];
    }

    public void sendMessage(int amount, String message) {
        c = (c + amount) % allColors.length;
        Component newMessage = Component.text(message).color(allColors[c]);
        player.sendMessage(newMessage);
    }
    public boolean playerInMinigame () {
        return inMinigame;
    }

    public static int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == -1) {
                continue;
            }
            if (array[i] == value) {
                return i; // Return the index when found
            }
        }
        return -1; // Return -1 if not found
    }

    public int[] compareArray(int[] array1, int[] array2) {
        int[] indices = new int[2];
        player.sendMessage(Arrays.toString(array1));
        player.sendMessage(Arrays.toString(array2));
        for (int i = 0; i < array1.length; i++) {
            for (int j = 0; j < array2.length; j++){
                if (array1[i] == -1 || array2[j] == -1) {
                    continue;
                }
                if (array1[i] == array2[j]) {
                    indices[0] = i;
                    indices[1] = j;
                    return indices; // Return the index when found
                }
            }
        }
        indices[0] = -1;
        indices[1] = -1;
        return indices; // Return -1 if not found
    }
    // this function randomly chooses the starting rooms, and all the hazard rooms.
    public void chooseRooms() {
        int[] randomRooms = new int[10];
        for(int i=0; i<10; i++){
            int newRandomRoom = (int) (Math.random() * 30 + 1);
            while (indexOf(randomRooms, newRandomRoom) != -1){
                newRandomRoom = (int) (Math.random() * 30 + 1);
            }
            randomRooms[i] = newRandomRoom;
        }
        playerRoom = randomRooms[0];
        wumpusRoom = randomRooms[1];
        batRooms[0] = randomRooms[2];
        batRooms[1] = randomRooms[3];
        batRooms[2] = randomRooms[4];
        batRooms[3] = randomRooms[5];
        batRooms[4] = randomRooms[6];
        pitRooms[0] = randomRooms[7];
        pitRooms[1] = randomRooms[8];
        pitRooms[2] = randomRooms[9];

        player.sendMessage("batRooms: " + Arrays.toString(batRooms));
        player.sendMessage("pitRooms: " + Arrays.toString(pitRooms));
        player.sendMessage("wumpusRoom: " + wumpusRoom);
        player.sendMessage("playerRoom: " + playerRoom);
        ServerData.getPlayerData(player).updateRoom(playerRoom);
    }
    public int findDistanceFromPlayerToWumpus () {
        LinkedList<Integer> nextRooms = new LinkedList<>();
        ArrayList<Integer> nextDepth = new ArrayList<>();
        int curDistance = 0;
        nextRooms.add(playerRoom);
        boolean[] visited = new boolean[rooms.length];

        while (!nextRooms.isEmpty()) {
            int curRoom = nextRooms.remove();

            visited[curRoom] = true;

            if (curRoom == wumpusRoom) {
                return curDistance;
            }

            for (int i = 0; i < 6; i++) {
                if (rooms[curRoom][i] > 0 && !visited[rooms[curRoom][i]]) {
                    nextDepth.add(rooms[curRoom][i]);
                }
            }

            if (nextRooms.isEmpty()) {
                curDistance++;

                nextRooms.addAll(nextDepth);

                nextDepth.clear();
            }
        }
        return -1;
    }
    public void notifyPossibleRooms () {
        for (int i = 0; i < 6; i++) {
            if (rooms[playerRoom][i] != -1) {
                player.sendMessage("You can move " + ChatColor.GREEN + directions[i]);
            }
        }

    }
    // moves the player in a certain direction.
    public void movePlayer(int dir) {
        if (!cave.getCurRoom().isCleared()) {
            player.sendMessage(Component.text("You have not cleared this room yet.").color(NamedTextColor.DARK_RED));
            return;
        }
        if (rooms[playerRoom][dir] == -1) {
            player.sendMessage(Component.text("There is no room in that direction.").color(NamedTextColor.DARK_RED));

            notifyPossibleRooms();
            return;
        }
        player.setHealth(20);
        int ogPlayer = playerRoom;
        int ogWumpus = wumpusRoom;
        playerRoom = rooms[playerRoom][dir];
        moveWumpus();
        playerCheck(ogPlayer, ogWumpus);
        if (!inMinigame) {
            cave.setCurRoom(playerRoom);
            //cave.setCurRoom(RoomStorage.getRandomRoom());
            cave.renderRoom(player);
        }
        //ServerData.getPlayerData(player).updateScore(100);
        ServerData.getPlayerData(player).updateRoom(playerRoom);
    }
    // moves the player after they complete trivia.
    public void movePlayerAfterTrivia () {
        inMinigame = false;
        cave.setCurRoom(playerRoom);
        //cave.setCurRoom(RoomStorage.getRandomRoom());
        cave.renderRoom(player);
    }
    public void checkForWumpusHit(int dir) {
        System.out.println(rooms[playerRoom][dir]);
        if (rooms[playerRoom][dir] == wumpusRoom || wumpusToggleModeOn) {
            player.sendMessage(Component.text("You successfully hit the wumpus...").color(NamedTextColor.RED));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 4));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, -1, 1));
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, -1, 4));
            player.setHealth(40); // max health of player with the health boost

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(Component.text("but the job isn't done yet.").color(NamedTextColor.GOLD));
                    TitleSender.sendTitle("Wumpus", "Annihilator of players", player, 1000, 3000, 1000,
                            NamedTextColor.DARK_RED, NamedTextColor.RED);
                }
            }.runTaskLater(HuntTheWumpusPlugin.getPlugin(), 40);
            cave.renderWumpusRoom(player);
            wumpus = new Wumpus(cave);
            player.teleport(cave.getRoomLocationInCave(RoomStorage.getWumpusRoom(), RoomStorage.getWumpusRoom().getSpawnLocation()));
            wumpus.spawn(cave.getRoomLocationInCave(RoomStorage.getWumpusRoom(), RoomStorage.getWumpusRoom().getSpawnLocation()));
        }
    }
    // move the wumpus to an adjacent room, if the player is not nearby.
    public void moveWumpus(){
        if(indexOf(rooms[playerRoom], wumpusRoom) == -1 && playerRoom != wumpusRoom){
            int newRoom = 0;
            while (true) {
                newRoom = (int)(Math.random() * 6);

                if (rooms[wumpusRoom][newRoom] != -1) {
                    wumpusRoom = rooms[wumpusRoom][newRoom];

                    return;
                }
            }
        }
    }
    public int getWumpusPhase () {
        if (wumpus == null) {
            return 0;
        }
        return wumpus.getPhase();
    }
    public int getWumpusTimeInPhase () {
        if (wumpus == null) {
            return -1;
        }
        return wumpus.getTimeInPhase();
    }
    public void checkForHazards () {

    }
    // checks if the player has walked into a hazardous room, and checks if there are any hazards near the player's new room.
    public void playerCheck(int ogPlayer, int ogWumpus) {
        wumpusNearby = false;
        pitNearby = false;
        batNearby = false;
        if(playerRoom == wumpusRoom){
            inMinigame = true;
            sendMessage(1, "You were eaten by the Wumpus");
            curTrivia = new  Trivia(4, 5, player);
            cave.setCurRoom(RoomStorage.getTriviaRoom());
            cave.renderRoom(player);
            //player.performCommand("kill");
        }
        else{
            if (ogPlayer != playerRoom) {
                sendMessage(1, "You moved from room " + ogPlayer + " to room " + playerRoom);
            }
            if(ogWumpus != wumpusRoom) {
                sendMessage(0, "The wumpus moved from room " + ogWumpus + " to room " + wumpusRoom);
                if(indexOf(rooms[playerRoom], wumpusRoom) != -1) {
                    player.sendMessage(Component.text("You smell a wumpus nearby!").color(NamedTextColor.DARK_GRAY));
                    world.playSound(player, Sound.ENTITY_WARDEN_ANGRY, SoundCategory.MASTER, 1.0f, 1.0f);
                    wumpusNearby = true;
                }
            }
            else{
                if (!initialized) { // this means first check since ogWumpus is never equal to wumpusRoom when the game starts
                    if(indexOf(rooms[playerRoom], wumpusRoom) != -1) {
                        player.sendMessage(Component.text("You smell a wumpus nearby!").color(NamedTextColor.DARK_GRAY));
                        world.playSound(player, Sound.ENTITY_WARDEN_ANGRY, SoundCategory.MASTER, 1.0f, 1.0f);
                        wumpusNearby = true;
                    }

                    initialized = true;
                }
                else {
                    sendMessage(0, "The wumpus stayed in room " + wumpusRoom);
                    sendMessage(0, "You smell a wumpus nearby");
                    world.playSound(player, Sound.ENTITY_WARDEN_ANGRY, SoundCategory.MASTER, 1.0f, 1.0f);
                    wumpusNearby = true;
                }
            }
            // bats
            if(compareArray(rooms[playerRoom], batRooms)[0] != -1) {
                player.sendMessage(Component.text("You hear the sound of flapping!").color(NamedTextColor.DARK_RED));
                world.playSound(player, Sound.ENTITY_BAT_TAKEOFF, SoundCategory.MASTER, 1.0f, 1.0f);
                batNearby = true;
            }
            else if(indexOf(batRooms, playerRoom) != -1){
                sendMessage(1, "Carried away by bats!");
                playerRoom = (int) (Math.random()*30 + 1);
                reCheckPlayer();
            }
            // pits
            if(compareArray(rooms[playerRoom], pitRooms)[0] != -1){
                player.sendMessage(Component.text("You feel a breeze!").color(NamedTextColor.DARK_RED));
                world.playSound(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 1.0f, 1.0f);
                pitNearby = true;
            }
            else if(indexOf(pitRooms, playerRoom) != -1){
                inMinigame = true;
                sendMessage(0, "You fell into a bottomless pit");
                curTrivia = new Trivia(3, 5, player);
                cave.setCurRoom(RoomStorage.getTriviaRoom());
                cave.renderRoom(player);
                //player.performCommand("kill");
                // newTrivia(event, 2, 3);
            }
        }
        sendMessage(0, "The Wumpus is " + findDistanceFromPlayerToWumpus() + " rooms away");
    }
    // check the player's room again, because they were teleported by a super bat
    public void reCheckPlayer() {
        playerCheck(playerRoom, wumpusRoom);
    }

    // starts the game for the player
    public void startGame(Player player, Cave cave) {
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("HuntTheWumpusPlugin"));
        chooseRooms();
        player = Bukkit.getPlayer(player.getName());
        Component message = Component.text("You are currently in are room " + playerRoom + " the Wumpus is in room " + wumpusRoom).color(allColors[0]);
        rooms = getCave().getCaveMap();
        playerCheck(playerRoom, wumpusRoom);
        player.sendMessage(message);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,-1, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,-1, 0, false, false));
        InventoryManager.initInventory(player);
        cave.setCurRoom(playerRoom);
        isRoomRendered[playerRoom] = true;
        //cave.setCurRoom(RoomStorage.getRandomRoom());

        cave.renderRoom(player);

        startActionBarUpdator();
    }
    public void startActionBarUpdator () {
        actionBarUpdator = new BukkitRunnable() {
            @Override
            public void run() {
                ActionBarManager.setActionBar(player, wumpusNearby, pitNearby, batNearby);
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 2);
    }

    // this event listener ends the game as the player has died.
    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event) { // update for multiple people at once
        //HandlerList.unregisterAll(this);
        //player.teleport(RoomStorage.getLobbyLocation());
        System.out.println("A player has died!");
        Player player = event.getPlayer();
        if (player != this.player) {
            return;
        }
         TitleSender.sendTitle("You Died...", "better luck next time", player,
                1000, 3000, 1000, TitleColors.playerDeathTitleColor, TitleColors.playerDeathSubtitleColor);
        endGame();
    }
    // sends the winning messages to the player
    public void playerWin () {
        System.out.println("A player has won!");
        TitleSender.sendTitle("You Killed the wumpus!!","But theres still more to do...", player,
                1, 3, 1);
        endGame();
    }
    public void endGame () {
        actionBarUpdator.cancel();
       // HandlerList.unregisterAll(this);
        ServerData.getPlayerData(player).endGame();
        if (gameLocations != null) {
            gameLocations.destroy();
            gameLocations = null;
        }
        if (curTrivia != null) {
            curTrivia.destroy();
            curTrivia = null;
        }
    }
}