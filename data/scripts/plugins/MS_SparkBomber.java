package data.scripts.plugins;

import data.scripts.weapons.MS_phaseGunEnergyBomb;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
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
import org.lwjgl.util.vector.Vector2f;
import data.scripts.util.MS_NebulizerPlugin;
import data.scripts.util.MS_NebulizerPlugin.NebulaParams;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.FastTrig;

public class MS_SparkBomber extends BaseEveryFrameCombatPlugin {
    
    private static final float SPARK_COLLISION_WAIT = 1.0f;
    private static final float SPARK_DAMAGE = 400f;
    private static final float SPARK_SPLODE_SIZE = 285f;
    private static final float SPARK_SPLODE_DUR = 0.15f;
    private static final float SPARK_CORE_SIZE = 90f;
    
    private static final float LOOK_AHEAD_TIME = 0.067f;
    
    private static final Color FX_COLOR1 = new Color(255, 145, 215, 255);
    private static final Color FX_COLOR2 = new Color(85, 35, 50, 255);
    
    private final IntervalUtil explode = new IntervalUtil (1f, 3f);
    
    private static final Vector2f ZERO = new Vector2f();
    
    private final Set<DamagingProjectileAPI> DO_NOT_EXPLODE = new HashSet<>();
    
    private static void sparkExplode (DamagingProjectileAPI proj, Vector2f point, CombatEngineAPI engine) {
        if (point == null)
        {
            return;
        }
        
        MS_effectsHook.createSparkShockwave(point);
        NebulaParams p = MS_NebulizerPlugin.createStandardNebulaParams(FX_COLOR2, 25f);
        p.underglow = new Color(5, 132, 140, 255);
        MS_NebulizerPlugin.spawnStandardNebula(proj, p);
        
        engine.addNegativeParticle(point, ZERO, SPARK_CORE_SIZE, 1f, SPARK_SPLODE_DUR, Color.WHITE);
        engine.addNegativeParticle(point, ZERO, SPARK_SPLODE_SIZE, 0.4f, 0.25f, FX_COLOR1);
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
        
        Global.getSoundPlayer().playSound("tanith_subproj_splode", 1f, 1f, point, proj.getVelocity());
        
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
    private DamagingProjectileAPI proj;
    
    public MS_SparkBomber(@NotNull DamagingProjectileAPI proj) {
        this.proj = proj;
        swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
	swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
        lifeCounter = 0f;
        estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
        targetAngle = proj.getWeapon().getCurrAngle() + MathUtils.getRandomNumberInRange(-0.5f, 0.5f);
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        //Checks if our script should be removed from the combat engine
	if (proj == null || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj)) {
            DO_NOT_EXPLODE.clear();
            Global.getCombatEngine().removePlugin(this);
            return;
	}
        
        sparkDirector(amount, events);
        
        Vector2f loc = proj.getLocation();
            
        if (proj.isFading() && (Math.random() < 0.5)) {
            sparkExplode(proj, loc, engine);
        }
                    
        explode.advance(amount);
                    
        float lifeCounter = 0f;
        lifeCounter += amount;
                    
        List<CombatEntityAPI> toCheck = new LinkedList<>();
        List<CombatEntityAPI> asteroids = CombatUtils.getAsteroidsWithinRange(loc, SPARK_CORE_SIZE);
        toCheck.addAll(CombatUtils.getShipsWithinRange(loc, SPARK_CORE_SIZE));
        toCheck.addAll(CombatUtils.getMissilesWithinRange(loc, SPARK_CORE_SIZE));
        toCheck.addAll(asteroids);
                    
        for (CombatEntityAPI entity : toCheck) {
            if (entity.getCollisionClass() == CollisionClass.NONE) {
                continue; 
            }
            if (entity == proj.getSource()) { // No collision checks with own (firing) ship
                continue;
            }
            if (lifeCounter < SPARK_COLLISION_WAIT) continue;
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
            if ((distance <= SPARK_CORE_SIZE))
            {
                DO_NOT_EXPLODE.add(proj);
                MS_SparkBomber.sparkExplode(proj, loc, engine);
            }
        }
        if ((proj.isFading() || proj.didDamage()) && !DO_NOT_EXPLODE.contains(proj))
        {
            DO_NOT_EXPLODE.add(proj);
            MS_SparkBomber.sparkExplode(proj, loc, engine);
        }
        if (explode.intervalElapsed()) {
            DO_NOT_EXPLODE.add(proj);
            MS_SparkBomber.sparkExplode(proj, loc, engine);
        }
    }
    
    private static final float MIN_SPARK_TURN_INTERVAL = 0.5f;
    private static final float MAX_SPARK_TURN_INTERVAL = 1.5f;
    private static final float SPARK_MAX_TURN = 360f;
    private static final float SPARK_HALF_TURN = SPARK_MAX_TURN / 2;
    
    private static final float SWAY_AMOUNT_PRIMARY = 30f;
    private static final float SWAY_AMOUNT_SECONDARY = 10f;
    
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    
    private float swayCounter1; // Counter for handling primary sway
    private float swayCounter2; // Counter for handling secondary sway
    private float swayThisFrame;
    private float lifeCounter; // Keeps track of projectile lifetime
    private float estimateMaxLife; // How long we estimate this projectile should be alive
    private float targetAngle; // Only for ONE_TURN_DUMB, the target angle that we want to hit with the projectile
    
    private final IntervalUtil nextTurn = new IntervalUtil (0.5f, 1.5f); 
    
    private void sparkDirector(float amount, List<InputEventAPI> events) {
        //Sanity checks
	if (Global.getCombatEngine() == null) {
		return;
	}
	if (Global.getCombatEngine().isPaused()) {
		amount = 0f;
	}

        //Ticks up our life counter: if we miscalculated, also top it off
        lifeCounter+=amount;
        if (lifeCounter > estimateMaxLife) { lifeCounter = estimateMaxLife; }

        swayCounter1 += amount*MIN_SPARK_TURN_INTERVAL;
        swayCounter2 += amount*MAX_SPARK_TURN_INTERVAL;
        swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
                    ((float)(FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + 
                (float)(FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));
                    
        nextTurn.advance(amount);
        
        if (!proj.didDamage() || !proj.isFading() || Global.getCombatEngine().isEntityInPlay(proj)) {
            float facingSwayless = proj.getFacing() - swayThisFrame;
            float turnAngle = proj.getFacing();
            if (nextTurn.intervalElapsed()) {
                turnAngle += (SPARK_MAX_TURN * Math.random()) - SPARK_HALF_TURN;
            }
            float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
            while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
            facingSwayless += Misc.getClosestTurnDirection(facingSwayless, turnAngle) * Math.min(angleDiffAbsolute, 180f * amount);
            proj.setFacing(facingSwayless + swayThisFrame);
            proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
            proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
        }
    }
    
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (Global.getCombatEngine() == null) {
            return;
        }

        String spec = proj.getProjectileSpecId();

        Vector2f Here = new Vector2f(0,0) ;
        Here.x = proj.getLocation().x;
        Here.y = proj.getLocation().y;

        SpriteAPI sprite = Global.getSettings().getSprite("flare", "derazho_ALF");

        if (!Global.getCombatEngine().isPaused()) {
            sprite.setAlphaMult(MathUtils.getRandomNumberInRange(0.9f, 1f));
        } else {
            float tAlf = sprite.getAlphaMult();
            sprite.setAlphaMult(tAlf);
        }
        
        sprite.setSize(200, 25);
        sprite.setAdditiveBlend();
        sprite.renderAtCenter(Here.x, Here.y);
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
