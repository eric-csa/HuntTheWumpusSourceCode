package Commands;

import ServerData.ServerData;
import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveRunes implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /giverunes <runesAmount>");
        }
        try {
            int runeAmount = Integer.parseInt(args[0]);
            ServerData.getPlayerData(player).updateRunes(runeAmount);
            player.sendMessage(Component.text("Gave " + runeAmount + " runes!").color(NamedTextColor.GOLD));
        }
        catch (NumberFormatException e) {
            player.sendMessage(Component.text("please enter a valid number.").color(NamedTextColor.RED));
            return true;
        }

        return true;
    }
}
