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
    private int ticksSinceLastPlay;
    private Player player;
    private BukkitTask soundTask;
    private int currentSoundDuration;

    private final int lobbySoundDuration = 5360;
    private final int minigameSoundDuration = 5360;
    private final int mainSoundDuration = 4280;
    private final int bossIntroDuration = 440;
    private final int bossThemePhase1Duration = 900;
    private final int bossThemePhase2InterludeDuration = 440;
    private final int bossThemePhase2Duration = 900;
    private final int bossThemePhase3Duration = 1400;
    private int wumpusPhase = 0;
    private int wumpusPhaseTime = 0;
    private String previousSound;
    String currentSound; // the current sound that is playing
    public SoundManager (Player player) {
        this.player = player;
        soundTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateSounds();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }
    private void updateCurrentSound (String sound) { // updates the current sound based on the sound given
        currentSound = sound;
        if (!currentSound.equals(previousSound)) {
            ticksSinceLastPlay = 0;
            previousSound = currentSound;
        }
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
    private void updateSounds () { // depending on where the player is in the game, update the sound for the player.
                                // this function only triggers when a new sound is needed, or when the current soundtrack has finished playing.
        if (!player.isOnline()) {
            soundTask.cancel();
            return;
        }
        player = player.getPlayer();
        // player is in the lobby
        if (ServerData.getPlayerData(player).getGame() == null) {
            updateCurrentSound("lobby");
        }
        // player is battling the wumpus
        else if (ServerData.getPlayerData(player).getGame().getWumpusPhase() > 0) {
            wumpusPhase = ServerData.getPlayerData(player).getGame().getWumpusPhase();
            wumpusPhaseTime = ServerData.getPlayerData(player).getGame().getWumpusTimeInPhase();

            if (wumpusPhase == 1) {
                if (wumpusPhaseTime <= bossIntroDuration) {
                    updateCurrentSound("bossintrocutscene");
                }
                else {
                    updateCurrentSound("phase1");
                }
            }
            else {
                if (wumpusPhaseTime <= bossThemePhase2InterludeDuration) {
                    updateCurrentSound("phase2interlude");
                }
                else {
                    updateCurrentSound("phase" + wumpusPhase);
                }
            }
        }
        // player is in a minigame
        else if (ServerData.getPlayerData(player).getGame().playerInMinigame()) {
            updateCurrentSound("minigame");
        }
        else {
            updateCurrentSound("main");
        }
        if (ticksSinceLastPlay <= 0) {
            player.stopAllSounds();
            ticksSinceLastPlay = 1;
            player.playSound(player, currentSound, SoundCategory.MASTER, 1.0f, 1.0f);
        }
        else {
            ticksSinceLastPlay++;

            if (ticksSinceLastPlay >= currentSoundDuration) {
                ticksSinceLastPlay = -2;
            }
        }
    }
}
