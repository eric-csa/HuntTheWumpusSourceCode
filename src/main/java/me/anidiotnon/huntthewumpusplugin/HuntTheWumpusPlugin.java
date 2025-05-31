package me.anidiotnon.huntthewumpusplugin;


import Caves.CaveManager;
import Commands.*;
import GUIs.GuiManager;
import GUIs.LobbyMenu;
import Loot.Loot;
import Map.WumpusMapManager;
import Rooms.RoomStorage;
import Rulebook.Rulebook;
import Scoreboards.HighScoreManager;
import ServerData.ServerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;

// base plugin class. Responsible for initializing all data, event listeners, and commands.  DO NOT TOUCH
public final class HuntTheWumpusPlugin extends JavaPlugin implements Listener {
    private static GuiManager guiManager;
    private static ServerData serverData;
    private static WumpusMapManager wumpusMapManager;
    private static Loot loot;
    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("HuntTheWumpusPlugin");
    private static File highScoresFile;
    private static FileConfiguration highScoresConfig;
    private static HighScoreManager highScoreManager;

    private static File rulebookFile;
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Hello World!");
        serverData = new ServerData();
        guiManager = new GuiManager();
        LobbyMenu lobbyMenu = new LobbyMenu();
        wumpusMapManager = new WumpusMapManager();
        loot = new Loot();

        RoomStorage.initRoomStorage();
        CaveManager.initCaveManager();
        plugin = Bukkit.getPluginManager().getPlugin("HuntTheWumpusPlugin");
        initializeFileConfigurations();

        Rulebook.initializeRulebook();

        try {
            highScoreManager = new HighScoreManager(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().registerEvents(lobbyMenu, this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        Bukkit.getPluginManager().registerEvents(serverData, this);
        Bukkit.getPluginManager().registerEvents(wumpusMapManager, this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        Bukkit.getPluginManager().registerEvents(loot, this);
        Bukkit.getPluginManager().registerEvents(guiManager.getShopMenu(), this);
        Bukkit.getPluginManager().registerEvents(guiManager.getArmorShopMenu(), this);
        Bukkit.getPluginManager().registerEvents(guiManager.getUtilitiesShopMenu(), this);
        Bukkit.getPluginManager().registerEvents(guiManager.getWeaponShopMenu(), this);

        Loot.initializeLootPool();

        getCommand("OpenLobbyMenu").setExecutor(new OpenLobbyMenu());
        getCommand("lobby").setExecutor(new Lobby());
        getCommand("Spawn").setExecutor(new Spawn());
        getCommand("shop").setExecutor(new Shop());
        getCommand("giverunes").setExecutor(new GiveRunes());
        getCommand("loadhighscores").setExecutor(new LoadHighScores());
        getCommand("kill").setExecutor(new Kill());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveHighScoresConfig();
    }
    // example of an event listener. Reference for Arnav
    @EventHandler
    public void onPlayerClick (PlayerInteractEvent event) {
        Component message = Component.text("Team").color(NamedTextColor.RED);
        // event.getPlayer().sendMessage(message);
    }
    public static GuiManager getGuiManager () {
        return guiManager;
    }

    public static Plugin getPlugin () {
        return plugin;
    }

    public static void print(String message) {
       plugin.getLogger().info(message);
    }

    public void initializeFileConfigurations () {
        highScoresFile = new File(getDataFolder(), "highscores.yml");

        if (!highScoresFile.exists()) {
            highScoresFile.getParentFile().mkdirs(); // redundancy
            saveResource("highscores.yml", false);
        }

        highScoresConfig = YamlConfiguration.loadConfiguration(highScoresFile);

        rulebookFile = new File(getDataFolder(), "rulebook.yml");
    }
    public static File getHighScoresFile () {
        return highScoresFile;
    }
    public static File getRulebookFile () {
        return rulebookFile;
    }
    public static FileConfiguration getHighScoresConfig () {
        return highScoresConfig;
    }
    public static void saveHighScoresConfig() {
        try {
            highScoresConfig.save(highScoresFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save highscores.yml!");
            e.printStackTrace();
        }
    }
    public static HighScoreManager getHighScoreManager () {
        return highScoreManager;
    }
}