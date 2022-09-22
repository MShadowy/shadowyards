//Initially created by Nicke535 and adapted from Magic Lib, liscenced under CC-BY-NC-SA 4.0 (https://creativecommons.org/licenses/by-nc-sa/4.0/)
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
//import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
//import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public class MS_ghostFireHandler extends BaseEveryFrameCombatPlugin /*implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin*/ {
    
    private static final List<String> VALID_TARGET_TYPES = new ArrayList<>();
    static {
        VALID_TARGET_TYPES.add("FIGHTER");
        VALID_TARGET_TYPES.add("FRIGATE");
	VALID_TARGET_TYPES.add("DESTROYER");
	VALID_TARGET_TYPES.add("CRUISER");
	VALID_TARGET_TYPES.add("CAPITAL");
    }
    
    private static final String GUIDANCE_MODE_PRIMARY = "INTERCEPT";
    private static final String GUIDANCE_MODE_SECONDARY = "REACQUIRE_RANDOM_PROJ";
    
    //The maximum range a target can be re-acquired at, in SU.
    //Note that this is counted from the *original* target by default, not the projectile itself (use _PROJ) for that behaviour
    private static final float TARGET_REACQUIRE_RANGE = 1000f;

    //The maximum angle a target can be re-acquired at, in degrees.
    //90 means 90 degrees to either side, I.E. a hemisphere in front of the projectile. Values 180 and above turns off the limitation altogether
    private static final float TARGET_REACQUIRE_ANGLE = 180f;
    
    private static final float TURN_RATE = 60f;
    private static final float SWAY_AMOUNT_PRIMARY = 120f;
    private static final float SWAY_AMOUNT_SECONDARY = 40f;
    private static final float SWAY_PERIOD_PRIMARY = 4f;
    private static final float SWAY_PERIOD_SECONDARY = 1f;
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    
    private static final int INTERCEPT_ITERATIONS = 4;
    private static final float INTERCEPT_ACCURACY_FACTOR = 1f;
    
    private static final boolean BROKEN_BY_PHASE = false;
    
    //---Internal script variables: don't touch!---
    private DamagingProjectileAPI proj;
    private CombatEntityAPI target;
    private Vector2f targetPoint; // For ONE_TURN_TARGET, actual target position. Otherwise, an offset from the target's "real" position. Not used for ONE_TURN_DUMB
    private float swayCounter1; // Counter for handling primary sway
    private float swayCounter2; // Counter for handling secondary sway
    private float lifeCounter; // Keeps track of projectile lifetime
    private final float estimateMaxLife; // How long we estimate this projectile should be alive
    private float delayCounter; // Counter for delaying targeting
    private Vector2f lastTargetPos; // The last position our target was located at, for target-reacquiring purposes
    private float actualGuidanceDelay; // The actual guidance delay for this specific projectile
    
    //private static final String DATA_KEY_PREFIX = "MS_GhostfireChaser_";
    
    /*@Override
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
        final Set<DamagingProjectileAPI> ghosterProjectiles = localData.ghosterProjectiles;
        
        target = projectile.getSource().getShipTarget();
        if (target != null) {
            applySwarmOffset(target);
        }
 
        ghosterProjectiles.add(projectile);
    }*/
    
    public MS_ghostFireHandler(@NotNull DamagingProjectileAPI proj, CombatEntityAPI target) {
        this.proj = proj;
	this.target = target;
	lastTargetPos = target != null ? target.getLocation() : new Vector2f(proj.getLocation());
	swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
	swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
	lifeCounter = 0f;
	estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
	delayCounter = 0f;
        
        targetPoint = new Vector2f(Misc.ZERO);
    }
    
    //Main advance method
    @Override
    public void advance(float amount, /*CombatEngineAPI engine, WeaponAPI weapon*/ List<InputEventAPI> events) {
	//Sanity checks
	if (Global.getCombatEngine() == null) {
		return;
	}
	if (Global.getCombatEngine().isPaused()) {
		amount = 0f;
	}
        
        if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
            Global.getCombatEngine().removePlugin(this);
            return;
        }
        
        /*final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> ghosterProjectiles = localData.ghosterProjectiles;
        
        if (ghosterProjectiles.isEmpty()) return;
        
        Iterator<DamagingProjectileAPI> iter = ghosterProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }
        
        iter = ghosterProjectiles.iterator();
        while (iter.hasNext()) {*/
            //DamagingProjectileAPI proj = iter.next();
            //String spec = proj.getProjectileSpecId();
            //target = proj.getSource().getShipTarget();
            
            /*if (spec == null) {
                iter.remove();
                continue;
            }
            
            swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
            swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
            lifeCounter = 0f;
            estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
            delayCounter = 0f;*/

            //lastTargetPos = target != null ? target.getLocation() : new Vector2f(proj.getLocation());

            //Ticks up our life counter: if we miscalculated, also top it off
        lifeCounter+=amount;
        if (lifeCounter > estimateMaxLife) { lifeCounter = estimateMaxLife; }

            //Delays targeting if we have that enabled
        if (delayCounter < actualGuidanceDelay) {
            delayCounter+=amount;
            return;
        }

            //Tick the sway counter up here regardless of if we need it or not: helps reduce boilerplate code
        swayCounter1 += amount*SWAY_PERIOD_PRIMARY;
        swayCounter2 += amount*SWAY_PERIOD_SECONDARY;
        float swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
                ((float)(FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float)(FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));

        if (!GUIDANCE_MODE_PRIMARY.contains("ONE_TURN")) {
            if (target != null) {
                if (!Global.getCombatEngine().isEntityInPlay(target)) {
                    target = null;
                }
                if (target instanceof ShipAPI) {
                    if (((ShipAPI)target).isHulk() || (((ShipAPI) target).isPhased() && BROKEN_BY_PHASE)) {
                        target = null;
                    }
                }
            }
            
            if (target == null) {
                reacquireTarget();
            }
            
            else {lastTargetPos = new Vector2f(target.getLocation());}
        }
        
        if (!GUIDANCE_MODE_PRIMARY.contains("ONE_TURN") && target == null) { }
        
        else {
            /*if (target == null) {
                reacquireTarget(lastTargetPos, proj);
            } else { 
                lastTargetPos = new Vector2f(target.getLocation());
            }*/

            //Otherwise, we start our guidance stuff...
            if (GUIDANCE_MODE_PRIMARY.contains("INTERCEPT")) {
                //Interceptors use iterative calculations to find an intercept point to the target
                //We use fewer calculation steps for projectiles that are very close, as they aren't needed at close distances
                int iterations = INTERCEPT_ITERATIONS;
                float facingSwayless = proj.getFacing() - swayThisFrame;
                Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());

                float angleToHit = VectorUtils.getAngle(proj.getLocation(), Vector2f.add(getApproximateInterception(iterations, target, proj), targetPointRotated, new Vector2f(Misc.ZERO)));
                float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
                while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}

                facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
                proj.setFacing(facingSwayless + swayThisFrame);
                proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
                proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
            }
        }
    }

    //Re-acquires a target depending on re-acquiring strategy
    private void reacquireTarget() {
	CombatEntityAPI newTarget = null;
	Vector2f centerOfDetection = lastTargetPos;
	if (GUIDANCE_MODE_SECONDARY.contains("_PROJ")) {
            centerOfDetection = proj.getLocation();
        }
	List<CombatEntityAPI> potentialTargets = new ArrayList<>();
        
	for (ShipAPI potTarget : CombatUtils.getShipsWithinRange(centerOfDetection, TARGET_REACQUIRE_RANGE)) {
		if (potTarget.getOwner() == proj.getOwner()
			|| Math.abs(VectorUtils.getAngle(proj.getLocation(), potTarget.getLocation()) - proj.getFacing()) > TARGET_REACQUIRE_ANGLE
			|| potTarget.isHulk()) {
			continue;
		}
		if (potTarget.isPhased() && BROKEN_BY_PHASE) {
			continue;
		}
		if (potTarget.getHullSize().equals(ShipAPI.HullSize.FIGHTER) && VALID_TARGET_TYPES.contains("FIGHTER")) {
			potentialTargets.add(potTarget);
		}
		if (potTarget.getHullSize().equals(ShipAPI.HullSize.FRIGATE) && VALID_TARGET_TYPES.contains("FRIGATE")) {
			potentialTargets.add(potTarget);
		}
		if (potTarget.getHullSize().equals(ShipAPI.HullSize.DESTROYER) && VALID_TARGET_TYPES.contains("DESTROYER")) {
			potentialTargets.add(potTarget);
		}
		if (potTarget.getHullSize().equals(ShipAPI.HullSize.CRUISER) && VALID_TARGET_TYPES.contains("CRUISER")) {
			potentialTargets.add(potTarget);
		}
		if (potTarget.getHullSize().equals(ShipAPI.HullSize.CAPITAL_SHIP) && VALID_TARGET_TYPES.contains("CAPITAL")) {
			potentialTargets.add(potTarget);
		}
	}
	//If we found any eligible target, continue selection, otherwise we'll have to stay with no target
	if (!potentialTargets.isEmpty()) {
            newTarget = potentialTargets.get(MathUtils.getRandomNumberInRange(0, potentialTargets.size()-1));

            target = newTarget;
            applySwarmOffset();
	}
    }

    //Iterative intercept point calculation: has option for taking more or less calculation steps to trade calculation speed for accuracy
    private Vector2f getApproximateInterception(int calculationSteps, CombatEntityAPI target, DamagingProjectileAPI proj) {
	Vector2f returnPoint = new Vector2f(target.getLocation());
	//Iterate a set amount of times, improving accuracy each time
	for (int i = 0; i < calculationSteps; i++) {
                //Get the distance from the current iteration point and the projectile, and calculate the approximate arrival time
		float arrivalTime = MathUtils.getDistance(proj.getLocation(), returnPoint)/proj.getVelocity().length();

		//Calculate the targeted point with this arrival time
		returnPoint.x = target.getLocation().x + (target.getVelocity().x * arrivalTime * INTERCEPT_ACCURACY_FACTOR);
		returnPoint.y = target.getLocation().y + (target.getVelocity().y * arrivalTime * INTERCEPT_ACCURACY_FACTOR);
	}

	return returnPoint;
    }

    //Used for getting a swarm target point, IE a random point offset on the target. Should only be used when target != null
    private void applySwarmOffset() {
	int i = 40; //We don't want to take too much time, even if we get unlucky: only try 40 times
	boolean success = false;
	while (i > 0 && target != null) {
		i--;
		//Get a random position and check if its valid
		Vector2f potPoint = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
		if (CollisionUtils.isPointWithinBounds(potPoint, target)) {
		//If the point is valid, convert it to an offset and store it
			potPoint.x -= target.getLocation().x;
			potPoint.y -= target.getLocation().y;
			potPoint = VectorUtils.rotate(potPoint, -target.getFacing());
			targetPoint = new Vector2f(potPoint);
			success = true;
			break;
		}
	}

	//If we didn't find a point in 40 tries, just choose target center
	if (!success) {
		targetPoint = new Vector2f(Misc.ZERO);
	}
    }
    
    /*private static final class LocalData {
        final Set<DamagingProjectileAPI> ghosterProjectiles = new LinkedHashSet<>(100);
    }*/
}
