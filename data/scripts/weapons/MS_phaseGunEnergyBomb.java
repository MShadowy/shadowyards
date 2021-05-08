package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.MS_shamashBombPrimer;
import data.scripts.util.MS_Utils;
import data.scripts.util.MS_effectsHook;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_phaseGunEnergyBomb implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {
    
    private static final float BOMB_DAMAGE = 400f; //Damage
    private static final float BOMB_SPLODE_SIZE = 125f; //Area of Effect
    private static final float BOMB_SPLODE_CORE = 112.5f; //Full damage area
    private static final Color FX_COLOR1 = new Color(210, 125, 105, 215);
    private static final Color FX_COLOR2 = new Color(85, 35, 50, 150);
    private static final float BOMB_FLASH_DUR = 0.25f;
    private static final float BOMB_FUSE_RANGE = 50f; //"Detonation" radius
    private static final float BOMB_VISUAL_SIZE = 125f;
    private static final float BOMB_CORE_VISUAL_SIZE = 50f;
    private static final float LOOK_AHEAD_TIME = 0.067f;    // Extrapolate projectile position for this long in look-ahead for collisions
    private static final Vector2f ZERO = new Vector2f();

    private final Set<DamagingProjectileAPI> DO_NOT_EXPLODE = new HashSet<>();
    
    private static final String DATA_KEY_PREFIX = "MS_PhaseRifter_";
    
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
        final Set<DamagingProjectileAPI> phaseBombProjectiles = localData.phaseBombProjectiles;
 
        phaseBombProjectiles.add(projectile);
        engine.addPlugin(new MS_shamashBombPrimer(projectile));
    }
    
    public static void phaseBombExplode(DamagingProjectileAPI projectile, Vector2f point, CombatEngineAPI engine)
    {
        if (point == null)
        {
            return;
        }

        MS_effectsHook.createRift(point);

        engine.addHitParticle(point, ZERO, BOMB_CORE_VISUAL_SIZE, 1f, BOMB_FLASH_DUR, Color.WHITE);
        engine.addHitParticle(point, ZERO, BOMB_VISUAL_SIZE, 0.4f, 0.4f, FX_COLOR1);
        Vector2f vel = new Vector2f();
        for (int i = 0; i < 30; i++)
        {
            vel.set(((float) Math.random() * 1.25f + 0.25f) * BOMB_VISUAL_SIZE, 0f);
            VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
            engine.addSmoothParticle(projectile.getLocation(), vel, (float) Math.random() * 2.5f + 2.5f, 1f,
                    (float) Math.random() * 0.3f + 0.6f, FX_COLOR2);
        }

        StandardLight light = new StandardLight(projectile.getLocation(), ZERO, ZERO, null);
        light.setColor(FX_COLOR1);
        light.setSize(BOMB_VISUAL_SIZE * 1.1f);
        light.setIntensity(0.15f);
        light.fadeOut(0.2f);
        LightShader.addLight(light);

        Global.getSoundPlayer().playSound("phaseGunGapAsplode", 1f, 1f, point, projectile.getVelocity());

        List<ShipAPI> ships = CombatUtils.getShipsWithinRange(point, BOMB_SPLODE_SIZE);
        List<CombatEntityAPI> targets = CombatUtils.getAsteroidsWithinRange(point, BOMB_SPLODE_SIZE);
        targets.addAll(CombatUtils.getMissilesWithinRange(point, BOMB_SPLODE_SIZE));

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

        for (CombatEntityAPI tgt : targets)
        {
            /* No friendly fire for flak */
            /*if (tgt.getOwner() == projectile.getOwner())
            {
                continue;
            }*/
            
            float distance = MS_Utils.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > BOMB_SPLODE_CORE)
            {
                reduction = (BOMB_SPLODE_SIZE - distance) / (BOMB_SPLODE_SIZE - BOMB_SPLODE_CORE);
            }

            if (reduction <= 0f)
            {
                continue;
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
            engine.applyDamage(tgt, damagePoint, BOMB_DAMAGE * reduction, DamageType.ENERGY, 0f, false, false, projectile.getSource());
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
        final Set<DamagingProjectileAPI> phaseBombProjectiles = localData.phaseBombProjectiles;
        
        if (phaseBombProjectiles.isEmpty()) return;
        
        Iterator<DamagingProjectileAPI> iter = phaseBombProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }
        
        iter = phaseBombProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            Vector2f loc = proj.getLocation();

            /*if (!bombs.contains(proj))
            {
                bombs.add(proj);
            }*/

            if (proj.isFading() && (Math.random() < 0.5)) {
                MS_phaseGunEnergyBomb.phaseBombExplode(proj, loc, engine);
            }

            List<CombatEntityAPI> toCheck = new LinkedList<>();
            List<CombatEntityAPI> asteroids = CombatUtils.getAsteroidsWithinRange(loc, BOMB_FUSE_RANGE);
            toCheck.addAll(CombatUtils.getShipsWithinRange(loc, BOMB_FUSE_RANGE));
            toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, BOMB_FUSE_RANGE));
            toCheck.addAll(asteroids);

            for (CombatEntityAPI entity : toCheck) {
                if (entity.getCollisionClass() == CollisionClass.NONE) {
                    continue; 
                }
                if (entity == proj.getSource()) { // No collision checks with own (firing) ship
                    continue;
                }
                //the default script includes sensible checks for a prox detonated flak canister to not kill your own ships or missiles
                //this is not a warhead, but rather an unstable P-Space instantiation, so beware friendly fire

                /* Are we about to run into a shield? */
                if (entity.getShield() != null)
                {
                    Vector2f ahead = new Vector2f(loc).translate(proj.getVelocity().getX() * LOOK_AHEAD_TIME,
                                proj.getVelocity().getY() * LOOK_AHEAD_TIME);
                    ShieldAPI shield = entity.getShield();
                    if (CollisionUtils.getCollides(loc, ahead, shield.getLocation(), shield.getRadius())
                                && shield.isWithinArc(ahead))   // Yes, we are
                    {
                        DO_NOT_EXPLODE.add(proj);
                        MS_phaseGunEnergyBomb.phaseBombExplode(proj, loc, engine);
                        break;
                    }
                }

                /* Don't proximity fuse on asteroids, don't even bother checking them */
                if (asteroids.contains(entity)) {
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
                if ((distance <= BOMB_FUSE_RANGE))
                {
                    DO_NOT_EXPLODE.add(proj);
                    MS_phaseGunEnergyBomb.phaseBombExplode(proj, loc, engine);
                }
            }
            if (proj.isFading() && !DO_NOT_EXPLODE.contains(proj))
            {
                DO_NOT_EXPLODE.add(proj);
                MS_phaseGunEnergyBomb.phaseBombExplode(proj, loc, engine);
            }
        }
    }
    
    /*@Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }*/
    
    private static final class LocalData {
        final Set<DamagingProjectileAPI> phaseBombProjectiles = new LinkedHashSet<>(100);
    }
}
