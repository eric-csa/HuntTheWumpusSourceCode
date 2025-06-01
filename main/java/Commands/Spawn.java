package Commands;

import Mobs.Wumpus;
import Mobs.WumpusMobManager;
import Rooms.RoomStorage;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// testing command to test out mobs in hunt the wumpus. Not used in the actual game.
public class Spawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("Usage: /Spawn <mob>");
        }

        if (args[0].equals("Wumpus")) {
            Wumpus wumpus = new Wumpus();
            player.teleport(RoomStorage.getWumpusRoom().getSpawnLocation());
            wumpus.spawn(RoomStorage.getWumpusRoom().getSpawnLocation());
        }

        return true;
    }
}
