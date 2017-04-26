package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.util.MS_Utils;
import data.scripts.util.MS_effectsHook;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_shamashFlaresFlakEffect extends BaseEveryFrameCombatPlugin {
    
    private final static String MISS_IDS = "ms_flare_shamash";
    private static final float flareFuseRange = 30f;
    private static final float flareFlakSize = 65f;
    private static final float flareCoreSize = 30f;
    private static final float flareDamage = 100f;
    private static final Color effectColor1 = new Color(210, 125, 105, 215);
    private static final Color effectColor2 = new Color(85, 35, 50, 150);
    private static final float flareFlashDur = 0.2f;
    private static final Vector2f ZERO = new Vector2f();
    
    public static void burstFlakExplode(DamagingProjectileAPI projectile, Vector2f point, CombatEngineAPI engine)
    {
        if (point == null)
        {
            return;
        }

        MS_effectsHook.createFlakShockwave(point);

        engine.addHitParticle(point, ZERO, flareCoreSize, 1f, flareFlashDur, Color.WHITE);
        engine.addHitParticle(point, ZERO, flareFlakSize, 0.4f, 0.6f, effectColor1);
        Vector2f vel = new Vector2f();
        for (int i = 0; i < 30; i++)
        {
            vel.set(((float) Math.random() * 1.25f + 0.25f) * flareFlakSize, 0f);
            VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
            engine.addSmoothParticle(projectile.getLocation(), vel, (float) Math.random() * 2.5f + 2.5f, 1f,
                    (float) Math.random() * 0.3f + 0.6f, effectColor2);
        }

        StandardLight light = new StandardLight(projectile.getLocation(), ZERO, ZERO, null);
        light.setColor(effectColor1);
        light.setSize(flareFlakSize * 1.1f);
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
                    if (flare.isFading() && (Math.random() < 0.5)) {
                        MS_shamashFlaresFlakEffect.burstFlakExplode(flare, loc, engine);
                        break;
                    }
            
                    List<CombatEntityAPI> toCheck = new LinkedList<>();
                    toCheck.addAll(CombatUtils.getShipsWithinRange(loc, flareFlakSize));
                    toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, flareFlakSize));
            
                    for (CombatEntityAPI entity : toCheck) {
                        // Check for an active phase cloak
                        if ((flare.getOwner() == 0) && (entity.getOwner() != 1)) {
                            continue;
                        }
                        if ((flare.getOwner() == 1) && (entity.getOwner() != 0)) {
                            continue;
                        }
                        if (entity.getCollisionClass() == CollisionClass.NONE) {
                            continue; 
                        }
                
                        float distance = MS_Utils.getActualDistance(loc, entity, true);
                
                        // we do the flak flare bit if a missile is close enough, or the flare is fading
                        if ((distance <= flareFuseRange) && (Math.random() < 0.7)) {
                            MS_shamashFlaresFlakEffect.burstFlakExplode(flare, loc, engine);
                            break;
                        }
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
