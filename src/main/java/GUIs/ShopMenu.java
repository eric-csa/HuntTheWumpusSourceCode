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

public class ShopMenu extends Menu implements Listener {
    public ShopMenu() {
        menuTitle = Component.text("SHOP").color(NamedTextColor.GREEN);
        menu = createInventory(null, 54, menuTitle);

        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS));
        }
        menu.setItem(20, createButton(Component.text("Weapons").color(NamedTextColor.BLUE), new ItemStack(Material.DIAMOND_SWORD)));
        menu.setItem(24, createButton(Component.text("Armor").color(NamedTextColor.BLUE), new ItemStack(Material.DIAMOND_CHESTPLATE)));
        menu.setItem(40, createButton(Component.text("Utilities").color(NamedTextColor.BLUE), new ItemStack(Material.COOKED_BEEF)));

    }
    @EventHandler
    public void onShopInventoryClick (InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().title().equals(menuTitle)) return;
        Player player = (Player) event.getWhoClicked();

        event.setCancelled(true);

        Material clickedMaterial = event.getCurrentItem().getType();

        switch (clickedMaterial) {
            case DIAMOND_SWORD -> player.performCommand("shop Weapons");
            case DIAMOND_CHESTPLATE -> player.performCommand("shop Armor");
            case COOKED_BEEF -> player.performCommand("shop Utilities");
        }
    }
}