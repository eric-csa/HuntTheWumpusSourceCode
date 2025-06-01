package GUIs.Inventory;

import GUIs.Shop.ShopItem;
import Rulebook.Rulebook;
import ServerData.ServerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryManager {
    private static final Component menuDisplay = Component.text("Game Menu").color(NamedTextColor.YELLOW);
    private static final Component shopDisplay = Component.text("SHOP").color(NamedTextColor.GREEN);
    private static final Component mapDisplay = Component.text("Cave Map").color(NamedTextColor.RED);

    public static void initInventory (Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack curItem;
        ItemMeta curItemMeta;
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
        inventory.setItem(5, Rulebook.getRulebook());

        curItem = new ItemStack(Material.NETHER_STAR);
        curItemMeta = curItem.getItemMeta();
        curItemMeta.displayName(menuDisplay);
        curItem.setItemMeta(curItemMeta);
        inventory.setItem(6, curItem);

        curItem = new ItemStack(Material.EMERALD);
        curItemMeta = curItem.getItemMeta();
        curItemMeta.displayName(shopDisplay);
        curItem.setItemMeta(curItemMeta);

        inventory.setItem(7, curItem);

        curItem = new ItemStack(Material.FILLED_MAP);
        curItemMeta = curItem.getItemMeta();
        curItemMeta.displayName(mapDisplay);
        curItem.setItemMeta(curItemMeta);

        inventory.setItem(8, curItem);
        ServerData.getPlayerData(player).updateRunes(20);
        ShopItem.giveItemToPlayer(Material.GOLDEN_SWORD, player);
        ShopItem.giveItemToPlayer(Material.GOLDEN_AXE, player);
    }
}
