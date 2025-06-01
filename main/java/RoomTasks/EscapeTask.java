package RoomTasks;

import Mobs.WumpusMobManager;
import ServerData.ServerData;
import Titles.TitleColors;
import Titles.TitleSender;
import Rooms.Room;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// player must escape the room to advance.
public class EscapeTask extends RoomTask {
    // escapeStart and escapeEnd signal the location box where a player is deemed to have escaped the room, and can now teleport
    private Location escapeRegionStart;
    private Location escapeRegionEnd;
    private int escapeTime; // escape time in ticks
    private int maxEscapeSeconds;
    private int escapeSeconds; // escape time in seconds
    private boolean mobsSpawned = false;
    public EscapeTask (Room room, Player player, Location escapeRegionStart, Location escapeRegionEnd, int escapeTime) {
        super(room, player);
        this.escapeRegionEnd = escapeRegionEnd;
        this.escapeRegionStart = escapeRegionStart;
        this.escapeTime = escapeTime;
        escapeSeconds = escapeTime / 20;
        this.maxEscapeSeconds = escapeTime / 20;
        initializeTask();
    }
    // checks if the player is in the escape region.
    private boolean isInEscapeRegion () {
        return taskPlayer.getX() >= escapeRegionStart.getX() && taskPlayer.getY() >= escapeRegionStart.getY() && taskPlayer.getZ() >= escapeRegionStart.getZ()
                && taskPlayer.getX() <= escapeRegionEnd.getX() && taskPlayer.getY() <= escapeRegionEnd.getY() && taskPlayer.getZ() <= escapeRegionEnd.getZ();
    }
    public void updateTask () {
        if (!taskPlayer.isOnline()) {
            return;
        }
        super.updateTask();
        if (!mobsSpawned) {
            if (taskRoom.canSpawnMobs()) {
                if (ServerData.getPlayerData(taskPlayer).getGame() != null) {
                    WumpusMobManager.spawnMobs(ServerData.getPlayerData(taskPlayer).getGame().getCave(), taskRoom, 6);
                    mobsSpawned = true;
                }
            }
        }
        escapeTime--;
        escapeSeconds = escapeTime / 20;
        if (escapeTime <= 0) {
            taskPlayer.sendMessage(Component.text("you failed to escape the room in time").color(TitleColors.playerDeathTitleColor));
            taskPlayer.performCommand("kill");
            task.cancel();
            return;
        }
        if (isInEscapeRegion()) {
            TitleSender.sendTitle("You Escaped!", taskVictoryMessage, taskPlayer, 1000, 3000, 1000,
            TitleColors.taskVictoryTitleColor, TitleColors.taskVictorySubtitleColor);
            givePlayerReward();
            ServerData.getPlayerData(taskPlayer).updateScore(escapeSeconds * 120 / (maxEscapeSeconds));
            finishTask();
            return;
        }
        if (escapeTime % 20 == 0 && (escapeSeconds <= 5 || escapeSeconds % 10 == 0)) {
            TitleSender.sendTitle(String.valueOf(escapeSeconds), "", taskPlayer, 250, 500, 250,
                    NamedTextColor.RED, NamedTextColor.RED);
        }
    }
    public void initializeTask () {
        TitleSender.sendTitle("Escape The Room!", "you have " + escapeSeconds + " seconds", taskPlayer, 1000,
                3000, 1000,
                TitleColors.taskInitializeTitleColor, TitleColors.taskInitializeSubtitleColor);
    }
}
