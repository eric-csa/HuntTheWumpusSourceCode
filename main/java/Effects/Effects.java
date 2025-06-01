package Effects;

import org.bukkit.*;

// Helper class for handling all animation effects, especially particle-related visual effects.
public class Effects {

    // Predefined DustOptions using different colors for particle effects
    public static final Particle.DustOptions black = new Particle.DustOptions(Color.BLACK, 1);
    public static final Particle.DustOptions orange = new Particle.DustOptions(Color.ORANGE, 1);
    public static final Particle.DustOptions red = new Particle.DustOptions(Color.RED, 1);
    public static final Particle.DustOptions yellow = new Particle.DustOptions(Color.YELLOW, 1);

    /**
     * Spawns a ring of particles around a center location.
     *
     * @param center       The center location of the ring
     * @param points       The number of points/particles to generate in the ring
     * @param radius       The radius of the ring
     * @param particle     The type of particle to spawn (e.g., REDSTONE)
     * @param dustOption   Custom DustOptions (e.g., color and size) for the particle
     */
    public static void spawnParticleRing(Location center, int points, double radius, Particle particle, Particle.DustOptions dustOption) {
        double theta = 0; // angle in radians
        for (int i = 0; i < points; i++) {
            // Calculate x and z coordinates based on circular placement
            double x = center.getX() + radius * Math.cos(theta);
            double z = center.getZ() + radius * Math.sin(theta);

            // Spawn a single particle at the computed location, at the same Y level as center
            Bukkit.getWorld("world").spawnParticle(
                particle,
                new Location(Bukkit.getWorld("world"), x, center.getY(), z),
                1,
                dustOption
            );

            // Increment angle for next point
            theta += (2 * Math.PI) / points;
        }
    }

    //Placeholder function
    public static void spawnParticleLine(Location start, Location end, int points, Particle particle, Particle.DustOptions dustOption) {
        
    }
}
