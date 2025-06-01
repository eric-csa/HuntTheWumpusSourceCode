package ServerData;

import GUIs.Inventory.InventoryManager;
import Rulebook.Rulebook;
import Titles.TitleSender;
import PlayerLogic.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.util.HashMap;

// the server data stores all the data in the server for each player.
public class ServerData implements Listener {
    private static HashMap<String, PlayerData> playerData; // data is stored in <username, playerData> hashmap

    public ServerData () {
        playerData = new HashMap<>();
    }
    // helper function to get the player data of a player
    public static PlayerData getPlayerData (Player curPlayer) {
        if (playerData.containsKey(curPlayer.getName())) {
            return playerData.get(curPlayer.getName());
        }
        return null;
    }
    // this event listener initializes a new player's data, and also updates data when a player rejoins the server.
    @EventHandler
    public void initializePlayerData (PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!playerData.containsKey(player.getName())) {
            playerData.put(player.getName(), new PlayerData(player));

            TitleSender.sendTitle("Welcome to Hunt The Wumpus!", "Happy Hunting...",
                    player, 1, 3, 1);
            InventoryManager.initInventory(player);
        }
        else {
            playerData.get(player.getName()).updatePlayer(player);
            TitleSender.sendTitle("Welcome Back to Hunt The Wumpus!", "Happy Hunting...",
                    player, 1, 3, 1);

            if (ServerData.getPlayerData(player).getGame() != null) {
                ServerData.getPlayerData(player).getGame().updatePlayer(player);
            }
            else {
                InventoryManager.initInventory(player);
            }
        }
        ServerData.getPlayerData(player).setSoundManager();
    }
}