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

public class WeaponShopMenu extends Menu implements Listener {
    public WeaponShopMenu () {
        menuTitle = Component.text("Weapon Shop").color(NamedTextColor.BLUE);
        menu = createInventory(null, 36, menuTitle);

        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS));
        }
        for (int i = 0; i < 4; i++) {
            menu.clear(2 + i * 9);
            menu.clear(6 + i * 9);
        }
        menu.setItem(13, new ShopItem("Wumpus Destroyer Bow", 40, Material.BOW).getItem());
        menu.setItem(22, new ShopItem("Weakening Arrow", 20, Material.ARROW).getItem());

        menu.setItem(3, new ShopItem("Base Wumpus Sword", 10, Material.GOLDEN_SWORD).getItem());
        menu.setItem(12, new ShopItem("Stone Sword", 10, Material.STONE_SWORD).getItem());
        menu.setItem(21, new ShopItem("Iron Sword", 30, Material.IRON_SWORD).getItem());
        menu.setItem(30, new ShopItem("Diamond Sword", 40, Material.DIAMOND_SWORD).getItem());


        menu.setItem(5, new ShopItem("Base Wumpus Axe", 10, Material.GOLDEN_AXE).getItem());
        menu.setItem(14, new ShopItem("Stone Axe", 10, Material.STONE_AXE).getItem());
        menu.setItem(23, new ShopItem("Iron Axe", 30, Material.IRON_AXE).getItem());
        menu.setItem(32, new ShopItem("Diamond Axe", 40, Material.DIAMOND_AXE).getItem());
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
