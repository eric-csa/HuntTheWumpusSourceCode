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

// The WumpusCaveMapRenderer renders a custom "Wumpus Cave" map onto a Minecraft map item for the player.
// It visually shows the current room and the adjacent rooms in a hexagonal layout,
// highlighting cleared rooms and displaying their room numbers.
public class WumpusCaveMapRenderer extends MapRenderer {
    private MapView mapView;  // The Minecraft MapView this renderer is associated with
    private Color[][] pixels = new Color[128][128];  // The pixel grid representing the map (Minecraft maps are 128x128)
    
    private int centerX = 40;  // X coordinate of the central hexagon top-left on the pixel grid
    private int centerY = 40;  // Y coordinate of the central hexagon top-left on the pixel grid

    // Displacements from the center hexagon to the 6 surrounding hexagons representing adjacent rooms
    // Order corresponds to: center room, N, NW, NE, SW, SE, S
    private final int[][] displacements = {
        {0, 0},     // center
        {-38, 0},   // north
        {-19, -33}, // northwest
        {-19, 33},  // northeast
        {19, -33},  // southwest
        {19, 33},   // southeast
        {38, 0}     // south
    };

    // Offsets used for positioning digits (room numbers) inside hexagons
    private int leftDigitDisplacementX = 16;
    private int leftDigitDisplacementY = 17;
    private int rightDigitDisplacementX = 16;
    private int rightDigitDisplacementY = 23;

    // Constructor initializes the pixel grid to white and adds the hexagon outlines
    public WumpusCaveMapRenderer() {
        // Fill entire pixel grid with white
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                pixels[x][y] = Color.WHITE;
            }
        }
        addHexagons(); // Draw initial empty hexagons on the grid
    }

    // Adds the six adjacent hexagons (without filling) and the central hexagon to the pixel grid
    public void addHexagons() {
        for (int i = 1; i < displacements.length; i++) {
            // For each adjacent position, add a black hexagon outline
            addHexagon(new Hexagon(false), Color.BLACK,
                       centerX + displacements[i][0],
                       centerY + displacements[i][1]);
        }
        // Add the center hexagon last
        addHexagon(new Hexagon(false), Color.BLACK, centerX, centerY);
    }

    // Adds a single hexagon shape to the pixel grid at the given top-left coordinates in a specified color
    public void addHexagon(Hexagon hexagon, Color color, int topLeftX, int topLeftY) {
        int[][] curHexagon = hexagon.getHexArray(); // Get the pixel representation of the hexagon

        // Loop through the hexagon array and color pixels where the hexagon's shape exists (value == 1)
        for (int i = 0; i < curHexagon.length; i++) {
            for (int j = 0; j < curHexagon[i].length; j++) {
                if (curHexagon[i][j] == 0) continue; // skip empty pixels

                // Set the pixel color on the overall pixel grid with offset (topLeftX, topLeftY)
                pixels[i + topLeftX][j + topLeftY] = color;
            }
        }
    }

    // Adds a digit representing the room number to the pixel grid at specified coordinates
    // This method maps digit integers (0-9) to predefined pixel arrays from Digits class
    public void addDigit(int digit, int topLeftX, int topLeftY) {
        // Based on digit, calls overloaded addDigit with the corresponding digit pixel pattern
        if (digit == 0) addDigit(Digits.ZERO, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 1) addDigit(Digits.ONE, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 2) addDigit(Digits.TWO, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 3) addDigit(Digits.THREE, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 4) addDigit(Digits.FOUR, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 5) addDigit(Digits.FIVE, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 6) addDigit(Digits.SIX, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 7) addDigit(Digits.SEVEN, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 8) addDigit(Digits.EIGHT, Color.BLACK, topLeftX, topLeftY);
        else if (digit == 9) addDigit(Digits.NINE, Color.BLACK, topLeftX, topLeftY);
    }

    // Adds the pixel representation of a digit to the pixel grid with the given color and position
    public void addDigit(int[][] digitPixels, Color color, int topLeftX, int topLeftY) {
        // Loop through the digit pixel array and color pixels accordingly
        for (int i = 0; i < digitPixels.length; i++) {
            for (int j = 0; j < digitPixels[i].length; j++) {
                if (digitPixels[i][j] == 0) continue; // skip empty pixels

                pixels[i + topLeftX][j + topLeftY] = color;
            }
        }
    }

    // The main render method called by Bukkit to draw the map for a player
    // This updates the pixel grid with hexagons and room numbers based on the player's current game state
    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        // Reset the pixel grid to white before drawing
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                pixels[x][y] = Color.WHITE;
            }
        }
        addHexagons(); // Add the outlines of hexagons to the grid

        // Check if the player is currently in a game
        if (ServerData.getPlayerData(player).getGame() != null) {
            int[] adjRooms = ServerData.getPlayerData(player).getGame().getAdjPlayerRooms(); // adjacent room numbers
            
            // Iterate over each adjacent room (excluding center at index 0)
            for (int i = 1; i < displacements.length; i++) {
                int roomNum = adjRooms[i - 1];
                int tensDigit = roomNum / 10;
                int onesDigit = roomNum % 10;

                if (roomNum == -1) {
                    // Room is cleared/invalid - draw a filled black hexagon to mark it
                    addHexagon(new Hexagon(true), Color.BLACK,
                               centerX + displacements[i][0], centerY + displacements[i][1]);
                    continue;
                }

                // If the room is cleared, fill the hexagon green with a black border
                if (ServerData.getPlayerData(player).getGame().getCave().getRoom(roomNum).isCleared()) {
                    addHexagon(new Hexagon(true), Color.GREEN,
                               centerX + displacements[i][0], centerY + displacements[i][1]);
                    addHexagon(new Hexagon(false), Color.BLACK,
                               centerX + displacements[i][0], centerY + displacements[i][1]);
                }

                // Add the two digits of the room number inside the hexagon
                addDigit(tensDigit,
                         centerX + displacements[i][0] + leftDigitDisplacementX,
                         centerY + displacements[i][1] + leftDigitDisplacementY);
                addDigit(onesDigit,
                         centerX + displacements[i][0] + rightDigitDisplacementX,
                         centerY + displacements[i][1] + rightDigitDisplacementY);
            }
        }

        // Finally, paint the pixel grid onto the actual Minecraft map canvas
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                mapCanvas.setPixelColor(j, i, pixels[i][j]);
            }
        }
        //System.out.println("Finished Rendering!");
    }
}