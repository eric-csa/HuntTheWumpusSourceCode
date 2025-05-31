package Hexagon;

// the hexagon is a helper class that renders a hexagon pixel art onto the player map.
public class Hexagon {
    private int[][] hexArray; //this is the array that stores the hexagon, a pixel is colored if it stores a side of the hexagon.
    private int size;
    private int[] repeats = {2, 2, 2, 1, 2, 2, 1, 2, 2, 2};
    private int start = 12;
    private int end = 34;

    public Hexagon(boolean isFullHexagon) {
        hexArray = new int[41][47];
        generateHexagon(isFullHexagon);
        start = 12;
        end = 34;
    }

    private void generateHexagon(boolean isFullHexagon) { // 45 side hexagon from minecraftshapes.com for rendering.
        int row = 1;
        for (int i = start; i <= end; i++) {
            hexArray[row][i] = 1;
        }
        row++;
        for (int i = 0; i < repeats.length; i++) {
            start--;
            end++;
            for (int j = 0; j < repeats[i]; j++) {
                hexArray[row][start] = 1;
                hexArray[row][end] = 1;
                if (isFullHexagon) {
                    for (int fillSpace = start; fillSpace <= end; fillSpace++) {
                        hexArray[row][fillSpace] = 1;
                    }
                }
                row++;
            }
        }
        hexArray[20][1] = 1;
        hexArray[20][45] = 1;
        if (isFullHexagon) {
            for (int fillSpace = start; fillSpace <= end; fillSpace++) {
                hexArray[row][fillSpace] = 1;
            }
        }
        start = 1;
        end = 45;
        row = 21;
        for (int i = repeats.length - 1; i >= 0; i--) {
            start++;
            end--;
            for (int j = 0; j < repeats[i]; j++) {
                hexArray[row][start] = 1;
                hexArray[row][end] = 1;
                if (isFullHexagon) {
                    for (int fillSpace = start; fillSpace <= end; fillSpace++) {
                        hexArray[row][fillSpace] = 1;
                    }
                }
                row++;
            }
        }
        start = 12;
        end = 34;
        for (int i = start; i <= end; i++) {
            hexArray[row][i] = 1;
        }
    }
    public void printHexagon() {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                System.out.print(hexArray[y][x] == 1 ? "# " : ". ");
            }
            System.out.println();
        }
    }
    public int[][] getHexArray () {
        return hexArray;
    }
}
