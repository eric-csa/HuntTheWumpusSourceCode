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

// this class is responsible for storing all the player data. Including, whether they are in a game,
// their high score, their current score, and the minecraft scoreboard to display information about their game.
//
public class PlayerData {
    private GameManager curGame;
    private Player player;
    private Scoreboard scoreboard;
    private int playerScore = 0;
    private int playerHighScore = 0;
    private Objective objective;
    private OfflinePlayer fakeOfflinePlayer; // minecraft scoreboards use fakeOfflinePlayers to display information.
    private Score scoreLine;
    String[] displays = new String[13];
    int[] blankLines = {12, 11, 9, 7, 5, 2};
    BukkitTask playerRegionChecker;
    int runes = 0;
    // initializes the player data and updates their scoreboard.
    private int playerCave;

    SoundManager soundManager;

    public PlayerData (Player player) {
        this.player = player;

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("HuntTheWumpus",
                Criteria.DUMMY, Component.text(Spacing.space(10) + "HuntTheWumpus" + Spacing.space(10)).color(NamedTextColor.YELLOW));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int score : blankLines) {
            displays[score] = "§8." + Spacing.space(score);
        }
        displays[10] = "§fRunes: §6" + runes;
        displays[6] = "§fYour Score: §6" + playerScore;
        displays[8] = "§7Location:§c Wumpus Cave";
        displays[4] = "§fObjective";
        displays[3] = "§eFind And Lobby The Wumpus";
        displays[1] = "§eHuntTheWumpus.aternos.me";

        for (int i = 1; i <= 12; i++) {
            fakeOfflinePlayer = Bukkit.getOfflinePlayer(displays[i]);
            scoreLine = objective.getScore(fakeOfflinePlayer);
            scoreLine.setScore(i);
        }

        player.setScoreboard(scoreboard); // render the scoreboard to the player.
        playerCave = -1;

        // this runTask makes sure that the player is in the lobby when they are supposed to be.
        playerRegionChecker =  new BukkitRunnable() {
            @Override
            public void run () {
                moveToLobby();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }
    public void setSoundManager () {
        soundManager = new SoundManager(player);
    }
    public void moveToLobby () {
        if (curGame == null && player.getLocation().getY() > 110) {
            player.teleport(RoomStorage.getLobbyLocation());
        }
    }
    // updates the player upon rejoining the server.
    public void updatePlayer (Player player) {
        this.player = player;
        player.setScoreboard(scoreboard);
    }
    private void updateHighScore (int newScore) throws IOException {
        playerHighScore = HuntTheWumpusPlugin.getHighScoreManager().getHighScore(player);
        if (newScore > playerHighScore) {
            playerHighScore = newScore;

            HuntTheWumpusPlugin.getHighScoreManager().addHighScore(player.getName(), playerHighScore);
        }
    }
    public int getPlayerScore () {
        return playerScore;
    }
    public int getPlayerRunes () {
        return runes;
    }
    // updates the player score.
    public void updateRunes (int amt) { // negative amount for transaction, position for gain
        fakeOfflinePlayer = Bukkit.getOfflinePlayer("§fRunes: §6" + runes);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        runes += amt;
        scoreLine.resetScore();
        fakeOfflinePlayer = Bukkit.getOfflinePlayer("§fRunes: §6" + runes);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        scoreLine.setScore(10);
    }
    public void updateScore (int amt) {
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
        if (amt > 0) {
            player.sendMessage(ChatColor.WHITE + "You gained " + ChatColor.YELLOW + amt + ChatColor.WHITE + " Points!");
        }
    }
    // updates the new player room.
    public void updateRoom (int newPlayerRoom) {
        fakeOfflinePlayer = Bukkit.getOfflinePlayer(displays[8]);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        scoreLine.resetScore();
        displays[8] = "§7Location:§c Room " + newPlayerRoom;
        fakeOfflinePlayer = Bukkit.getOfflinePlayer(displays[8]);
        scoreLine = objective.getScore(fakeOfflinePlayer);
        scoreLine.setScore(8);
    }
    public boolean playerHasItem (Material material) {
        PlayerInventory playerInventory = player.getInventory();

        for (ItemStack armorPiece : playerInventory.getArmorContents()) {
            if (armorPiece == null || armorPiece.isEmpty()) {
                continue;
            }
            if (armorPiece.getType() == material) {
                return true;
            }
        }

        return playerInventory.contains(material);
    }
    public int getEmptyInventorySlot () {
        PlayerInventory playerInventory = player.getInventory();

        for (int i = 0; i < 36; i++) {
            if (playerInventory.getItem(i) == null || playerInventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
    public void setPlayerCave (int caveNum) {
        playerCave = caveNum;
    }
    // starts the game for the player
    public void startGame () {
        if (curGame != null) {
            player.sendMessage(Component.text("You already have a game active. Finish that first!").color(NamedTextColor.RED));
            return;
        }
        curGame = new GameManager(player);
        System.out.println("Game Started For Player " + player.getName());
        updateScore(-playerScore);
        updateRunes(-runes);
        player.setScoreboard(scoreboard);
    }
    // ends the game for the player
    public void endGame () {
        curGame = null;
        if (curGame == null) {
            System.out.println("Game Stopped!");
        }
        updateScore(-playerScore);
        updateRunes(-runes);

        if (playerCave != -1) {
            CaveManager.clearCave(playerCave);
            playerCave = -1;
        }
    }
    public GameManager getGame () {
        return curGame;
    }
}
