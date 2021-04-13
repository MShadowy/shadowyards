package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_WavebeamOnHitEffect implements OnHitEffectPlugin {

    // The sound the projectile makes if it deals extra damage
    private static final String SOUND_ID = "disabled_large_crit";
    // The damage types that the extra damage can deal (randomly selected)
    private static final DamageType[] TYPES
            = {
                DamageType.ENERGY
            };
    // How likely it is that the extra damage will be applied (1 = 100% chance)
    private static final float EXTRA_DAMAGE_CHANCE = 0.3f;
    private static final Color EXPLOSION_COLOR = new Color(125, 155, 115, 255);
    private static final Color PARTICLE_COLOR = new Color(125, 155, 115, 255);
    private static final int NUM_PARTICLES = 14;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI apply, CombatEngineAPI engine) {
        // Check if we hit a ship (not its shield)
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= EXTRA_DAMAGE_CHANCE) {
            // Apply extra damage of a random type
            engine.applyDamage(target, point,
                    200, TYPES[(int) (Math.random() * TYPES.length)], 0f, false,
                    true, projectile.getSource());

            // Spawn visual effects
            engine.spawnExplosion(point, (Vector2f) new Vector2f(target.getVelocity()).scale(.48f), EXPLOSION_COLOR, 39f, 1f);
            float speed = projectile.getVelocity().length();
            float facing = 400f;
            for (int x = 0; x < NUM_PARTICLES; x++) {
                engine.addHitParticle(point, MathUtils.getPointOnCircumference(
                        null, MathUtils.getRandomNumberInRange(speed * .007f, speed * .17f),
                        MathUtils.getRandomNumberInRange(facing - 180f, facing + 180f)),
                        5f, 1f, 1.6f, PARTICLE_COLOR);
            }

            // Sound follows enemy that was hit
            Global.getSoundPlayer().playSound(SOUND_ID, 1.1f, 0.5f, target.getLocation(), target.getVelocity());
        }
    }
}