package GUIs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

// GuiManager manages all of the different game menus in Hunt The Wumpus.
public class GuiManager implements Listener {
    private LobbyMenu lobbyMenu;
    private GameMenu gameMenu;
    private ShopMenu shopMenu;
    private WeaponShopMenu weaponShopMenu;
    private ArmorShopMenu armorShopMenu;
    private UtilitiesShopMenu utilitiesShopMenu;
    public GuiManager () {
        lobbyMenu = new LobbyMenu();
        gameMenu = new GameMenu();
        shopMenu = new ShopMenu();
        weaponShopMenu = new WeaponShopMenu();
        armorShopMenu = new ArmorShopMenu();
        utilitiesShopMenu = new UtilitiesShopMenu();
    }
    // checks if the player has opened the lobby menu, if so, render the menu for the player.
    @EventHandler
    public void onLobbyMenuOpen (PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() == Material.NETHER_STAR) {
            event.getPlayer().performCommand("openlobbymenu");
        }
    }
    @EventHandler
    public void onShopMenuOpen (PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() == Material.EMERALD) {
            event.getPlayer().performCommand("shop");
        }
    }
    @EventHandler
    public void preventDroppingEssentialItems (PlayerDropItemEvent event) {
        Material itemMaterial = event.getItemDrop().getItemStack().getType();
        if (itemMaterial == Material.NETHER_STAR || itemMaterial == Material.FILLED_MAP || itemMaterial == Material.EMERALD) {
            event.getPlayer().sendMessage(Component.text("This item is essential. Do not drop it!").color(NamedTextColor.RED));
            event.setCancelled(true);
        }
    }
    public LobbyMenu getLobbyMenu () {
        return lobbyMenu;
    }
    public GameMenu getGameMenu () {
        return gameMenu;
    }
    public ShopMenu getShopMenu () {
        return shopMenu;
    }
    public WeaponShopMenu getWeaponShopMenu () {
        return weaponShopMenu;
    }
    public ArmorShopMenu getArmorShopMenu () {
        return armorShopMenu;
    }
    public UtilitiesShopMenu getUtilitiesShopMenu () {
        return utilitiesShopMenu;
    }

}