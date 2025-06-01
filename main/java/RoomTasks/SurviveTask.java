package RoomTasks;

import Mobs.WumpusMobManager;
import Rooms.Room;
import ServerData.ServerData;
import Titles.TitleColors;
import Titles.TitleSender;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SurviveTask extends RoomTask implements Listener {
    private int surviveTime = 0;
    private int surviveSeconds = 0;
    private int surviveMobs = 0;
    private boolean mobsSpawned = false;
    private final int killScoreGain = 20;
    private final int surviveScoreGain = 40;
    public SurviveTask (Room room, Player player, int surviveTime) {
        super(room, player);
        this.surviveTime = surviveTime;
        surviveSeconds = surviveTime / 20;
        initializeTask();
    }
    public void updateTask () {
        if (!taskPlayer.isOnline()) {
            return;
        }
        super.updateTask();
        if (!mobsSpawned) {
            if (ServerData.getPlayerData(taskPlayer).getGame() != null) {
                WumpusMobManager.spawnMobs(ServerData.getPlayerData(taskPlayer).getGame().getCave(), taskRoom, Math.min(6, taskRoom.getRoomSpawnLocationCount()));
                mobsSpawned = true;
            }
        }
        surviveTime--;
        surviveSeconds = surviveTime / 20;
        if (surviveTime <= 0) {
            TitleSender.sendTitle("You Survived!", taskVictoryMessage, taskPlayer, 1000, 3000, 1000,
                    TitleColors.taskVictoryTitleColor, TitleColors.taskVictorySubtitleColor);
            givePlayerReward();
            ServerData.getPlayerData(taskPlayer).getGame().getCave().killMobs();
            ServerData.getPlayerData(taskPlayer).updateScore(surviveScoreGain);
            finishTask();
            return;
        }
        if (surviveTime % 20 == 0 && (surviveSeconds <= 5 || surviveSeconds % 10 == 0)) {
            TitleSender.sendTitle(String.valueOf(surviveSeconds), "", taskPlayer, 250, 500, 250,
                    NamedTextColor.RED, NamedTextColor.RED);
        }
    }
    public void initializeTask () {
        TitleSender.sendTitle("Survive!", "survive the waves of mobs for " + surviveSeconds + " seconds", taskPlayer, 1000, 3000,
                1000, TitleColors.taskInitializeTitleColor, TitleColors.taskInitializeSubtitleColor);
        Bukkit.getPluginManager().registerEvents(this, HuntTheWumpusPlugin.getPlugin());
    }
    @EventHandler
    public void onMobDeathEvent (EntityDeathEvent event) {
        if (surviveTime <= 0) {
            return;
        }
        if (event.getEntity().getKiller() == taskPlayer) {
            taskPlayer.sendMessage(Component.text("You reduced survive time by 4 seconds from killing a mob!").color(NamedTextColor.YELLOW));
            surviveTime -= 80;
            TitleSender.sendTitle(String.valueOf(surviveTime / 20), "", taskPlayer, 250, 500, 250,
                    NamedTextColor.RED, NamedTextColor.RED);
            ServerData.getPlayerData(taskPlayer).updateScore(killScoreGain);
        }

    }
}
