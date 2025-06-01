package SoundManager;

import ServerData.ServerData;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

// Sound Managing class. Manages all of the sounds for the players
public class SoundManager {
    private int ticksSinceLastPlay; // Counter for how many ticks have passed since last sound play
    private Player player; // The player for whom sounds are managed
    private BukkitTask soundTask; // Task that periodically updates sounds
    private int currentSoundDuration; // Duration in ticks for the currently playing sound

    // Duration constants for different soundtracks (in ticks)
    private final int lobbySoundDuration = 5360;
    private final int minigameSoundDuration = 5360;
    private final int mainSoundDuration = 4280;
    private final int bossIntroDuration = 440;
    private final int bossThemePhase1Duration = 900;
    private final int bossThemePhase2InterludeDuration = 440;
    private final int bossThemePhase2Duration = 900;
    private final int bossThemePhase3Duration = 1400;

    private int wumpusPhase = 0; // Current phase of the Wumpus boss fight
    private int wumpusPhaseTime = 0; // Time elapsed in current Wumpus phase
    private String previousSound; // The previously played sound to detect changes
    String currentSound; // The current sound that is playing

    // Constructor initializes the sound manager for the given player and starts the periodic update task
    public SoundManager (Player player) {
        this.player = player;
        // Create a repeating task that calls updateSounds() every tick (1/20 second)
        soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateSounds();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }

    // Updates the current sound based on the given sound name
    private void updateCurrentSound (String sound) {
        currentSound = sound;

        // Reset ticksSinceLastPlay if the sound has changed
        if (!currentSound.equals(previousSound)) {
            ticksSinceLastPlay = 0;
            previousSound = currentSound;
        }

        // Set the duration for the new current sound based on predefined constants
        switch (sound) {
            case "lobby" -> currentSoundDuration = lobbySoundDuration;
            case "main" -> currentSoundDuration = mainSoundDuration;
            case "minigame" -> currentSoundDuration = minigameSoundDuration;
            case "bossintrocutscene" -> currentSoundDuration = bossIntroDuration;
            case "phase1" -> currentSoundDuration = bossThemePhase1Duration;
            case "phase2" -> currentSoundDuration = bossThemePhase2Duration;
            case "phase3" -> currentSoundDuration = bossThemePhase3Duration;
            case "phase2interlude" -> currentSoundDuration = bossThemePhase2InterludeDuration;
            default -> throw new IllegalArgumentException("invalid sound: " + sound + " does not exist");
        }
    }

    // Periodically called method to update and play sounds according to player's game state
    private void updateSounds () {
        // Cancel sound updates if player is offline
        if (!player.isOnline()) {
            soundTask.cancel();
            return;
        }

        // Refresh player reference (in case of respawn or other state changes)
        player = player.getPlayer();

        // Determine what sound to play based on player's current game status:

        // If player is in lobby (no active game)
        if (ServerData.getPlayerData(player).getGame() == null) {
            updateCurrentSound("lobby");
        }
        // If player is currently battling the Wumpus boss (phase > 0)
        else if (ServerData.getPlayerData(player).getGame().getWumpusPhase() > 0) {
            wumpusPhase = ServerData.getPlayerData(player).getGame().getWumpusPhase();
            wumpusPhaseTime = ServerData.getPlayerData(player).getGame().getWumpusTimeInPhase();

            // Play intro cutscene sound if in phase 1 and within intro duration
            if (wumpusPhase == 1) {
                if (wumpusPhaseTime <= bossIntroDuration) {
                    updateCurrentSound("bossintrocutscene");
                }
                else {
                    updateCurrentSound("phase1");
                }
            }
            else {
                // For phases beyond 1, play interlude or phase sound depending on time
                if (wumpusPhaseTime <= bossThemePhase2InterludeDuration) {
                    updateCurrentSound("phase2interlude");
                }
                else {
                    updateCurrentSound("phase" + wumpusPhase);
                }
            }
        }
        // If player is in a minigame
        else if (ServerData.getPlayerData(player).getGame().playerInMinigame()) {
            updateCurrentSound("minigame");
        }
        // Default to main game soundtrack
        else {
            updateCurrentSound("main");
        }

        // Control playback timing and looping of the current sound
        if (ticksSinceLastPlay <= 0) {
            player.stopAllSounds(); // Stop any sounds currently playing to avoid overlap
            ticksSinceLastPlay = 1;
            player.playSound(player, currentSound, SoundCategory.MASTER, 1.0f, 1.0f); // Play current sound for the player
        }
        else {
            ticksSinceLastPlay++; // Increment tick counter since last play

            // When the current sound's duration is reached, reset counter to allow replay
            if (ticksSinceLastPlay >= currentSoundDuration) {
                ticksSinceLastPlay = -2;
            }
        }
    }
}
