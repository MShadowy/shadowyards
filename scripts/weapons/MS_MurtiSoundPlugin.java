package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_MurtiSoundPlugin implements EveryFrameWeaponEffectPlugin {
    
    private static final Color CHARGEUP_PARTICLE_COLOR = new Color(130, 190, 160, 100);
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 4.0f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 40.0f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 1.0f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 4.0f;
    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 180.0f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.15f;
    private static final float CHARGEUP_PARTICLE_COUNT_FACTOR = 7.0f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.7f;
    
    private static final Vector2f ZERO = new Vector2f();
        
    private float last_charge_level = 0.0f;
    //private float baseBeam = 0f;
    private boolean charging = false;
    private boolean firing = false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        Vector2f point = new Vector2f(weapon.getLocation());
        Vector2f offset = new Vector2f();
        if (weapon.getSlot().isTurret()) {
            offset = new Vector2f(23f, 5f);
        } else if (weapon.getSlot().isHardpoint()) {
            offset = new Vector2f (17f, 5f);
        }
        VectorUtils.rotate(offset, weapon.getCurrAngle(), offset);
        Vector2f.add(offset, point,point);
        
        float charge_level = weapon.getChargeLevel();
        
        if (charging) {
            if (firing && weapon.getCooldownRemaining() <= 0f && weapon.getChargeLevel() <1f) {
                charging = false;
                firing = false;
            } else if (weapon.getChargeLevel() < 1f && weapon.getCooldownRemaining() <= 0f) {
                if (charge_level > last_charge_level) {
                    if (charge_level > last_charge_level && weapon.isFiring()) {
                        // do chargeup particles, number based on charge level                           
                        int particle_count = (int) (CHARGEUP_PARTICLE_COUNT_FACTOR * charge_level);
                        float distance, size, angle, speed;
                        Vector2f particle_velocity;
                        for (int i = 0; i < particle_count; ++i) {
                        // distance from muzzle
                        distance = MathUtils.getRandomNumberInRange(
                            CHARGEUP_PARTICLE_DISTANCE_MIN,
                            CHARGEUP_PARTICLE_DISTANCE_MAX);
                        // particle size
                        size = MathUtils.getRandomNumberInRange(
                            CHARGEUP_PARTICLE_SIZE_MIN,
                            CHARGEUP_PARTICLE_SIZE_MAX);
                        // angle (spread in virtual firing arc) reversed
                        angle = MathUtils.getRandomNumberInRange(
                            -0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD,
                            0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD);
                        // spawn location
                        Vector2f spawn_location = MathUtils.getPointOnCircumference(
                            point, distance, (angle + weapon.getCurrAngle()));
                        // speed from "distance to muzzle" as required by specified duration
                        speed = distance / CHARGEUP_PARTICLE_DURATION;
                        particle_velocity = MathUtils.getPointOnCircumference(
                            weapon.getShip().getVelocity(),
                            speed,
                            180.0f + angle + weapon.getCurrAngle()
                        );
                    
                        engine.addHitParticle(
                            spawn_location,
                            particle_velocity,
                            size,
                            CHARGEUP_PARTICLE_BRIGHTNESS,
                            CHARGEUP_PARTICLE_DURATION,
                            CHARGEUP_PARTICLE_COLOR
                        );
                        }
                    }
                }
            } else {
                firing = true;
                
                if (weapon.isFiring()) {
                    Global.getCombatEngine().addHitParticle(point, ZERO, (float) Math.random() * 15f + 15f * weapon.getChargeLevel(), weapon.getChargeLevel()
                                                                                                                                             * 0.3f, 0.2f,
                                                        new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(150, 200),
                                                                  MathUtils.getRandomNumberInRange(50, 150), 255));
                }
            } 
        } else {
            if (weapon.getChargeLevel() >0f && weapon.getCooldownRemaining() <= 0f) {
                charging = true;
                Global.getSoundPlayer().playSound("ms_slowBeamM_charge", 1, 1, point, weapon.getShip().getVelocity());
            }   
        }
        
        
        
        last_charge_level = charge_level;
    }
    
}
