package GUIs;

import GUIs.Shop.ShopItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Bukkit.createInventory;

public class UtilitiesShopMenu extends Menu implements Listener {
    public UtilitiesShopMenu () {
        menuTitle = Component.text("Utilities Shop").color(NamedTextColor.BLUE);
        menu = createInventory(null, 9, menuTitle);

        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS));
        }
        menu.setItem(3, new ShopItem("Player Meat", 5, Material.COOKED_BEEF).getItem());
        menu.setItem(5, new ShopItem("Golden Apple", 40, Material.GOLDEN_APPLE).getItem());
    }
    @EventHandler
    public void ShopItemPurchaseEvent (InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().title().equals(menuTitle)) return;

        Player player = (Player) event.getWhoClicked();

        event.setCancelled(true);

        Material clickedMaterial = event.getCurrentItem().getType();

        ShopItem.giveItemToPlayer(clickedMaterial, player);
    }
}
