package GUIs;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.Bukkit.createInventory;

// the base class for the menu, with some base helper functions. All menus extend this class
public class Menu {
    protected Inventory menu;
    protected Component menuTitle;
    protected ItemStack[][] menuGrid;
    public Menu () {

    }
    public void setGuiSlot (int slot, ItemStack item) {
        menu.setItem(slot, item);
    }
    public ItemStack createButton (Component text, ItemStack button) {
        ItemMeta meta = button.getItemMeta();
        meta.displayName(text);
        button.setItemMeta(meta);

        return button;
    }
    public void openMenu (Player player) {
        player.openInventory(menu);
    }
}
