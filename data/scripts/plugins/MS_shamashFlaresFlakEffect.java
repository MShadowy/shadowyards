package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.util.MS_Utils;
import data.scripts.util.MS_effectsHook;
import java.awt.Color;
import java.util.ArrayList;
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
import org.lwjgl.util.vector.Vector2f;

public class MS_shamashFlaresFlakEffect extends BaseEveryFrameCombatPlugin {
    
    private static final float flareDamage = 100f;
    private static final float flareFlakSize = 65f;
    private static final float flareFlakCoreSize = 30f;
    private static final Color effectColor1 = new Color(210, 125, 105, 215);
    private static final Color effectColor2 = new Color(85, 35, 50, 150);
    private static final float flareFlashDur = 0.2f;
    private static final float flareFuseRange = 30f;
    private final static String MISS_IDS = "ms_flare_shamash";
    private static final float flareVisualSize = 90f;
    private static final float flareCoreSize = 30f;
    private static final float LOOK_AHEAD_TIME = 0.067f;    // Extrapolate projectile position for this long in look-ahead for collisions
    private static final Vector2f ZERO = new Vector2f();

    private final Set<DamagingProjectileAPI> DO_NOT_EXPLODE = new HashSet<>();
    
    public static void burstFlakExplode(DamagingProjectileAPI projectile, Vector2f point, CombatEngineAPI engine)
    {
        if (point == null)
        {
            return;
        }

        MS_effectsHook.createFlakShockwave(point);

        engine.addHitParticle(point, ZERO, flareCoreSize, 1f, flareFlashDur, Color.WHITE);
        engine.addHitParticle(point, ZERO, flareVisualSize, 0.4f, 0.6f, effectColor1);
        Vector2f vel = new Vector2f();
        for (int i = 0; i < 30; i++)
        {
            vel.set(((float) Math.random() * 1.25f + 0.25f) * flareVisualSize, 0f);
            VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
            engine.addSmoothParticle(projectile.getLocation(), vel, (float) Math.random() * 2.5f + 2.5f, 1f,
                    (float) Math.random() * 0.3f + 0.6f, effectColor2);
        }

        StandardLight light = new StandardLight(projectile.getLocation(), ZERO, ZERO, null);
        light.setColor(effectColor1);
        light.setSize(flareVisualSize * 1.1f);
        light.setIntensity(0.15f);
        light.fadeOut(0.2f);
        LightShader.addLight(light);

        Global.getSoundPlayer().playSound("hit_light", 1f, 1f, point, projectile.getVelocity());

        List<ShipAPI> ships = CombatUtils.getShipsWithinRange(point, flareFlakSize);
        List<CombatEntityAPI> targets = CombatUtils.getAsteroidsWithinRange(point, flareFlakSize);
        targets.addAll(CombatUtils.getMissilesWithinRange(point, flareFlakSize));

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
            if (tgt.getOwner() == projectile.getOwner())
            {
                continue;
            }

            float distance = MS_Utils.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > flareFuseRange)
            {
                reduction = (flareFlakSize - distance) / (flareFlakSize - flareFuseRange);
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
            engine.applyDamage(tgt, damagePoint, flareDamage * reduction, DamageType.FRAGMENTATION, 0f, false, false, projectile.getSource());
        }

        /* Don't want it exploding multiple times, do we?  Also cleans up the look of it */
        engine.removeEntity(projectile);
    }
    private CombatEngineAPI engine;
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        
        /* Clean up do-not-explode set of projectiles as needed */
        List<DamagingProjectileAPI> toRemove = new ArrayList<>();
        for (DamagingProjectileAPI proj : DO_NOT_EXPLODE)
        {
            if (!projectiles.contains(proj))    // No longer exists
            {
                toRemove.add(proj);
            }
        }
        DO_NOT_EXPLODE.removeAll(toRemove);
        
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI flare = projectiles.get(i);
            String spec = flare.getProjectileSpecId();
            Vector2f loc = flare.getLocation();
            if (spec == null) {
                continue;
            }
            
            switch (spec) {
                case MISS_IDS: {
                    /* If the projectile already collided with something it's none of our business */
                    if (flare.didDamage())
                    {
                        break;
                    }
                    if (DO_NOT_EXPLODE.contains(flare))
                    {
                        break;
                    }
            
                    List<CombatEntityAPI> toCheck = new LinkedList<>();
                    List<CombatEntityAPI> asteroids = CombatUtils.getAsteroidsWithinRange(loc, flareFlakSize);
                    toCheck.addAll(CombatUtils.getShipsWithinRange(loc, flareFlakSize));
                    toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, flareFlakSize));
                    toCheck.addAll(asteroids);
            
                    for (CombatEntityAPI entity : toCheck) {
                        if (entity.getCollisionClass() == CollisionClass.NONE) {
                            continue; 
                        }
                        if (entity == flare.getSource())	// No collision checks with own (firing) ship
                        {
                            continue;
                        }
                
                        if (entity.getOwner() == flare.getOwner())
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
                            Vector2f ahead = new Vector2f(loc).translate(flare.getVelocity().getX() * LOOK_AHEAD_TIME,
                                    flare.getVelocity().getY() * LOOK_AHEAD_TIME);
                            ShieldAPI shield = entity.getShield();
                            if (CollisionUtils.getCollides(loc, ahead, shield.getLocation(), shield.getRadius())
                                    && shield.isWithinArc(ahead))   // Yes, we are
                            {
                                if (entity.getOwner() == flare.getOwner() || entity.getOwner() > 1)  // Neutral or friendly shield, disarm
                                {
                                    DO_NOT_EXPLODE.add(flare);
                                }
                                else	// Hostile shield, blow up
                                {
                                    DO_NOT_EXPLODE.add(flare);
                                    MS_shamashFlaresFlakEffect.burstFlakExplode(flare, loc, engine);
                                }
                                break;
                            }
                        }

                        /* Handle any neutral or friendly things we're likely to run into if we've started fading out,
                        this prevents double hits where we do direct hit damage *and* explode */
                        if ((entity.getOwner() == flare.getOwner() || entity.getOwner() > 1))
                        {
                            float distance = MS_Utils.getActualDistance(loc, entity, true);
                            if ((distance <= flareFuseRange))
                            {
                                // Look-ahead hax
                                // If we'll impact a neutral or friendly target in 0.067 seconds, deactivate warhead
                                Vector2f ahead = new Vector2f(loc).translate(flare.getVelocity().getX() * LOOK_AHEAD_TIME,
                                        flare.getVelocity().getY() * LOOK_AHEAD_TIME);
                                if (CollisionUtils.getCollisionPoint(loc, ahead, entity) != null)
                                {
                                    DO_NOT_EXPLODE.add(flare);
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
                        if ((flare.getOwner() == 0) && (entity.getOwner() != 1))
                        {
                            continue;
                        }
                        if ((flare.getOwner() == 1) && (entity.getOwner() != 0))
                        {
                            continue;
                        }

                        /* Check for targets in range */
                        float distance = MS_Utils.getActualDistance(loc, entity, true);
                        if ((distance <= flareFuseRange))
                        {
                            DO_NOT_EXPLODE.add(flare);
                            MS_shamashFlaresFlakEffect.burstFlakExplode(flare, loc, engine);
                            break;
                        }
                    }
                    /* Detonate at the end-of-life, like real flak */
                    if (flare.isFading() && !DO_NOT_EXPLODE.contains(flare))
                    {
                        DO_NOT_EXPLODE.add(flare);
                        MS_shamashFlaresFlakEffect.burstFlakExplode(flare, loc, engine);
                        break;
                    }
                    
                    break;
                }
                default:
            }
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
