package Mobs;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Bomb extends WumpusMob {
    private final Vector bombVelocity = new Vector(0, -0.7, 0);
    public Bomb() {
        super("Bomb");
    }
    public void updateAI () {
        bukkitEntity.setVelocity(bombVelocity);

        if (bukkitEntity.isOnGround()) {
            explode();
        }
    }
    public void explode () {
        useMythicAttack("Explode");
    }
}
