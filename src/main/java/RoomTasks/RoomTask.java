package RoomTasks;

import Minigames.EscapePuzzle;
import Rooms.Room;
import ServerData.ServerData;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static Caves.CaveManager.world;

// base class for room challenges that the player needs to complete.
public class RoomTask {
    protected static String randomTask () {
        double randRoll = Math.random();

        if (randRoll < 0.33) {
            return "Survive";
        }
        if (randRoll < 0.66) {
            return "Target";
        }
        return "Slay";
    }
    protected Room taskRoom;
    protected Player taskPlayer;
    protected BukkitTask task;
    protected int taskTime;
    protected final String taskVictoryMessage = "you may now move to other rooms or shoot the wumpus";

    public RoomTask (Room room, Player player) {
        taskRoom = room;
        taskPlayer = player;
        taskTime = 0;
        task = new BukkitRunnable() {
            @Override
            public void run () {
                updateTask();
            }
        }.runTaskTimer(HuntTheWumpusPlugin.getPlugin(), 0, 1);
    }
    public void updatePlayer (Player player) {
        taskPlayer = player;
    }
    protected void finishTask () {
        task.cancel();
        taskRoom.setCleared();
        //taskRoom = null;
        //taskPlayer = null;
        if (this instanceof EscapePuzzle) {
            HandlerList.unregisterAll((EscapePuzzle)this);
        }
        if (this instanceof SurviveTask) {
            HandlerList.unregisterAll((SurviveTask)this);
        }
        if (this instanceof TargetTask) {
            HandlerList.unregisterAll((TargetTask)this);
        }
    }
    protected void givePlayerReward () {
        ServerData.getPlayerData(taskPlayer).updateRunes(15);
        taskPlayer.sendMessage(Component.text("You got 15 runes for completing this challenge!").color(NamedTextColor.GOLD));
    }
    public void updateTask () {
        taskTime++;


        if (ServerData.getPlayerData(taskPlayer).getGame() == null) {
            finishTask();
        }
    }
    public void initializeTask () {

    }

}
