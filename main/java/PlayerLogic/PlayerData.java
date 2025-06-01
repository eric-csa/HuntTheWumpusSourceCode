package PlayerLogic;

import Caves.CaveManager;
import Design.Spacing;
import GameManager.GameManager;
import Rooms.RoomStorage;
import Scoreboards.HighScoreManager;
import ServerData.ServerData;
import SoundManager.SoundManager;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

// This class stores and manages all player-related data,
// such as their current game state, scores, scoreboard display,
// and sound effects related to the player.
public class PlayerData {

    // Current game the player is playing (null if not in game)
    private GameManager curGame;

    // Bukkit player object representing the actual player
    private Player player;

    // Bukkit scoreboard to display player stats and info on screen
    private Scoreboard scoreboard;

    // Player's current score in the game
    private int playerScore = 0;

    // Player's highest score recorded on the server
    private int playerHighScore = 0;

    // Scoreboard objective that holds the display info
    private Objective objective;

    // A "fake" OfflinePlayer used by Bukkit's scoreboard system to display lines
    private OfflinePlayer fakeOfflinePlayer;

    // A score line in the scoreboard that can be updated
    private Score scoreLine;

    // Array of strings used for scoreboard display lines
    String[] displays = new String[13];

    // Lines in scoreboard designated as blank or spacer lines for formatting
    int[] blankLines = {12, 11, 9, 7, 5, 2};

    // A repeating task to check if the player is in the correct lobby region
    BukkitTask playerRegionChecker;

    // Player's collected "runes" currency count
    int runes = 0;

    // The cave number that the player is currently in (-1 if none)
    private int playerCave;

    // Manages sounds for the player (e.g. game events sounds)
    SoundManager soundManager;

    // Constructor: Initialize player data and set up scoreboard and lobby region check
    public PlayerData(Player player) {
        this.player = player;

        // Create a new scoreboard instance for this player
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Create a new scoreboard objective with a title "HuntTheWumpus"
        objective = scoreboard.registerNewObjective("HuntTheWumpus",
                Criteria.DUMMY, Component.text(Spacing.space(10) + "HuntTheWumpus" + Spacing.space(10)).color(NamedTextColor.YELLOW));

        // Set the scoreboard display location on the sidebar
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Initialize blank lines on scoreboard with spaced gray dots for visual formatting
        for (int score : blankLines) {
            displays[score] = "§8." + Spacing.space(score);
        }

        // Initialize scoreboard lines with default values
        displays[10] = "§fRunes: §6" + runes;              // Player currency line
        displays[6] = "§fYour Score: §6" + playerScore;    // Player score line
        displays[8] = "§7Location:§c Wumpus Cave";         // Player location line
        displays[4] = "§fObjective";                         // Objective header
        displays[3] = "§eFind And Lobby The Wumpus";        // Objective description
        displays[1] = "§eHuntTheWumpus.aternos.me";         // Server website or footer

        // Add each line to the scoreboard by creating a fake OfflinePlayer to hold the text
        for (int i = 1; i <= 12; i++) {
            fakeOfflinePlayer = Bukkit.getOfflinePlayer(displays[i]);
            scoreLine = objective.getScore(fakeOfflinePlayer);
            scoreLine.setScore(i);
        }

        // Set this scoreboard as the player's active scoreboard
        player.setScoreboard(scoreboard);

        // Initialize player's cave number to -1 (no cave)
        playerCave = -1;

        // Start a repeating task that runs every tick (1/20 second)
        // This checks if player is in lobby region, and teleports if not
        playerRegionChecker = new BukkitRunnable() {
            @Override
            public void run() {
                moveToLobby();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }

    // Initialize the sound manager for this player
    public void setSoundManager() {
        soundManager = new SoundManager(player);
    }

    // If player is not in a game and above certain height (e.g. left lobby), teleport them back to lobby
    public void moveToLobby() {
        if (curGame == null && player.getLocation().getY() > 110) {
            player.teleport(RoomStorage.getLobbyLocation());
        }
    }

    // When player rejoins the server, update the player reference and reset their scoreboard
    public void updatePlayer(Player player) {
        this.player = player;
        player.setScoreboard(scoreboard);
    }

    // Update the player's high score if the new score is higher
    private void updateHighScore(int newScore) throws IOException {
        playerHighScore = HuntTheWumpusPlugin.getHighScoreManager().getHighScore(player);
        if (newScore > playerHighScore) {
            playerHighScore = newScore;
            HuntTheWumpusPlugin.getHighScoreManager().addHighScore(player.getName(), playerHighScore);
        }
    }

    // Getter for the player's current score
    public int getPlayerScore() {
        return playerScore;
    }

    // Getter for the player's runes count (currency)
    public int getPlayerRunes() {
        return runes;
    }

    // Update the runes count by a given amount (positive or negative),
    // and update the scoreboard display accordingly
    public void updateRunes(int amt) {
        fakeOfflinePlayer = Bukkit.getOfflinePlayer("§fRunes: §6" + runes);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        runes += amt;
        scoreLine.resetScore();
        fakeOfflinePlayer = Bukkit.getOfflinePlayer("§fRunes: §6" + runes);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        scoreLine.setScore(10);
    }

    // Update the player's score by a given amount,
    // update scoreboard, and also update high score if needed
    public void updateScore(int amt) {
        fakeOfflinePlayer = Bukkit.getOfflinePlayer("§fYour Score: §6" + playerScore);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        playerScore += amt;
        scoreLine.resetScore();
        fakeOfflinePlayer = Bukkit.getOfflinePlayer("§fYour Score: §6" + playerScore);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        scoreLine.setScore(6);

        try {
            updateHighScore(playerScore);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Notify player visually if score increased
        if (amt > 0) {
            player.sendMessage(ChatColor.WHITE + "You gained " + ChatColor.YELLOW + amt + ChatColor.WHITE + " Points!");
        }
    }

    // Update the player's current room number and update scoreboard location display
    public void updateRoom(int newPlayerRoom) {
        fakeOfflinePlayer = Bukkit.getOfflinePlayer(displays[8]);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        scoreLine.resetScore();
        displays[8] = "§7Location:§c Room " + newPlayerRoom;
        fakeOfflinePlayer = Bukkit.getOfflinePlayer(displays[8]);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        scoreLine.setScore(8);
    }

    // Check if the player currently has an item of the specified material
    // This includes armor contents and inventory items
    public boolean playerHasItem(Material material) {
        PlayerInventory playerInventory = player.getInventory();

        // Check armor pieces first
        for (ItemStack armorPiece : playerInventory.getArmorContents()) {
            if (armorPiece == null || armorPiece.isEmpty()) {
                continue;
            }
            if (armorPiece.getType() == material) {
                return true;
            }
        }

        // Check general inventory
        return playerInventory.contains(material);
    }

    // Return the first empty slot index in the player's inventory, or -1 if none are empty
    public int getEmptyInventorySlot() {
        PlayerInventory playerInventory = player.getInventory();

        for (int i = 0; i < 36; i++) {
            if (playerInventory.getItem(i) == null || playerInventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    // Set which cave the player is currently in
    public void setPlayerCave(int caveNum) {
        playerCave = caveNum;
    }

    // Starts a new game for the player if they don't already have one active
    public void startGame() {
        if (curGame != null) {
            player.sendMessage(Component.text("You already have a game active. Finish that first!").color(NamedTextColor.RED));
            return;
        }
        curGame = new GameManager(player);
        System.out.println("Game Started For Player " + player.getName());

        // Reset player score and runes on new game start
        updateScore(-playerScore);
        updateRunes(-runes);

        // Reset scoreboard display
        player.setScoreboard(scoreboard);
    }

    // Ends the current game for the player and resets relevant data
    public void endGame() {
        curGame = null;

        if (curGame == null) {
            System.out.println("Game Stopped!");
        }

        // Reset score and runes
        updateScore(-playerScore);
        updateRunes(-runes);

        // Clear cave if player was assigned one
        if (playerCave != -1) {
            CaveManager.clearCave(playerCave);
            playerCave = -1;
        }
    }

    // Get the current game manager instance for this player
    public GameManager getGame() {
        return curGame;
    }
}
