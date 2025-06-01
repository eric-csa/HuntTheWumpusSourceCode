package Scoreboards;

import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// manages and saves the highscores of the player.
public class HighScoreManager {
    private final Plugin plugin;
    private final ConfigurationSection highScoresSection;

    // Constructor initializes the high score manager and ensures
    // that the "highscores" section exists in the config file.
    public HighScoreManager(Plugin plugin) throws IOException {
        this.plugin = plugin;

        // Check if "highscores" section exists in config, create if not
        if (HuntTheWumpusPlugin.getHighScoresConfig().getConfigurationSection("highscores") == null) {
            HuntTheWumpusPlugin.getHighScoresConfig().createSection("highscores");
            HuntTheWumpusPlugin.getHighScoresConfig().save(HuntTheWumpusPlugin.getHighScoresFile());
        }

        // Cache the highscores section for further operations
        this.highScoresSection = HuntTheWumpusPlugin.getHighScoresConfig().getConfigurationSection("highscores");
    }

    // Adds or updates a player's high score in the config and saves it
    public void addHighScore(String playerName, int score) throws IOException {
        highScoresSection.set(playerName + ".highscore", score);
        HuntTheWumpusPlugin.getHighScoresConfig().save(HuntTheWumpusPlugin.getHighScoresFile());
    }

    // Retrieves the high score of a specific player, returns -1 if none found
    public int getHighScore(Player player) {
        String playerName = player.getName();

        int highScore = highScoresSection.getInt(playerName + ".highscore", -1);

        return highScore;
    }

    // Loads and displays the top high scores to the given player
    public void loadHighScores(Player player) {
        // Display the header for the scoreboard
        player.sendMessage(ChatColor.YELLOW + "--------- " + ChatColor.GOLD + "TOP HIGH SCORES" + ChatColor.YELLOW + " ---------");

        // If no highscores found, inform the player and exit
        if (highScoresSection == null || highScoresSection.getKeys(false).isEmpty()) {
            player.sendMessage(ChatColor.RED + "No high scores found!");
            return;
        }

        // Build a map of player names to their scores
        Map<String, Integer> highScores = new HashMap<>();
        for (String playerName : highScoresSection.getKeys(false)) {
            int score = highScoresSection.getInt(playerName + ".highscore");
            highScores.put(playerName, score);

            // Optional debug: player.sendMessage(playerName + " " + score);
        }

        // Sort the map entries by score descending
        List<Map.Entry<String, Integer>> sortedScores = highScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        // Limit to top 10 scores or fewer if less available
        String playerName = player.getName();
        int displayCount = Math.min(10, sortedScores.size());

        // Send each top score entry to the player
        for (int i = 0; i < displayCount; i++) {
            Map.Entry<String, Integer> entry = sortedScores.get(i);
            int score = entry.getValue();

            player.sendMessage(ChatColor.GOLD + "#" + (i + 1) + " " + ChatColor.WHITE + entry.getKey() + ": " +
                    ChatColor.YELLOW + score);
        }

        // Display the footer for the scoreboard
        player.sendMessage(ChatColor.YELLOW + "--------------------------------");

        // If the player has a high score, display their rank and score
        if (highScores.containsKey(playerName)) {
            int playerScore = highScores.get(playerName);
            int playerRank = 1;

            // Calculate player's rank by iterating through sorted list
            for (Map.Entry<String, Integer> entry : sortedScores) {
                if (entry.getKey().equals(playerName)) {
                    break;
                }
                playerRank++;
            }

            player.sendMessage(ChatColor.GREEN + "Your rank: " + ChatColor.GOLD + "#" + playerRank +
                    ChatColor.GREEN + " with score: " + ChatColor.GOLD + playerScore);
        }
    }
}