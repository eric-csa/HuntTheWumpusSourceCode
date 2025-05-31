package RoomTasks;

import Mobs.WumpusMob;
import Mobs.WumpusMobManager;
import Rooms.Room;
import ServerData.ServerData;
import Titles.TitleColors;
import Titles.TitleSender;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

// player must kill a specific mob to advance.
public class TargetTask extends RoomTask implements Listener {
    private Entity targetEntity;
    private WumpusMob targetMob;
    private Location targetSpawnLocation;

    public TargetTask (Room room, Player player, WumpusMob targetMob, Location targetSpawnLocation) {
        super(room, player);
        this.targetMob = targetMob;
        this.targetSpawnLocation = targetSpawnLocation;
        initializeTask();
    }
    public void initializeTask () {
        targetMob.spawn(targetSpawnLocation);
        targetEntity = targetMob.getBukkitEntity();
        TitleSender.sendTitle("Slay Target!", "slay the highlighted mob", taskPlayer, 1000, 3000, 1000,
                TitleColors.taskInitializeTitleColor, TitleColors.taskInitializeSubtitleColor);
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
    }
    @EventHandler
    public void onMobDeathEvent (EntityDeathEvent event) {
        if (event.getEntity() == targetEntity && taskPlayer != null) {
            TitleSender.sendTitle("You Successfully Killed The Target", taskVictoryMessage, taskPlayer,1000, 3000, 1000,
                    TitleColors.taskVictoryTitleColor, TitleColors.taskVictorySubtitleColor);
            givePlayerReward();
            ServerData.getPlayerData(taskPlayer).updateScore(Math.max(0, 120 - taskTime / 20));
            finishTask();
        }
    }

    public void updateTask () {
        if (!taskPlayer.isOnline()) {
            return;
        }
        super.updateTask();
//        if (taskTime % 1200 == 2) {
//            WumpusMobManager.spawnMobs(ServerData.getPlayerData(taskPlayer).getGame().getCave(), taskRoom, 2);
//        }
    }
}
