package Commands;

import GUIs.GuiManager;
import GUIs.LobbyMenu;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// the open lobby menu command, will open the starting game menu once the player types the command.
public class OpenLobbyMenu implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        HuntTheWumpusPlugin.getGuiManager().getLobbyMenu().openMenu(player); // opens the gui

        return true;
    }
}