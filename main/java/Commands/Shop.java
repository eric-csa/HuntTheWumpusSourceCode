package Commands;

import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Shop implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length > 0) {
            switch (args[0]) {
                case "Weapons" -> HuntTheWumpusPlugin.getGuiManager().getWeaponShopMenu().openMenu(player);
                case "Armor" -> HuntTheWumpusPlugin.getGuiManager().getArmorShopMenu().openMenu(player);
                case "Utilities" -> HuntTheWumpusPlugin.getGuiManager().getUtilitiesShopMenu().openMenu(player);
            }
            return true;
        }

        HuntTheWumpusPlugin.getGuiManager().getShopMenu().openMenu(player); // opens the gui

        return true;
    }
}
