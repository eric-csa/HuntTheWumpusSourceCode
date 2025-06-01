package Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

// This class listens for players changing the item they hold, and if it is a filled map,
// it sets the map view to the custom Wumpus cave map so it updates with the custom renderer.
public class WumpusMapManager implements Listener {
    private MapView mapView;                // The custom map view to display the Wumpus cave map
    private WumpusCaveMapRenderer renderer; // Renderer that draws the cave map

    // Constructor creates a new custom map and sets up the renderer
    public WumpusMapManager() {
        renderer = new WumpusCaveMapRenderer();
        // Create a new map in the first loaded world
        mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
        mapView.getRenderers().clear();    // Remove default renderers to avoid conflicts
        mapView.addRenderer(renderer);     // Add the custom Wumpus cave renderer
    }

    // Event handler: when a player switches the item slot they are holding
    @EventHandler
    public void onPlayerHoldMap(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        // Get the item in the new slot the player is switching to
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        // Check if the item is a filled map
        if (item != null && item.getType() == Material.FILLED_MAP) {
            System.out.println("Updating Map");
            // Cast to MapMeta to access map-specific data
            MapMeta meta = (MapMeta) item.getItemMeta();
            if (meta != null) {
                // Set the map view to the custom map so it uses your renderer
                meta.setMapView(mapView);
                item.setItemMeta(meta);
            }
        }
    }
}
