package GUIs.ActionBar;

import ServerData.ServerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionBarManager {

    public static void setActionBar (Player player, boolean wumpusNearby, boolean pitNearby, boolean batNearby) {
        String spacing = "       ";
        String actionBarMessage = ChatColor.BOLD + "" + ChatColor.RED + "Hazards: " + spacing +
                ChatColor.BLUE + "Wumpus Distance: " + ServerData.getPlayerData(player).getGame().findDistanceFromPlayerToWumpus() + " blocks" + spacing +
                ChatColor.GRAY + "Pit Nearby: " + pitNearby + spacing +
                ChatColor.YELLOW + "Bat Nearby: " + batNearby;

        player.sendActionBar(actionBarMessage);
    }
}
