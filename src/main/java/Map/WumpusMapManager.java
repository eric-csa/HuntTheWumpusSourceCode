package Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

// this class is responsible for updating the map every time the player holds it.
public class WumpusMapManager implements Listener {
    private MapView mapView;
    private WumpusCaveMapRenderer renderer;

    public WumpusMapManager () {
        renderer = new WumpusCaveMapRenderer();
        mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
        mapView.getRenderers().clear();
        mapView.addRenderer(renderer);
    }

    @EventHandler
    public void onPlayerHoldMap(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && item.getType() == Material.FILLED_MAP) {
            System.out.println("Updating Map");
            MapMeta meta = (MapMeta)item.getItemMeta();
            meta.setMapView(mapView);
            item.setItemMeta(meta);
        }
    }
}
