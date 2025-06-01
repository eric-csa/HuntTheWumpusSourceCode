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

public class ArmorShopMenu extends Menu implements Listener {
    public ArmorShopMenu () {
        menuTitle = Component.text("Armor Shop").color(NamedTextColor.BLUE);
        menu = createInventory(null, 36, menuTitle);

        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS));
        }
        for (int i = 0; i < 4; i++) {
            menu.clear(2 + i * 9);
            menu.clear(6 + i * 9);
        }
        menu.setItem(3, new ShopItem("Leather Helmet", 10, Material.LEATHER_HELMET).getItem());
        menu.setItem(12, new ShopItem("Leather Chestplate", 20, Material.LEATHER_CHESTPLATE).getItem());
        menu.setItem(21, new ShopItem("Leather Leggings", 15, Material.LEATHER_LEGGINGS).getItem());
        menu.setItem(30, new ShopItem("Leather Boots", 10, Material.LEATHER_BOOTS).getItem());


        menu.setItem(5, new ShopItem("Chainmail Helmet", 15, Material.CHAINMAIL_HELMET).getItem());
        menu.setItem(14, new ShopItem("Chainmail Chestplate", 30, Material.CHAINMAIL_CHESTPLATE).getItem());
        menu.setItem(23, new ShopItem("Chainmail Leggings", 25, Material.CHAINMAIL_LEGGINGS).getItem());
        menu.setItem(32, new ShopItem("Chainmail Boots", 15, Material.CHAINMAIL_BOOTS).getItem());
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
