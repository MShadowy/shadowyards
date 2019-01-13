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

public class MS_phaseGunEnergyBomb extends BaseEveryFrameCombatPlugin {
    
    private static final float bombDamage = 400f; //Damage
    private static final float bombSplodeSize = 125f; //Area of Effect
    private static final float bombSplodeCore = 112.5f; //Full damage area
    private static final Color effectColor1 = new Color(210, 125, 105, 215);
    private static final Color effectColor2 = new Color(85, 35, 50, 150);
    private static final float bombFlashDur = 0.25f;
    private static final float bombFuseRange = 50f; //"Detonation" radius
    private final static String MISS_IDS = "ms_phaseblast";
    private static final float bombVisualSize = 125f;
    private static final float bombCoreVisualSize = 50f;
    private static final float LOOK_AHEAD_TIME = 0.067f;    // Extrapolate projectile position for this long in look-ahead for collisions
    private static final Vector2f ZERO = new Vector2f();

    private final Set<DamagingProjectileAPI> DO_NOT_EXPLODE = new HashSet<>();
    
    public static void phaseBombExplode(DamagingProjectileAPI projectile, Vector2f point, CombatEngineAPI engine)
    {
        if (point == null)
        {
            return;
        }

        MS_effectsHook.createRift(point);

        engine.addHitParticle(point, ZERO, bombCoreVisualSize, 1f, bombFlashDur, Color.WHITE);
        engine.addHitParticle(point, ZERO, bombVisualSize, 0.4f, 0.4f, effectColor1);
        Vector2f vel = new Vector2f();
        for (int i = 0; i < 30; i++)
        {
            vel.set(((float) Math.random() * 1.25f + 0.25f) * bombVisualSize, 0f);
            VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
            engine.addSmoothParticle(projectile.getLocation(), vel, (float) Math.random() * 2.5f + 2.5f, 1f,
                    (float) Math.random() * 0.3f + 0.6f, effectColor2);
        }

        StandardLight light = new StandardLight(projectile.getLocation(), ZERO, ZERO, null);
        light.setColor(effectColor1);
        light.setSize(bombVisualSize * 1.1f);
        light.setIntensity(0.15f);
        light.fadeOut(0.2f);
        LightShader.addLight(light);

        Global.getSoundPlayer().playSound("phaseGunGapAsplode", 1f, 1f, point, projectile.getVelocity());

        List<ShipAPI> ships = CombatUtils.getShipsWithinRange(point, bombSplodeSize);
        List<CombatEntityAPI> targets = CombatUtils.getAsteroidsWithinRange(point, bombSplodeSize);
        targets.addAll(CombatUtils.getMissilesWithinRange(point, bombSplodeSize));

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
            if (distance > bombSplodeCore)
            {
                reduction = (bombSplodeSize - distance) / (bombSplodeSize - bombSplodeCore);
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
            engine.applyDamage(tgt, damagePoint, bombDamage * reduction, DamageType.ENERGY, 0f, false, false, projectile.getSource());
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
            DamagingProjectileAPI bomb = projectiles.get(i);
            String spec = bomb.getProjectileSpecId();
            Vector2f loc = bomb.getLocation();
            
            if (spec == null) {
                continue;
            }
            
            switch (spec) {
                case MISS_IDS: {
                    if (bomb.didDamage())
                    {
                        break;
                    }
                    if (DO_NOT_EXPLODE.contains(bomb))
                    {
                        break;
                    }
                    if (bomb.isFading() && (Math.random() < 0.5)) {
                        MS_phaseGunEnergyBomb.phaseBombExplode(bomb, loc, engine);
                        break;
                    }
            
                    List<CombatEntityAPI> toCheck = new LinkedList<>();
                    List<CombatEntityAPI> asteroids = CombatUtils.getAsteroidsWithinRange(loc, bombFuseRange);
                    toCheck.addAll(CombatUtils.getShipsWithinRange(loc, bombFuseRange));
                    toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, bombFuseRange));
                    toCheck.addAll(asteroids);
            
                    for (CombatEntityAPI entity : toCheck) {
                        if (entity.getCollisionClass() == CollisionClass.NONE) {
                            continue; 
                        }
                        if (entity == bomb.getSource()) { // No collision checks with own (firing) ship
                            continue;
                        }
                        //the default script includes sensible checks for a prox detonated flak canister to not kill your own ships or missiles
                        //this is not a warhead, but rather an unstable P-Space instantiation, so beware friendly fire
                
                        /* Are we about to run into a shield? */
                        if (entity.getShield() != null)
                        {
                            Vector2f ahead = new Vector2f(loc).translate(bomb.getVelocity().getX() * LOOK_AHEAD_TIME,
                                    bomb.getVelocity().getY() * LOOK_AHEAD_TIME);
                            ShieldAPI shield = entity.getShield();
                            if (CollisionUtils.getCollides(loc, ahead, shield.getLocation(), shield.getRadius())
                                    && shield.isWithinArc(ahead))   // Yes, we are
                            {
                                DO_NOT_EXPLODE.add(bomb);
                                MS_phaseGunEnergyBomb.phaseBombExplode(bomb, loc, engine);
                                break;
                            }
                        }
                        
                        /* Don't proximity fuse on asteroids, don't even bother checking them */
                        if (asteroids.contains(entity)) {
                            continue;
                        }
                        
                        /* Don't explode on neutrals or allies -- unless the projectile is neutral, in which case everything is fair game */
                        if ((bomb.getOwner() == 0) && (entity.getOwner() != 1))
                        {
                            continue;
                        }
                        if ((bomb.getOwner() == 1) && (entity.getOwner() != 0))
                        {
                            continue;
                        }
                        
                        /* Check for targets in range */
                        float distance = MS_Utils.getActualDistance(loc, entity, true);
                        if ((distance <= bombFuseRange))
                        {
                            DO_NOT_EXPLODE.add(bomb);
                            MS_phaseGunEnergyBomb.phaseBombExplode(bomb, loc, engine);
                            break;
                        }
                    }
                    if (bomb.isFading() && !DO_NOT_EXPLODE.contains(bomb))
                    {
                        DO_NOT_EXPLODE.add(bomb);
                        MS_phaseGunEnergyBomb.phaseBombExplode(bomb, loc, engine);
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
