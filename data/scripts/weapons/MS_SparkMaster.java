package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.MS_DerazhoFlareEffectPlugin;
import data.scripts.plugins.MS_SparkBomber;
import data.scripts.util.MS_NebulizerPlugin;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_SparkMaster implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    private final Set<DamagingProjectileAPI> alreadyRegisteredProjectiles = new HashSet<>();
    
    private static final String DATA_KEY_PREFIX = "MS_Derazho_";
    private static final String SPARK_ID = "ms_derazhoSpark";
    
    private static final Color PARTICLE_CORE_COLOR = new Color(165, 145, 255, 255);
    
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 4.0f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 60.0f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 2.0f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 8.0f;
    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 180.0f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.2f;
    private static final float CHARGEUP_PARTICLE_COUNT_FACTOR = 3.0f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 1f;
    
    private static final String SPARK_BURST_SOUND = "tanith_split";
    private static final Color SPARK_BURST_COLOR = new Color(85, 35, 50, 255);//(255, 145, 215, 255);
    private static final int NUM_SPARKS = 10;
    
    private boolean charging = false;
    private boolean firing = false;
    protected boolean shouldSplit = true;
    
    private Vector2f loc, vel;
    
    private static final Vector2f NULLVEL = new Vector2f(0, 0);
    
    private float last_charge_level = 0.0f;
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if ((projectile == null) || (weapon == null)) {
            return;
        }
 
        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> derazhoProjectiles = localData.derazhoProjectiles;
 
        derazhoProjectiles.add(projectile);
        engine.addPlugin(new MS_DerazhoFlareEffectPlugin(projectile));
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        chargeUp(amount, engine, weapon);
        
        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> derazhoProjectiles = localData.derazhoProjectiles;
        
        if (derazhoProjectiles.isEmpty()) return;
        
        Iterator<DamagingProjectileAPI> iter = derazhoProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }
        
        iter = derazhoProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            String spec = proj.getProjectileSpecId();
            ShipAPI ship = weapon.getShip();
            
            if (spec == null) {
                iter.remove();
                continue;
            }
            
            if (proj.isFading() || proj.didDamage()) {
                loc = proj.getLocation();
                vel = proj.getVelocity();
                
                if (shouldSplit) {
                    for (int i = 0; i < NUM_SPARKS; i++) {
                        engine.spawnProjectile(ship, weapon, SPARK_ID, loc, (float) i * (360f / (float) NUM_SPARKS), NULLVEL);
                    }

                    MS_NebulizerPlugin.NebulaParams p = MS_NebulizerPlugin.createStandardNebulaParams(SPARK_BURST_COLOR, 25f);
                    p.underglow = new Color(5, 132, 140, 255);
                    MS_NebulizerPlugin.spawnStandardNebula(proj, p);
                    Global.getSoundPlayer().playSound(SPARK_BURST_SOUND, 1f, 1f, loc, vel);
                    
                    shouldSplit = false;
                }
                
                for (DamagingProjectileAPI pj : CombatUtils.getProjectilesWithinRange(loc, 200f)) {
                    if (!alreadyRegisteredProjectiles.contains(pj) && engine.isEntityInPlay(pj) && !pj.didDamage() && !pj.isFading()) {
                        String id = pj.getProjectileSpecId();
                        switch (id) {
                            case "ms_derazhoSpark":
                                engine.addPlugin(new MS_SparkBomber(pj));
                                alreadyRegisteredProjectiles.add(pj);
                                break;
                        }
                    }
                }

                if (!alreadyRegisteredProjectiles.isEmpty() && alreadyRegisteredProjectiles.size() >= NUM_SPARKS) {
                    engine.removeEntity(proj);
                }
                
                if (!Global.getCombatEngine().isEntityInPlay(proj)) {
                    shouldSplit = true;
                }
            }
        }

        //And clean up our registered projectile list
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI pj : cloneList) {
            if (!engine.isEntityInPlay(pj) || pj.didDamage()) {
                alreadyRegisteredProjectiles.remove(pj);
            }
        }
    }
    
    private void chargeUp(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        Vector2f point1 = new Vector2f(weapon.getLocation());
        Vector2f offset1 = new Vector2f();
        if (weapon.getSlot().isTurret()) {
            offset1 = new Vector2f(17f, 0f);
        } else if (weapon.getSlot().isHardpoint()) {
            offset1 = new Vector2f(45f, 0f);
        }
        VectorUtils.rotate(offset1, weapon.getCurrAngle() - 180f, offset1);
        Vector2f.add(offset1, point1, point1);
        
        float chargeLevel = weapon.getChargeLevel();
        
        if (charging) {
            if (firing && weapon.getCooldownRemaining() <= 0f && weapon.getChargeLevel() <1f) {
                charging = false;
                firing = false;
            } else if (weapon.getChargeLevel() < 1f && weapon.getCooldownRemaining() <= 0f) {
                if (chargeLevel > last_charge_level) {
                    int particle_count = (int) (CHARGEUP_PARTICLE_COUNT_FACTOR * chargeLevel);
                    float distance, size, angle, speed;
                    Vector2f particle_velocity;
                    for (int i = 0; i < particle_count; ++i) {
                        distance = MathUtils.getRandomNumberInRange(
                                CHARGEUP_PARTICLE_DISTANCE_MIN,
                                CHARGEUP_PARTICLE_DISTANCE_MAX);
                        size = MathUtils.getRandomNumberInRange(
                                CHARGEUP_PARTICLE_SIZE_MIN,
                                CHARGEUP_PARTICLE_SIZE_MAX);
                        angle = MathUtils.getRandomNumberInRange(
                                -0.25f * CHARGEUP_PARTICLE_ANGLE_SPREAD,
                                0.25f * CHARGEUP_PARTICLE_ANGLE_SPREAD);
                        Vector2f spawn_location1 = MathUtils.getPointOnCircumference(
                                point1, distance, (angle + weapon.getCurrAngle()));
                        Vector2f spawn_location2 = MathUtils.getPointOnCircumference(
                                point1, distance, (angle + weapon.getCurrAngle() - 180f));
                        speed = distance / CHARGEUP_PARTICLE_DURATION;
                        particle_velocity = MathUtils.getPointOnCircumference(
                                weapon.getShip().getVelocity(),
                                speed,
                                180.0f + angle + weapon.getCurrAngle()
                        );
                        engine.addNegativeParticle(
                                spawn_location1,
                                particle_velocity,
                                size,
                                CHARGEUP_PARTICLE_BRIGHTNESS,
                                CHARGEUP_PARTICLE_DURATION,
                                PARTICLE_CORE_COLOR
                        );
                        engine.addHitParticle(
                                spawn_location2,
                                particle_velocity,
                                size,
                                CHARGEUP_PARTICLE_BRIGHTNESS,
                                CHARGEUP_PARTICLE_DURATION,
                                PARTICLE_CORE_COLOR
                        );
                    }
                }
            } else {
                firing = true;
            }
        } else {
            if (weapon.getChargeLevel() >0f && weapon.getCooldownRemaining() <= 0f) {
                charging = true;
            }
        }
        
        last_charge_level = chargeLevel;
    }
    
    private static final class LocalData {
        final Set<DamagingProjectileAPI> derazhoProjectiles = new LinkedHashSet<>(100);
    }
}
