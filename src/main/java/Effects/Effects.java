package Effects;

import org.bukkit.*;

// helper class for everything that has to do with animation effects.
public class Effects {
    public static final Particle.DustOptions black = new Particle.DustOptions(Color.BLACK, 1);
    public static final Particle.DustOptions orange = new Particle.DustOptions(Color.ORANGE, 1);
    public static final Particle.DustOptions red = new Particle.DustOptions(Color.RED, 1);
    public static final Particle.DustOptions yellow = new Particle.DustOptions(Color.YELLOW, 1);

    public static void spawnParticleRing (Location center, int points, double radius, Particle particle, Particle.DustOptions dustOption) {
        double theta = 0;
        for (int i = 0; i < points; i++) {
            double x = center.getX() + radius * Math.cos(theta);
            double z = center.getZ() + radius * Math.sin(theta);
            Bukkit.getWorld("world").spawnParticle(particle, new Location(Bukkit.getWorld("world"), x, center.getY(), z), 1, dustOption);
            theta += (2 * Math.PI) / points;
        }
    }
    public static void spawnParticleLine (Location start, Location end, int points, Particle particle, Particle.DustOptions dustOption) {

    }
}
