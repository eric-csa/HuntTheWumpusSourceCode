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

    public HighScoreManager(Plugin plugin) throws IOException {
        this.plugin = plugin;

        // Ensure highscores section exists in file
        if (HuntTheWumpusPlugin.getHighScoresConfig().getConfigurationSection("highscores") == null) {
            HuntTheWumpusPlugin.getHighScoresConfig().createSection("highscores");
            HuntTheWumpusPlugin.getHighScoresConfig().save(HuntTheWumpusPlugin.getHighScoresFile());
        }

        this.highScoresSection = HuntTheWumpusPlugin.getHighScoresConfig().getConfigurationSection("highscores");
    }

    public void addHighScore(String playerName, int score) throws IOException {
        highScoresSection.set(playerName + ".highscore", score);
        HuntTheWumpusPlugin.getHighScoresConfig().save(HuntTheWumpusPlugin.getHighScoresFile());
    }
    public int getHighScore (Player player) {
        String playerName = player.getName();

        int highScore = highScoresSection.getInt(playerName + ".highscore", -1);

        return highScore;
    }

    public void loadHighScores (Player player) {
        // Display header
        player.sendMessage(ChatColor.YELLOW + "--------- " + ChatColor.GOLD + "TOP HIGH SCORES" + ChatColor.YELLOW + " ---------");

        if (highScoresSection == null || highScoresSection.getKeys(false).isEmpty()) {
            player.sendMessage(ChatColor.RED + "No high scores found!");
            return;
        }

        // Create a map of playerName -> score
        Map<String, Integer> highScores = new HashMap<>();
        for (String playerName : highScoresSection.getKeys(false)) {
            int score = highScoresSection.getInt(playerName + ".highscore");
            highScores.put(playerName, score);

           // player.sendMessage(playerName + " " + score);
        }

        // Sort the scores in descending order
        List<Map.Entry<String, Integer>> sortedScores = highScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        // Display top 10 scores (or fewer if there aren't 10)
        String playerName = player.getName();

        int displayCount = Math.min(10, sortedScores.size());
        for (int i = 0; i < displayCount; i++) {
            Map.Entry<String, Integer> entry = sortedScores.get(i);

            int score = entry.getValue();

            player.sendMessage(ChatColor.GOLD + "#" + (i + 1) + " " + ChatColor.WHITE + entry.getKey() + ": " +
                    ChatColor.YELLOW + score);
        }

        // Display footer
        player.sendMessage(ChatColor.YELLOW + "--------------------------------");

        // Show player's own rank if they have a score
        if (highScores.containsKey(playerName)) {
            int playerScore = highScores.get(playerName);
            int playerRank = 1;

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
