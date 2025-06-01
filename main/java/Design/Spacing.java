package Design;

public class Spacing {
    // helper class to easily add spaces to text.
    public static String space(int amount) {
        String spaces = "";

        for (int i = 0; i < amount; i++) {
            spaces += " ";
        }

        return spaces;
    }
}
