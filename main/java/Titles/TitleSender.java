package Titles;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

// a helper class to send minecraft titles to certain players.
public class TitleSender {
    public static void sendTitle (String titleString, String subtitleString, Player player, long fadeInDuration,
                                  long stayDuration, long fadeOutDuration) {
        Component title = Component.text(titleString).color(NamedTextColor.DARK_PURPLE);
        Component subtitle = Component.text(subtitleString).color(NamedTextColor.DARK_BLUE);

        // Define title display and fade durations
        Title.Times times = Title.Times.times(Duration.ofSeconds(fadeInDuration), Duration.ofSeconds(stayDuration),
                Duration.ofSeconds(fadeOutDuration));

        // Get audience and send title
        Audience audience = (Audience)player;
        audience.showTitle(Title.title(title, subtitle, times));
    }
    // actual one
    public static void sendTitle (String titleString, String subtitleString, Player player, long fadeInDuration,
                                  long stayDuration, long fadeOutDuration, NamedTextColor titleColor, NamedTextColor subtitleColor) {
        Component title = Component.text(titleString).color(titleColor);
        Component subtitle = Component.text(subtitleString).color(subtitleColor);

        // Define title display and fade durations
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeInDuration), Duration.ofMillis(stayDuration),
                Duration.ofMillis(fadeOutDuration));

        // Get audience and send title
        Audience audience = (Audience)player;
        audience.showTitle(Title.title(title, subtitle, times));
    }
}
