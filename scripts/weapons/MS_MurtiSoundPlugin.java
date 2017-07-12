package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_MurtiSoundPlugin implements EveryFrameWeaponEffectPlugin {
    
    private static final Color CHARGEUP_PARTICLE_COLOR = new Color(130, 190, 160, 100);
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 4.0f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 60.0f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 1.0f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 4.0f;
    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 180.0f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.2f;
    private static final float CHARGEUP_PARTICLE_COUNT_FACTOR = 5.0f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.7f;
    
    private static final Vector2f ZERO = new Vector2f();
    
    public static final float MAX_OFFSET = 10f; 
    public static final float SWEEP_INTERVAL = 0.95f;
    private List <Float> ANGLES = new ArrayList();
    
    protected float currOffset = 0;
    protected float timer = 0;
    protected int dir = 1;
    
    private float last_charge_level = 0.0f;
    private boolean charging = false;
    private boolean firing = false;
    private boolean restart = true;
    private boolean runOnce = false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        
        Vector2f point1 = new Vector2f(weapon.getLocation());
        Vector2f offset1 = new Vector2f();
        if (weapon.getSlot().isTurret()) {
            offset1 = new Vector2f(17f, 9f);
        } else if (weapon.getSlot().isHardpoint()) {
            offset1 = new Vector2f(20f, 9f);
        }
        VectorUtils.rotate(offset1, weapon.getCurrAngle(), offset1);
        Vector2f.add(offset1, point1,point1);
        
        Vector2f point2 = new Vector2f(weapon.getLocation());
        Vector2f offset2 = new Vector2f();
        if (weapon.getSlot().isTurret()) {
            offset2 = new Vector2f(17f, -9f);
        } else if (weapon.getSlot().isHardpoint()) {
            offset2 = new Vector2f(20f, -9f);
        }
        VectorUtils.rotate(offset2, weapon.getCurrAngle(), offset2);
        Vector2f.add(offset2, point2,point2);
        
        float charge_level = weapon.getChargeLevel();
        
        if (!runOnce) {
            runOnce = true;
            ANGLES=new ArrayList<>(weapon.getSpec().getTurretAngleOffsets());
            
            weapon.ensureClonedSpec();
        }
                    
        if (restart) {
            for(int i=0; i<weapon.getSpec().getTurretAngleOffsets().size(); i++){
                weapon.getSpec().getHardpointAngleOffsets().set(i, ANGLES.get(i));
                weapon.getSpec().getTurretAngleOffsets().set(i, ANGLES.get(i));
                weapon.getSpec().getHiddenAngleOffsets().set(i, ANGLES.get(i));
            }
        }
        
        if(weapon.getChargeLevel()>=0.99f){
            restart=false;
            timer+=amount*SWEEP_INTERVAL;
            for(int i=0; i<weapon.getSpec().getTurretAngleOffsets().size(); i++){            
                weapon.getSpec().getHardpointAngleOffsets().set(i, (-1+2*i)*MAX_OFFSET*-1*(float)FastTrig.cos(timer));            
                weapon.getSpec().getTurretAngleOffsets().set(i, (-1+2*i)*MAX_OFFSET*-1*(float)FastTrig.cos(timer));            
                weapon.getSpec().getHiddenAngleOffsets().set(i, (-1+2*i)*MAX_OFFSET*-1*(float)FastTrig.cos(timer));  
            }
        }else if(!restart){
            timer=0;
            restart=true;
        }
        
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
                        Vector2f spawn_location1 = MathUtils.getPointOnCircumference(
                            point1, distance, (angle + weapon.getCurrAngle()));
                        Vector2f spawn_location2 = MathUtils.getPointOnCircumference(
                            point2, distance, (angle + weapon.getCurrAngle() + 45f));
                        // speed from "distance to muzzle" as required by specified duration
                        speed = distance / CHARGEUP_PARTICLE_DURATION;
                        particle_velocity = MathUtils.getPointOnCircumference(
                            weapon.getShip().getVelocity(),
                            speed,
                            180.0f + angle + weapon.getCurrAngle()
                        );
                    
                        engine.addHitParticle(
                            spawn_location1,
                            particle_velocity,
                            size,
                            CHARGEUP_PARTICLE_BRIGHTNESS,
                            CHARGEUP_PARTICLE_DURATION,
                            CHARGEUP_PARTICLE_COLOR
                        );
                        engine.addHitParticle(
                            spawn_location2,
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
                
                restart=false;
                
                if (weapon.isFiring()) {
                    Global.getCombatEngine().addHitParticle(point1, ZERO, (float) Math.random() * 8f + 8f * weapon.getChargeLevel(), weapon.getChargeLevel()
                                                                                                                                             * 0.3f, 0.2f,
                                                        new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(150, 200),
                                                                  MathUtils.getRandomNumberInRange(50, 150), 255));
                    Global.getCombatEngine().addHitParticle(point2, ZERO, (float) Math.random() * 8f + 8f * weapon.getChargeLevel(), weapon.getChargeLevel()
                                                                                                                                             * 0.3f, 0.2f,
                                                        new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(150, 200),
                                                                  MathUtils.getRandomNumberInRange(50, 150), 255));
                }
            } 
        } else {
            if (weapon.getChargeLevel() >0f && weapon.getCooldownRemaining() <= 0f) {
                charging = true;
                Global.getSoundPlayer().playSound("ms_slowBeamH_charge", 1, 1, point1, weapon.getShip().getVelocity());
            }   
        }
        
        last_charge_level = charge_level;
    }
}
