package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MS_Utils;
import data.scripts.util.MS_effectsHook;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.ShadowyardsModPlugin;
import data.scripts.hullmods.TEM_LatticeShield;
import java.util.LinkedHashSet;

public class MS_EMPFlakSmall implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    
    private static final float FLAK_DAMAGE = 30f; //Damage
    private static final float FLAK_EMP_DAMAGE = 300f; //EMP Damage
    private static final float EMP_SIZE = 30f; //Area of Effect
    private static final float EMP_CORE = 12f; //Full damage area
    private static final Color EFFECT_COLOR1 = new Color(100, 200, 255, 215);
    private static final Color EFFECT_COLOR2 = new Color(35, 50, 85, 150);
    private static final float EMP_FLASH_DURATION = 0.25f;
    private static final float EMP_FUSE_RANGE = 30f; //"Detonation" radius
    private static final float EMP_VISUAL_SIZE = 15f;
    private static final float EMP_CORE_VISUAL_SIZE = 12f;
    private static final float LOOK_AHEAD_TIME = 0.067f;    // Extrapolate projectile position for this long in look-ahead for collisions
    private static final Vector2f ZERO = new Vector2f();

    private final Set<DamagingProjectileAPI> DO_NOT_EXPLODE = new HashSet<>();
    
    private static final String DATA_KEY_PREFIX = "MS_FlakBurst_";
    
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
        final Set<DamagingProjectileAPI> flakburstProjectiles = localData.flakBurstProjectiles;
 
        flakburstProjectiles.add(projectile);
    }
    
    /*So first off, lets just think about changing this particular section to being about the visual effect,
    so we no longer apply damage through it, with that instead being handled by the advance*/
    public static void flakEMPExplode(DamagingProjectileAPI projectile, Vector2f point, CombatEngineAPI engine)
    {
        if (point == null)
        {
            return;
        }
        
        MS_effectsHook.createEMPShockwave(point);
        
        engine.addHitParticle(point, ZERO, EMP_CORE_VISUAL_SIZE, 1f, EMP_FLASH_DURATION, Color.WHITE);
        engine.addHitParticle(point, ZERO, EMP_VISUAL_SIZE, 0.4f, 0.4f, EFFECT_COLOR1);
        Vector2f vel = new Vector2f();
        for (int i = 0; i < 30; i++)
        {
            vel.set(((float) Math.random() * 1.25f + 0.25f) * EMP_VISUAL_SIZE, 0f);
            VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
            engine.addSmoothParticle(projectile.getLocation(), vel, (float) Math.random() * 2.5f + 2.5f, 1f,
                    (float) Math.random() * 0.3f + 0.6f, EFFECT_COLOR2);
        }
        
        for (int i = 0; i < 3; i++) {
            float angle = (float) Math.random() * 360f;
            float distance = (float) Math.random() * 10f + 20f;
            Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);
            Vector2f point2 = new Vector2f(point);
            engine.spawnEmpArc(projectile.getSource(), point1, new SimpleEntity(point1), new SimpleEntity(point2),
                    DamageType.ENERGY, 0f, 0f, 1000f, null, 15f,
                    EFFECT_COLOR1, EFFECT_COLOR2);
        }
        
        StandardLight light = new StandardLight(projectile.getLocation(), ZERO, ZERO, null);
        light.setColor(EFFECT_COLOR1);
        light.setSize(EMP_VISUAL_SIZE * 1.1f);
        light.setIntensity(0.15f);
        light.fadeOut(0.2f);
        LightShader.addLight(light);
        
        Global.getSoundPlayer().playSound("ms_lemp_shot_impact", 1f, 1f, point, projectile.getVelocity());
        
        List<ShipAPI> ships = CombatUtils.getShipsWithinRange(point, EMP_SIZE);
        List<CombatEntityAPI> targets = CombatUtils.getAsteroidsWithinRange(point, EMP_SIZE);
        //targets.addAll(CombatUtils.getMissilesWithinRange(point, EMP_SIZE));
        List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(point, EMP_SIZE);
        
        Iterator<ShipAPI> iter = ships.iterator();
        while (iter.hasNext())
        {
            ShipAPI ship = iter.next();
            if (ship.getCollisionClass() == CollisionClass.NONE)
            {
                iter.remove();
                continue;
            }

            if (!ship.isFighter() && !ship.isDrone())
            {
                continue;
            }

            boolean remove = false;
            for (ShipAPI shp : ships)
            {
                if (shp.getShield() != null && shp != ship)
                {
                    if (shp.getShield().isWithinArc(ship.getLocation()) && shp.getShield().isOn()
                            && MathUtils.getDistance(ship.getLocation(), shp.getShield().getLocation()) <= shp.getShield().getRadius())
                    {
                        remove = true;
                    }
                }
            }

            if (remove)
            {
                iter.remove();
            }
        }
        
        ships = MS_Utils.getSortedAreaList(point, ships);
        targets.addAll(ships);
        ShipAPI targ = null;
        
        for (CombatEntityAPI tgt : targets)
        {
            /* No friendly fire for flak */
            if (tgt.getOwner() == projectile.getOwner())
            {
                continue;
            }

            float distance = MS_Utils.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > EMP_FUSE_RANGE)
            {
                reduction = (EMP_CORE - distance) / (EMP_CORE - EMP_FUSE_RANGE);
            }

            if (reduction <= 0f)
            {
                continue;
            }
            
            List<CombatEntityAPI> rocks = CombatUtils.getAsteroidsWithinRange(point, EMP_SIZE);
            rocks.addAll(missiles);
            
            if (!rocks.contains(tgt)) {
                targ = (ShipAPI) tgt;
            }

            boolean shieldHit = false;
            if (tgt instanceof ShipAPI)
            {
                ShipAPI ship = (ShipAPI) tgt;
                if (ship.getShield() != null && ship.getShield().isWithinArc(point))
                {
                    shieldHit = true;
                }
            }

            Vector2f damagePoint;
            if (shieldHit)
            {
                ShipAPI ship = (ShipAPI) tgt;
                damagePoint = MathUtils.getPointOnCircumference(null, ship.getShield().getRadius(), VectorUtils.getAngle(ship.getShield().getLocation(), point));
                Vector2f.add(damagePoint, tgt.getLocation(), damagePoint);
            }
            else
            {
                Vector2f projection = VectorUtils.getDirectionalVector(point, tgt.getLocation());
                projection.scale(tgt.getCollisionRadius());
                Vector2f.add(projection, tgt.getLocation(), projection);
                damagePoint = CollisionUtils.getCollisionPoint(point, projection, tgt);
            }
            if (damagePoint == null)
            {
                damagePoint = point;
            }
            
            engine.applyDamage(tgt, damagePoint, FLAK_DAMAGE * reduction, DamageType.ENERGY, FLAK_EMP_DAMAGE * reduction, false, false, projectile.getSource());
        }
        
        for (MissileAPI mtgt : missiles) {
            float distance = MS_Utils.getActualDistance(point, mtgt, true);
            float reduction = 1f;
            if (distance > EMP_FUSE_RANGE)
            {
                reduction = (EMP_CORE - distance) / (EMP_CORE - EMP_FUSE_RANGE);
            }
 
            if (reduction <= 0f)
            {
                continue;
            }
            
            Vector2f damagePoint;
            Vector2f projection = VectorUtils.getDirectionalVector(point, mtgt.getLocation());
            projection.scale(mtgt.getCollisionRadius());
            Vector2f.add(projection, mtgt.getLocation(), projection);
            damagePoint = CollisionUtils.getCollisionPoint(point, projection, mtgt);
           
            //engine.applyDamage(mtgt, damagePoint, FLAK_DAMAGE * reduction, DamageType.ENERGY, FLAK_EMP_DAMAGE * reduction, false, false, projectile.getSource());
            if (targ != null && (targ.getVariant().getHullMods().contains("tem_latticeshield") && ((!ShadowyardsModPlugin.templarsExist || TEM_LatticeShield.shieldLevel(targ) > 0f) || !targ.getVariant().getHullMods().contains("tem_latticeshield")))) 
            {            
            } else {
                if (damagePoint != null) {
                engine.spawnEmpArc(projectile.getSource(), damagePoint, mtgt, mtgt,
                    DamageType.ENERGY,
                    10f * reduction,
                    FLAK_EMP_DAMAGE * reduction,
                    100f,
                    null,
                    10f,
                    EFFECT_COLOR1,
                    EFFECT_COLOR1);
                }
            }
        }
        
        /* Don't want it exploding multiple times, do we?  Also cleans up the look of it */
        engine.removeEntity(projectile);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> flakBurstProjectiles = localData.flakBurstProjectiles;
        
        if (flakBurstProjectiles.isEmpty()) return;
        
        Iterator<DamagingProjectileAPI> iter = flakBurstProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }
        
        iter = flakBurstProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            Vector2f loc = proj.getLocation();

            List<CombatEntityAPI> toCheck = new LinkedList<>();
            List<CombatEntityAPI> asteroids = CombatUtils.getAsteroidsWithinRange(loc, EMP_SIZE);
            toCheck.addAll(CombatUtils.getShipsWithinRange(loc, EMP_SIZE));
            toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, EMP_SIZE));
            toCheck.addAll(asteroids);

            for (CombatEntityAPI entity : toCheck) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue; 
                }
                if (entity == proj.getSource())	// No collision checks with own (firing) ship
                {
                    continue;
                }

                if (entity.getOwner() == proj.getOwner())
                {
                    // Don't check friendly projectiles for disarming / proximity fuse
                    if (entity instanceof DamagingProjectileAPI)
                    {
                        continue;
                    }

                    // ... or friendly fighters and drones
                    else if (entity instanceof ShipAPI)
                    {
                        ShipAPI ship = (ShipAPI) entity;
                        if ((ship.isFighter() || ship.isDrone()) && ship.isAlive() && !ship.getEngineController().isFlamedOut())
                        {
                            continue;
                        }
                    }
                }

                /* Are we about to run into a shield? */
                if (entity.getShield() != null)
                {
                    Vector2f ahead = new Vector2f(loc).translate(proj.getVelocity().getX() * LOOK_AHEAD_TIME,
                                proj.getVelocity().getY() * LOOK_AHEAD_TIME);
                    ShieldAPI shield = entity.getShield();
                    if (CollisionUtils.getCollides(loc, ahead, shield.getLocation(), shield.getRadius())
                                    && shield.isWithinArc(ahead))   // Yes, we are
                    {
                        if (entity.getOwner() == proj.getOwner() || entity.getOwner() > 1)  // Neutral or friendly shield, disarm
                        {
                            DO_NOT_EXPLODE.add(proj);
                        }
                        else	// Hostile shield, blow up
                        {
                            DO_NOT_EXPLODE.add(proj);
                            MS_EMPFlakSmall.flakEMPExplode(proj, loc, engine);
                        }
                        break;
                    }
                }

                /* Handle any neutral or friendly things we're likely to run into if we've started fading out,
                this prevents double hits where we do direct hit damage *and* explode */
                if ((entity.getOwner() == proj.getOwner() || entity.getOwner() > 1))
                {
                    float distance = MS_Utils.getActualDistance(loc, entity, true);
                    if ((distance <= EMP_FUSE_RANGE))
                    {
                        // Look-ahead hax
                        // If we'll impact a neutral or friendly target in 0.067 seconds, deactivate warhead
                        Vector2f ahead = new Vector2f(loc).translate(proj.getVelocity().getX() * LOOK_AHEAD_TIME,
                                    proj.getVelocity().getY() * LOOK_AHEAD_TIME);
                        if (CollisionUtils.getCollisionPoint(loc, ahead, entity) != null)
                        {
                            DO_NOT_EXPLODE.add(proj);
                            break;
                        }
                    }
                }

                /* Don't proximity fuse on asteroids, don't even bother checking them */
                if (asteroids.contains(entity))
                {
                    continue;
                }

                /* Don't explode on neutrals or allies -- unless the projectile is neutral, in which case everything is fair game */
                if ((proj.getOwner() == 0) && (entity.getOwner() != 1))
                {
                    continue;
                }
                if ((proj.getOwner() == 1) && (entity.getOwner() != 0))
                {
                    continue;
                }

                /* Check for targets in range */
                float distance = MS_Utils.getActualDistance(loc, entity, true);
                if ((distance <= EMP_FUSE_RANGE))
                {
                    DO_NOT_EXPLODE.add(proj);
                    MS_EMPFlakSmall.flakEMPExplode(proj, loc, engine);
                }
            }

            /* Detonate at the end-of-life, like real flak */
            if (proj.isFading() && !DO_NOT_EXPLODE.contains(proj))
            {
                DO_NOT_EXPLODE.add(proj);
                MS_EMPFlakSmall.flakEMPExplode(proj, loc, engine);        
            }
        }
    }
    
    private static final class LocalData {
        final Set<DamagingProjectileAPI> flakBurstProjectiles = new LinkedHashSet<>(100);
    }
}
