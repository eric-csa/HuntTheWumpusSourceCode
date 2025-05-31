
package Commands;

import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoadHighScores implements CommandExecutor {
    // command to load the high scores.
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        HuntTheWumpusPlugin.getHighScoreManager().loadHighScores(player);

        return true;
    }
}
