package Hexagon;

// The Hexagon class is a helper that generates and stores a pixel-art representation 
// of a hexagon shape in a 2D int array. It is used to render hexagons onto a player map.
public class Hexagon {
    private int[][] hexArray; // 2D array representing pixels of the hexagon; 1 means colored (part of hex), 0 means empty.
    private int size; // The size of the hexagon or drawing area (not initialized here, may be set externally).
    
    // Array representing how many rows to repeat for each line while expanding or contracting the hex sides.
    private int[] repeats = {2, 2, 2, 1, 2, 2, 1, 2, 2, 2};
    
    // Variables that track the start and end column indices for drawing the hex sides on each row.
    private int start = 12;
    private int end = 34;

    // Constructor that initializes the hexArray to fixed size 41x47 and triggers hex generation.
    // The boolean parameter controls whether the hexagon is filled or only an outline.
    public Hexagon(boolean isFullHexagon) {
        hexArray = new int[41][47]; // Initialize the array to hold the hex pattern.
        generateHexagon(isFullHexagon); // Generate the hex pattern based on fill option.
        start = 12; // Reset start index after generation.
        end = 34;   // Reset end index after generation.
    }

    // Private method that generates a hexagon shape in the hexArray.
    // If isFullHexagon is true, fills the entire hex shape; otherwise only edges.
    private void generateHexagon(boolean isFullHexagon) { 
        // Draw the top horizontal side of the hexagon at row 1 from 'start' to 'end' columns.
        int row = 1;
        for (int i = start; i <= end; i++) {
            hexArray[row][i] = 1; // Mark pixels as part of the hex.
        }
        row++; // Move to next row for drawing upper expanding edges.

        // Loop through 'repeats' array to expand the hexagon sides vertically.
        for (int i = 0; i < repeats.length; i++) {
            start--; // Decrement start column (expand left side).
            end++;   // Increment end column (expand right side).
            for (int j = 0; j < repeats[i]; j++) { // Repeat rows according to repeats count.
                hexArray[row][start] = 1; // Left edge pixel.
                hexArray[row][end] = 1;   // Right edge pixel.
                if (isFullHexagon) {
                    // If filled hexagon, fill the entire row between start and end.
                    for (int fillSpace = start; fillSpace <= end; fillSpace++) {
                        hexArray[row][fillSpace] = 1;
                    }
                }
                row++; // Move to next row.
            }
        }

        // Draw the middle narrow points of the hexagon at row 20 (left and right ends).
        hexArray[20][1] = 1;
        hexArray[20][45] = 1;

        // Fill the last upper part if full hexagon is requested.
        if (isFullHexagon) {
            for (int fillSpace = start; fillSpace <= end; fillSpace++) {
                hexArray[row][fillSpace] = 1;
            }
        }

        // Prepare variables for bottom half of the hexagon.
        start = 1;  // Reset start to left narrow column.
        end = 45;   // Reset end to right narrow column.
        row = 21;   // Start drawing bottom half from row 21.

        // Loop backwards through repeats to contract the hexagon sides vertically (bottom half).
        for (int i = repeats.length - 1; i >= 0; i--) {
            start++; // Increment start column (contract left side).
            end--;   // Decrement end column (contract right side).
            for (int j = 0; j < repeats[i]; j++) { // Repeat rows according to repeats count.
                hexArray[row][start] = 1; // Left edge pixel.
                hexArray[row][end] = 1;   // Right edge pixel.
                if (isFullHexagon) {
                    // If filled hexagon, fill entire row between start and end.
                    for (int fillSpace = start; fillSpace <= end; fillSpace++) {
                        hexArray[row][fillSpace] = 1;
                    }
                }
                row++; // Move to next row.
            }
        }

        // Draw the bottom horizontal side of the hexagon at final row from 'start' to 'end' columns.
        start = 12; // Reset to initial start.
        end = 34;   // Reset to initial end.
        for (int i = start; i <= end; i++) {
            hexArray[row][i] = 1; // Mark bottom side pixels.
        }
    }

    // Prints the hexagon to the console using '#' for filled pixels and '.' for empty.
    // Loops over 'size' which should be set externally for correct output.
    public void printHexagon() {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                System.out.print(hexArray[y][x] == 1 ? "# " : ". ");
            }
            System.out.println();
        }
    }

    // Getter method for accessing the hexArray pattern.
    public int[][] getHexArray () {
        return hexArray;
    }
}
