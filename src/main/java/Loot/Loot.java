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

public class Loot implements Listener{
    private static ArrayList<Integer> runesPool;
    private static ArrayList<ItemStack> itemsPool;
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.BARREL){
            event.setDropItems(false);
            player.sendMessage(Component.text("You broke a BARREL and got loot!").color(NamedTextColor.DARK_AQUA));
            dropItems(player);
        }


    }

    @EventHandler
    public void onBarrelInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock.getType() == Material.BARREL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Break the block, don't click on it");
        }
    }


    public static void dropItems(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        Random rand = new Random();

        world.dropItemNaturally(loc, new ItemStack(Material.ARROW, 3));
        world.dropItemNaturally(loc, new ItemStack(Material.BOW, 1));

        if (rand.nextDouble() > 0.8) {
            world.dropItemNaturally(loc, new ItemStack(Material.GOLDEN_APPLE, 1));
        }

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

        if (rand.nextDouble() > 0.5) {
            ItemStack randomLoot = lootTable.get(rand.nextInt(lootTable.size()));
            world.dropItemNaturally(loc, randomLoot);
        }

        int runesAmount = getRandomRuneAmount();
        player.sendMessage(Component.text("You gained " + runesAmount + " runes!").color(NamedTextColor.GREEN));
        ServerData.getPlayerData(player).updateRunes(runesAmount);
    }

    public static void spawnBarrelWithLoot(String worldName, int x, int y, int z) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Location loc = new Location(world, x, y, z);
        loc.getChunk().load(); // Ensure the chunk is loaded

        Block block = loc.getBlock();
        block.setType(Material.BARREL);

        if (block.getState() instanceof org.bukkit.block.Barrel barrel) {
            barrel.getInventory().addItem(new ItemStack(Material.ARROW, 4));
            barrel.getInventory().addItem(new ItemStack(Material.BOW, 1));
            barrel.update(); // Important: update the block state
        }
    }
    public static void initializeLootPool () {
        runesPool = new ArrayList<>();
        itemsPool = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            runesPool.add(10);
        }
        for (int i = 0; i < 30; i++) {
            runesPool.add(5);
        }
        for (int i = 0; i < 60; i++) {
            runesPool.add(3);
        }
    }
    public static int getRandomRuneAmount () {
        return runesPool.get((int)(Math.random() * runesPool.size()));
    }








}
