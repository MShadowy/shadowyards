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
import com.fs.starfarer.api.util.IntervalUtil;
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

public class MS_SparkBomber extends BaseEveryFrameCombatPlugin {
    
    private static final float SPARK_COLLISION_WAIT = 1.0f;
    private static final float SPARK_DAMAGE = 400f;
    private static final float SPARK_SPLODE_SIZE = 285f;
    private static final float SPARK_SPLODE_DUR = 0.15f;
    private static final float SPARK_CORE_SIZE = 90f;
    private static final String SPARK_ID = "ms_dSpark";
    
    private static final float LOOK_AHEAD_TIME = 0.067f;
    
    private static final Color FX_COLOR1 = new Color(210, 125, 105, 215);
    private static final Color FX_COLOR2 = new Color(85, 35, 50, 150);
    
    private final List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();
    
    private final IntervalUtil explode = new IntervalUtil (4f, 6f);
    private final IntervalUtil solidify = new IntervalUtil (1f, 1f);
    
    private static final Vector2f ZERO = new Vector2f();
    
    private final Set<DamagingProjectileAPI> DO_NOT_EXPLODE = new HashSet<>();
    
    private static void sparkExplode (DamagingProjectileAPI proj, Vector2f point, CombatEngineAPI engine) {
        if (point == null)
        {
            return;
        }
        
        MS_effectsHook.createRift(point);
        
        engine.addHitParticle(point, ZERO, SPARK_CORE_SIZE, 1f, SPARK_SPLODE_DUR, Color.WHITE);
        engine.addHitParticle(point, ZERO, SPARK_SPLODE_SIZE, 0.4f, 0.25f, FX_COLOR1);
        Vector2f vel = new Vector2f();
        for (int i = 0; i < 30; i++)
        {
            vel.set(((float) Math.random() * 1.25f + 0.25f) * SPARK_SPLODE_SIZE, 0f);
            VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
            engine.addSmoothParticle(proj.getLocation(), vel, (float) Math.random() * 2.5f + 2.5f, 1f,
                    (float) Math.random() * 0.3f + 0.6f, FX_COLOR2);
        }
        
        StandardLight light = new StandardLight(proj.getLocation(), ZERO, ZERO, null);
        light.setColor(FX_COLOR1);
        light.setSize(SPARK_SPLODE_SIZE * 1.1f);
        light.setIntensity(0.15f);
        light.fadeOut(0.2f);
        LightShader.addLight(light);
        
        Global.getSoundPlayer().playSound("phaseGunGapAsplode", 1f, 1f, point, proj.getVelocity());
        
        List<ShipAPI> ships = CombatUtils.getShipsWithinRange(point, SPARK_SPLODE_SIZE);
        List<CombatEntityAPI> targets = CombatUtils.getAsteroidsWithinRange(point, SPARK_SPLODE_SIZE);
        targets.addAll(CombatUtils.getMissilesWithinRange(point, SPARK_SPLODE_SIZE));
        
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
            if (tgt.getOwner() == proj.getOwner())
            {
                continue;
            }
            
            float distance = MS_Utils.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > SPARK_CORE_SIZE)
            {
                reduction = (SPARK_SPLODE_SIZE - distance) / (SPARK_SPLODE_SIZE - SPARK_CORE_SIZE);
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
            engine.applyDamage(tgt, damagePoint, SPARK_DAMAGE * reduction, DamageType.ENERGY, 0f, false, false, proj.getSource());
        }

        /* Don't want it exploding multiple times, do we?  Also cleans up the look of it */
        engine.removeEntity(proj);
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
        for (DamagingProjectileAPI projector : DO_NOT_EXPLODE)
        {
            if (!projectiles.contains(projector))    // No longer exists
            {
                toRemove.add(projector);
            }
        }
        DO_NOT_EXPLODE.removeAll(toRemove);
        
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI spark = projectiles.get(i);
            String spec = spark.getProjectileSpecId();
            Vector2f loc = spark.getLocation();
            
            if (spec == null) {
                continue;
            }
            
            switch (spec) {
                case SPARK_ID: {
                    if (spark.didDamage()) 
                    {
                        break;
                    }
                    if (DO_NOT_EXPLODE.contains(spark))
                    {
                        break;
                    }
                    if (spark.isFading() && (Math.random() < 0.5)) {
                        sparkExplode(spark, loc, engine);
                        break;
                    }
                    
                    if (!alreadyRegisteredProjectiles.contains(spark) && engine.isEntityInPlay(spark) && !spark.didDamage()) {
                        engine.addPlugin(new MS_SparkDirector(spark, null));
                        alreadyRegisteredProjectiles.add(spark);
                    }
                    
                    explode.advance(amount);
                    solidify.advance(amount);
                    
                    float lifeCounter = 0f;
                    lifeCounter += amount;
                    
                    if (spark.getCollisionClass() == CollisionClass.NONE && solidify.intervalElapsed()) {
                        spark.setCollisionClass(CollisionClass.PROJECTILE_FF);
                    }
                    
                    List<CombatEntityAPI> toCheck = new LinkedList<>();
                    List<CombatEntityAPI> asteroids = CombatUtils.getAsteroidsWithinRange(loc, SPARK_CORE_SIZE);
                    toCheck.addAll(CombatUtils.getShipsWithinRange(loc, SPARK_CORE_SIZE));
                    toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, SPARK_CORE_SIZE));
                    toCheck.addAll(asteroids);
                    
                    for (CombatEntityAPI entity : toCheck) {
                        if (entity.getCollisionClass() == CollisionClass.NONE) {
                            continue; 
                        }
                        if (entity == spark.getSource()) { // No collision checks with own (firing) ship
                            continue;
                        }
                        if (lifeCounter < SPARK_COLLISION_WAIT) continue;
                        //the default script includes sensible checks for a prox detonated flak canister to not kill your own ships or missiles
                        //this is not a warhead, but rather an unstable P-Space instantiation, so beware friendly fire
                
                        /* Are we about to run into a shield? */
                        if (entity.getShield() != null)
                        {
                            Vector2f ahead = new Vector2f(loc).translate(spark.getVelocity().getX() * LOOK_AHEAD_TIME,
                                    spark.getVelocity().getY() * LOOK_AHEAD_TIME);
                            ShieldAPI shield = entity.getShield();
                            if (CollisionUtils.getCollides(loc, ahead, shield.getLocation(), shield.getRadius())
                                    && shield.isWithinArc(ahead))   // Yes, we are
                            {
                                DO_NOT_EXPLODE.add(spark);
                                MS_phaseGunEnergyBomb.phaseBombExplode(spark, loc, engine);
                                break;
                            }
                        }
                        
                        /* Don't proximity fuse on asteroids, don't even bother checking them */
                        if (asteroids.contains(entity)) {
                            continue;
                        }
                        
                        /* Don't explode on neutrals or allies -- unless the projectile is neutral, in which case everything is fair game */
                        if ((spark.getOwner() == 0) && (entity.getOwner() != 1))
                        {
                            continue;
                        }
                        if ((spark.getOwner() == 1) && (entity.getOwner() != 0))
                        {
                            continue;
                        }
                        
                        /* Check for targets in range */
                        float distance = MS_Utils.getActualDistance(loc, entity, true);
                        if ((distance <= SPARK_CORE_SIZE))
                        {
                            DO_NOT_EXPLODE.add(spark);
                            MS_SparkBomber.sparkExplode(spark, loc, engine);
                            break;
                        }
                    }
                    if ((spark.isFading() || spark.didDamage()) && !DO_NOT_EXPLODE.contains(spark))
                    {
                        DO_NOT_EXPLODE.add(spark);
                        MS_SparkBomber.sparkExplode(spark, loc, engine);
                        break;
                    }
                    if (explode.intervalElapsed()) {
                        DO_NOT_EXPLODE.add(spark);
                        MS_SparkBomber.sparkExplode(spark, loc, engine);
                        break;
                    }
                    break;
                }
                default:
            }
        }
        
        //And clean up our registered projectile list
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
