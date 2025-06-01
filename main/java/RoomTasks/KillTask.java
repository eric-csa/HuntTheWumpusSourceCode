package RoomTasks;

import Mobs.WumpusMobManager;
import ServerData.ServerData;
import Titles.TitleColors;
import Titles.TitleSender;
import Rooms.Room;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import static Titles.TitleColors.taskVictorySubtitleColor;
import static Titles.TitleColors.taskVictoryTitleColor;

// player must kill a certain amount of mobs to advance.
public class KillTask extends RoomTask implements Listener {
    private int mobsKilled;
    private int killsRequired = 0;
    private boolean mobsSpawned = false;
    public KillTask (Room room, Player player, int killsRequired) {
        super(room, player);
        this.killsRequired = killsRequired;
        mobsKilled = 0;
        initializeTask();
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
    }
    public void initializeTask () {
        TitleSender.sendTitle("Slay!", "you need to kill at least " + killsRequired + " mobs", taskPlayer, 1000, 3000,
                1000,
                TitleColors.taskInitializeTitleColor, TitleColors.taskInitializeSubtitleColor);
    }
    public void updateTask () {
        super.updateTask();

        if (!mobsSpawned) {
            if (ServerData.getPlayerData(taskPlayer).getGame() != null) {
                WumpusMobManager.spawnMobs(ServerData.getPlayerData(taskPlayer).getGame().getCave(), taskRoom, killsRequired + 1);
                mobsSpawned = true;
            }
        }
        if (ServerData.getPlayerData(taskPlayer).getGame() == null) {
            HandlerList.unregisterAll(this);
        }
    }
    public void updateKillCount () {
        mobsKilled++;
        if (mobsKilled >= killsRequired) {
            TitleSender.sendTitle("You Killed Enough Mobs", taskVictoryMessage, taskPlayer, 1000, 3000, 1000,
                    taskVictoryTitleColor, taskVictorySubtitleColor);
            ServerData.getPlayerData(taskPlayer).updateScore(Math.max(0, 120 - taskTime / 20));
            givePlayerReward();
            HandlerList.unregisterAll(this);
            finishTask();
            return;
        }
        taskPlayer.sendMessage(Component.text("you have killed " + mobsKilled + " out of " + killsRequired + " mobs").color(NamedTextColor.LIGHT_PURPLE));
    }
    @EventHandler
    public void onPlayerKillEvent (EntityDeathEvent event) {
        if (event.getEntity().getKiller() == taskPlayer) {
            updateKillCount();
        }
    }
}
