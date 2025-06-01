package Rulebook;

import me.anidiotnon.huntthewumpusplugin.HuntTheWumpusPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Rulebook {
    private static ItemStack rulebook;
    private static BookMeta bookMeta; // content of a book
    private static FileConfiguration rulebookConfig;
    private static String instructions;
    private static ConfigurationSection rulebookSection;
    // initializes the rulebook
    public static void saveRulebookData (Player player) throws IOException {
        rulebookConfig = YamlConfiguration.loadConfiguration(HuntTheWumpusPlugin.getRulebookFile());

        if (rulebookConfig.getConfigurationSection("rulebook") == null) {
            rulebookConfig.createSection("rulebook");
            rulebookConfig.save(HuntTheWumpusPlugin.getRulebookFile());

        }
        rulebookSection = rulebookConfig.getConfigurationSection("rulebook");

        rulebook = player.getInventory().getItem(2);
        bookMeta = (BookMeta)rulebook.getItemMeta();

        int pageCount = 0;

        List<String> bookPages = bookMeta.getPages();

        for (String page : bookPages) {
            pageCount++;

            rulebookSection.set("page" + pageCount, page);
        }

        rulebookConfig.save(HuntTheWumpusPlugin.getRulebookFile());
    }
    public static void initializeRulebook () {
        rulebookConfig = YamlConfiguration.loadConfiguration(HuntTheWumpusPlugin.getRulebookFile());
        rulebookSection = rulebookConfig.getConfigurationSection("rulebook");

        rulebook = new ItemStack(Material.WRITTEN_BOOK);

        bookMeta = (BookMeta)rulebook.getItemMeta();
        bookMeta.setTitle("Game Rules");
        bookMeta.setAuthor("The Wumpus");

        for (String key : rulebookSection.getKeys(false)) {
            bookMeta.addPage(rulebookSection.getString(key));

            HuntTheWumpusPlugin.print(bookMeta.getPage(1));
        }
        bookMeta.displayName(Component.text("Rulebook").color(NamedTextColor.GRAY));

        rulebook.setItemMeta(bookMeta);
    }
    public static ItemStack getRulebook () {
        return rulebook;
    }
}
