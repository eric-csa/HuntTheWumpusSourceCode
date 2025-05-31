package GUIs.Shop;

import ServerData.ServerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Caves.CaveManager.world;

public class ShopItem {

    private String name;
    private int cost;
    private ItemStack item;
    private String itemType;

    public ShopItem(String name, int cost, Material material) {
        this.name = name;
        this.cost = cost;

        this.item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(NamedTextColor.GOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Price: " + cost + " Runes").color(NamedTextColor.GREEN));
        lore.add(Component.text("Click to Purchase!").color(NamedTextColor.GOLD));
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getItemType() {
        return itemType;
    }
    public static int getPrice (Material material) {
        if (material == null) {
            return -1;
        }
        switch (material) {
            case BOW, DIAMOND_SWORD, DIAMOND_AXE -> {
                return 40;
            }
            case GOLDEN_SWORD, GOLDEN_AXE, LEATHER_HELMET, LEATHER_BOOTS, STONE_SWORD, STONE_AXE -> {
                return 10;
            }
            case LEATHER_LEGGINGS, CHAINMAIL_HELMET, CHAINMAIL_BOOTS -> {
                return 15;
            }
            case IRON_SWORD, IRON_AXE, CHAINMAIL_CHESTPLATE -> {
                return 30;
            }
            case LEATHER_CHESTPLATE, ARROW -> {
                return 20;
            }
            case CHAINMAIL_LEGGINGS -> {
                return 25;
            }

            case COOKED_BEEF -> {
                return 5;
            }
            case GOLDEN_APPLE -> {
                return 30;
            }
            default -> {
                return -1;
            }
        }
    }
    public static void giveItemToPlayer (Material material, Player player) {
        int itemPrice = getPrice(material);
        if (itemPrice == -1) {
            return; //slot clicked is not an item
        }
        if (ServerData.getPlayerData(player).getPlayerRunes() < itemPrice) {
            player.sendMessage(Component.text("You do not have enough runes to buy this item!").color(NamedTextColor.DARK_RED));
            return;
        }
        Material requiredMaterial;

        switch (material) {
            case STONE_SWORD -> requiredMaterial = Material.GOLDEN_SWORD;
            case IRON_SWORD -> requiredMaterial = Material.STONE_SWORD;
            case DIAMOND_SWORD -> requiredMaterial = Material.IRON_SWORD;

            case STONE_AXE -> requiredMaterial = Material.GOLDEN_AXE;
            case IRON_AXE -> requiredMaterial = Material.STONE_AXE;
            case DIAMOND_AXE -> requiredMaterial = Material.IRON_AXE;

            case CHAINMAIL_HELMET -> requiredMaterial = Material.LEATHER_HELMET;
            case CHAINMAIL_CHESTPLATE -> requiredMaterial = Material.LEATHER_CHESTPLATE;
            case CHAINMAIL_LEGGINGS -> requiredMaterial = Material.LEATHER_LEGGINGS;
            case CHAINMAIL_BOOTS -> requiredMaterial = Material.LEATHER_BOOTS;

            default -> requiredMaterial = null;
        }
        if (requiredMaterial != null) {
            if (!ServerData.getPlayerData(player).playerHasItem(requiredMaterial)) {
                player.sendMessage(Component.text("You need to have " + requiredMaterial.name() + " to buy this item!").color(NamedTextColor.DARK_RED));
                return;
            }
            player.getInventory().remove(requiredMaterial);

            ItemStack[] armor = player.getInventory().getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                if (armor[i] != null && armor[i].getType() == requiredMaterial) {
                    armor[i] = null;
                }
            }
            player.getInventory().setArmorContents(armor);
        }
        int emptySlot = ServerData.getPlayerData(player).getEmptyInventorySlot();

        if (emptySlot == -1) {
            player.sendMessage(Component.text("Cannot buy item: Your inventory is full!").color(NamedTextColor.DARK_RED));
            return;
        }
        world.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.0f, 1.0f);
        ItemStack givenItem = new ItemStack(material);
        givenItem.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
        player.getInventory().setItem(emptySlot, givenItem);
        player.sendMessage(Component.text("You bought a " + material.name()).color(NamedTextColor.GREEN));
        ServerData.getPlayerData(player).updateRunes(-itemPrice); // transaction for item
    }
}