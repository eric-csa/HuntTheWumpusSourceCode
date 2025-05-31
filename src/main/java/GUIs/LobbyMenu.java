package GUIs;

import ServerData.ServerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.naming.Name;

import static org.bukkit.Bukkit.createInventory;

// the lobby menu. This menu allows the player to start / cancel their hunt.
public class LobbyMenu extends Menu implements Listener {
    public static Component title = Component.text("Lobby Menu").color(NamedTextColor.GREEN);

    public LobbyMenu () {
        menu = createInventory(null, 9, title);
        ItemStack buttonMaterial = new ItemStack(Material.ARROW);
        Component startGameText = Component.text("Start The Hunt!").color(NamedTextColor.GREEN);
        Component cancelGameText = Component.text("Cancel Hunt!").color(NamedTextColor.RED);

        setGuiSlot(4, createButton(startGameText, buttonMaterial));

        buttonMaterial = new ItemStack(Material.BARRIER);

        setGuiSlot(8, createButton(cancelGameText, buttonMaterial));
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().title().equals(title)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            ServerData.getPlayerData(player).startGame();
        }
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            player.sendMessage(Component.text("Canceled Hunt. Returning to Lobby").color(NamedTextColor.RED));
            player.performCommand("kill");
            //ServerData.getPlayerData(player).endGame();
        }
    }
}