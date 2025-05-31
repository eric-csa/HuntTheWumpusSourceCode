package Map;
import Digits.Digits;
import ServerData.ServerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import Hexagon.Hexagon;

import java.awt.*;

// the Wumpus Cave Map Renderer renders the cave map for the player.
// It shows the player the nearby rooms they can travel to.
public class WumpusCaveMapRenderer extends MapRenderer {
    private MapView mapView;
    private Color[][] pixels = new Color[128][128];
    private int centerX = 40;
    private int centerY = 40;
    private final int[][] displacements = {{0, 0}, {-38, 0}, {-19, -33}, {-19, 33}, {19, -33}, {19, 33}, {38, 0}};
    // displacements array: displacements of: center room, N, NW, NE, SW, SE, S
    // displacements are used to easily calculate where to render the 6 adjacent hexagons by translating
    // the hexagon shape by a certain x and y amount
    private int leftDigitDisplacementX = 16;
    private int leftDigitDisplacementY = 17;
    private int rightDigitDisplacementX = 16;
    private int rightDigitDisplacementY = 23;
    public WumpusCaveMapRenderer() {
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                pixels[x][y] = Color.WHITE;
            }
        }
        addHexagons();
    }
    // adds the adjacent hexagons onto the empty map
    public void addHexagons () {
        for (int i = 1; i < displacements.length; i++) {
            addHexagon(new Hexagon(false), Color.BLACK, centerX + displacements[i][0],
                    centerY + displacements[i][1]);
        }
        addHexagon(new Hexagon(false), Color.BLACK, 40, 40);
    }
    public void addHexagon (Hexagon hexagon, Color color, int topLeftX, int topLeftY) {
        int[][] curHexagon = hexagon.getHexArray();

        for (int i = 0; i < curHexagon.length; i++) {
            for (int j = 0; j < curHexagon[i].length; j++) {
                if (curHexagon[i][j] == 0) {
                    continue;
                }
                pixels[i + topLeftX][j + topLeftY] = color;
            }
        }
    }
    // the add digit adds a digit to display the room number of the nearby room.
    public void addDigit (int digit, int topLeftX, int topLeftY) {
        if (digit == 0) {
            addDigit(Digits.ZERO, Color.black, topLeftX, topLeftY);
        }
        if (digit == 1) {
            addDigit(Digits.ONE, Color.black, topLeftX, topLeftY);
        }
        if (digit == 2) {
            addDigit(Digits.TWO, Color.black, topLeftX, topLeftY);
        }
        if (digit == 3) {
            addDigit(Digits.THREE, Color.black, topLeftX, topLeftY);
        }
        if (digit == 4) {
            addDigit(Digits.FOUR, Color.black, topLeftX, topLeftY);
        }
        if (digit == 5) {
            addDigit(Digits.FIVE, Color.black, topLeftX, topLeftY);
        }
        if (digit == 6) {
            addDigit(Digits.SIX, Color.black, topLeftX, topLeftY);
        }
        if (digit == 7) {
            addDigit(Digits.SEVEN, Color.black, topLeftX, topLeftY);
        }
        if (digit == 8) {
            addDigit(Digits.EIGHT, Color.black, topLeftX, topLeftY);
        }
        if (digit == 9) {
            addDigit(Digits.NINE, Color.black, topLeftX, topLeftY);
        }
    }
    public void addDigit (int[][] digitPixels, Color color, int topLeftX, int topLeftY) {
        for (int i = 0; i < digitPixels.length; i++) {
            for (int j = 0; j < digitPixels[i].length; j++) {
                if (digitPixels[i][j] == 0) {
                    continue;
                }
                pixels[i + topLeftX][j + topLeftY] = color;
            }
        }
    }
    // the minecraft map is represented by a 128 x 128 pixel grid. This render function renders the hunt the wumpus map
    // into the minecraft map.
    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        // update and render the room numbers onto the hexagons
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                pixels[x][y] = Color.WHITE;
            }
        }
        addHexagons();
        if (ServerData.getPlayerData(player).getGame() != null) { // if the player is in a game, render the room numbers
            int[] adjRooms = ServerData.getPlayerData(player).getGame().getAdjPlayerRooms();
            for (int i = 1; i < displacements.length; i++) {
                int tensDigit = adjRooms[i - 1] / 10;
                int onesDigit = adjRooms[i - 1] % 10;
                if (adjRooms[i - 1] == -1) {
                    addHexagon(new Hexagon(true), Color.BLACK, centerX + displacements[i][0], // marks the room as cleared
                            centerY + displacements[i][1]);
                    continue;
                }
                if (ServerData.getPlayerData(player).getGame().getCave().getRoom(adjRooms[i - 1]).isCleared()) {
                    addHexagon(new Hexagon(true), Color.GREEN, centerX + displacements[i][0], // marks the room as cleared
                            centerY + displacements[i][1]);
                    addHexagon(new Hexagon(false), Color.BLACK, centerX + displacements[i][0], // keeps the border black
                            centerY + displacements[i][1]);
                }
                addDigit(tensDigit, // left digit
                        centerX + displacements[i][0] + leftDigitDisplacementX,
                        centerY + displacements[i][1] + leftDigitDisplacementY);
                addDigit(onesDigit,
                        centerX + displacements[i][0] + rightDigitDisplacementX,
                        centerY + displacements[i][1] + rightDigitDisplacementY); // right digit
            }
        }
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                mapCanvas.setPixelColor(j, i, pixels[i][j]);
            }
        }
        //System.out.println("Finished Rendering!");
    }
}
