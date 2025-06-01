package Loot;

import GUIs.Shop.ShopItem;
import ServerData.ServerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;

import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import java.util.ArrayList;

// The Loot class listens for player interactions with barrels and handles custom loot drops.
public class Loot implements Listener {
    private static ArrayList<Integer> runesPool; // Pool of rune amounts to randomly give players
    private static ArrayList<ItemStack> itemsPool; // Currently unused, but could hold loot items
    
    // Event handler triggered when a block is broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if the broken block is a barrel
        if (block.getType() == Material.BARREL) {
            event.setDropItems(false); // Prevent default barrel drops
            player.sendMessage(Component.text("You broke a BARREL and got loot!").color(NamedTextColor.DARK_AQUA));
            dropItems(player); // Custom loot drop
        }
    }

    // Event handler triggered when a player right-clicks a block
    @EventHandler
    public void onBarrelInteract(PlayerInteractEvent event) {
        // Only proceed if the action was a right-click on a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block clickedBlock = event.getClickedBlock();
        // If the clicked block is a barrel, cancel interaction and send message
        if (clickedBlock.getType() == Material.BARREL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Break the block, don't click on it");
        }
    }

    // Method to drop items at the player's location as loot
    public static void dropItems(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return; // Safety check

        Random rand = new Random();

        // Always drop arrows and a bow
        world.dropItemNaturally(loc, new ItemStack(Material.ARROW, 3));
        world.dropItemNaturally(loc, new ItemStack(Material.BOW, 1));

        // 20% chance to drop a golden apple
        if (rand.nextDouble() > 0.8) {
            world.dropItemNaturally(loc, new ItemStack(Material.GOLDEN_APPLE, 1));
        }

        // Loot table of possible extra items to drop
        List<ItemStack> lootTable = List.of(
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_HELMET),
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.COOKED_BEEF, 4),
                new ItemStack(Material.EXPERIENCE_BOTTLE, 3),
                new ItemStack(Material.ENCHANTED_BOOK)
        );

        // 50% chance to drop a random item from loot table
        if (rand.nextDouble() > 0.5) {
            ItemStack randomLoot = lootTable.get(rand.nextInt(lootTable.size()));
            world.dropItemNaturally(loc, randomLoot);
        }

        // Give the player a random amount of runes from the runesPool
        int runesAmount = getRandomRuneAmount();
        player.sendMessage(Component.text("You gained " + runesAmount + " runes!").color(NamedTextColor.GREEN));
        ServerData.getPlayerData(player).updateRunes(runesAmount);
    }

    // Method to spawn a barrel with predefined loot in a specified world and coordinates
    public static void spawnBarrelWithLoot(String worldName, int x, int y, int z) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Location loc = new Location(world, x, y, z);
        loc.getChunk().load(); // Ensure the chunk is loaded

        Block block = loc.getBlock();
        block.setType(Material.BARREL);

        // If the block's state is a barrel, add default loot items to its inventory
        if (block.getState() instanceof org.bukkit.block.Barrel barrel) {
            barrel.getInventory().addItem(new ItemStack(Material.ARROW, 4));
            barrel.getInventory().addItem(new ItemStack(Material.BOW, 1));
            barrel.update(); // Update the block state to apply changes
        }
    }

    // Initializes the runesPool with varying amounts of runes (10, 5, and 3)
    public static void initializeLootPool() {
        runesPool = new ArrayList<>();
        itemsPool = new ArrayList<>(); // Currently unused, placeholder for future items pool

        // Add 10 runes, ten times
        for (int i = 0; i < 10; i++) {
            runesPool.add(10);
        }
        // Add 5 runes, thirty times
        for (int i = 0; i < 30; i++) {
            runesPool.add(5);
        }
        // Add 3 runes, sixty times
        for (int i = 0; i < 60; i++) {
            runesPool.add(3);
        }
    }

    // Returns a random rune amount from the runesPool list
    public static int getRandomRuneAmount() {
        return runesPool.get((int)(Math.random() * runesPool.size()));
    }
}
