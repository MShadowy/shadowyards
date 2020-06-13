//Initially created by Nicke535 and adapted from Magic Lib, liscenced under CC-BY-NC-SA 4.0 (https://creativecommons.org/licenses/by-nc-sa/4.0/)
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;


public class MS_ghostFireHandler extends BaseEveryFrameCombatPlugin {
    private static final String GUIDANCE_MODE_PRIMARY = "INTERCEPT_SWARM";
    private static final String GUIDANCE_MODE_SECONDARY = "REACQUIRE_RANDOM_PROJ";
    
    private static final List<String> VALID_TARGET_TYPES = new ArrayList<>();
    static {
        VALID_TARGET_TYPES.add("FIGHTER");
        VALID_TARGET_TYPES.add("FRIGATE");
	VALID_TARGET_TYPES.add("DESTROYER");
	VALID_TARGET_TYPES.add("CRUISER");
	VALID_TARGET_TYPES.add("CAPITAL");
    }
    
    //The maximum range a target can be re-acquired at, in SU.
    //Note that this is counted from the *original* target by default, not the projectile itself (use _PROJ) for that behaviour
    private static final float TARGET_REACQUIRE_RANGE = 1000f;

    //The maximum angle a target can be re-acquired at, in degrees.
    //90 means 90 degrees to either side, I.E. a hemisphere in front of the projectile. Values 180 and above turns off the limitation altogether
    private static final float TARGET_REACQUIRE_ANGLE = 180f;
    
    private static final float TURN_RATE = 200f;
    private static final float SWAY_AMOUNT_PRIMARY = 30f;
    private static final float SWAY_AMOUNT_SECONDARY = 10f;
    private static final float SWAY_PERIOD_PRIMARY = 2f;
    private static final float SWAY_PERIOD_SECONDARY = 1f;
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    private static final float ONE_TURN_DUMB_INACCURACY = 0f;
    private static final float ONE_TURN_TARGET_INACCURACY = 0f;
    
    private static final int INTERCEPT_ITERATIONS = 4;
    private static final float INTERCEPT_ACCURACY_FACTOR = 1f;
    private static final float GUIDANCE_DELAY_MAX = 0f;
    private static final float GUIDANCE_DELAY_MIN = 0f;
    
    private static final boolean BROKEN_BY_PHASE = false;
    private static final boolean RETARGET_ON_SIDE_SWITCH = true;
    
    //---Internal script variables: don't touch!---
    private DamagingProjectileAPI proj; //The projectile itself
    private CombatEntityAPI target; // Current target of the projectile
    private Vector2f targetPoint; // For ONE_TURN_TARGET, actual target position. Otherwise, an offset from the target's "real" position. Not used for ONE_TURN_DUMB
    private float targetAngle; // Only for ONE_TURN_DUMB, the target angle that we want to hit with the projectile
    private float swayCounter1; // Counter for handling primary sway
    private float swayCounter2; // Counter for handling secondary sway
    private float lifeCounter; // Keeps track of projectile lifetime
    private float estimateMaxLife; // How long we estimate this projectile should be alive
    private float delayCounter; // Counter for delaying targeting
    private Vector2f offsetVelocity; // Only used for ONE_TURN_DUMB: keeps velocity from the ship and velocity from the projectile separate (messes up calculations otherwise)
    private Vector2f lastTargetPos; // The last position our target was located at, for target-reacquiring purposes
    private float actualGuidanceDelay; // The actual guidance delay for this specific projectile
    
    public MS_ghostFireHandler(@NotNull DamagingProjectileAPI proj, CombatEntityAPI target) {
	this.proj = proj;
	this.target = target;
	lastTargetPos = target != null ? target.getLocation() : new Vector2f(proj.getLocation());
	swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
	swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
	lifeCounter = 0f;
	estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
	delayCounter = 0f;
	actualGuidanceDelay = MathUtils.getRandomNumberInRange(GUIDANCE_DELAY_MIN, GUIDANCE_DELAY_MAX);

	//For one-turns, we set our target point ONCE and never adjust it
	if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_DUMB")) {
        	targetAngle = proj.getWeapon().getCurrAngle() + MathUtils.getRandomNumberInRange(-ONE_TURN_DUMB_INACCURACY, ONE_TURN_DUMB_INACCURACY);
		offsetVelocity = proj.getSource().getVelocity();
	} else if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_TARGET")) {
		targetPoint = MathUtils.getRandomPointInCircle(getApproximateInterception(25), ONE_TURN_TARGET_INACCURACY);
	}

	//SWARM-type projectiles gets a random offset on the target and targets that point instead of dead-center
	else if (GUIDANCE_MODE_PRIMARY.contains("SWARM") && target != null) {
		applySwarmOffset();
	} else {
		targetPoint = new Vector2f(Misc.ZERO);
	}
    }
    
    //Main advance method
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
	//Sanity checks
	if (Global.getCombatEngine() == null) {
		return;
	}
	if (Global.getCombatEngine().isPaused()) {
		amount = 0f;
	}

	//Checks if our script should be removed from the combat engine
	if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
		Global.getCombatEngine().removePlugin(this);
		return;
	}

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

	//First: are we a one-turn? in that case, skip secondary targeting
	if (!GUIDANCE_MODE_PRIMARY.contains("ONE_TURN")) {
		//Check if we need to find a new target
		if (target != null) {
			if (!Global.getCombatEngine().isEntityInPlay(target)) {
				target = null;
			}
			if (target instanceof ShipAPI) {
				if (((ShipAPI)target).isHulk() || (((ShipAPI) target).isPhased() && BROKEN_BY_PHASE) || (target.getOwner() == proj.getOwner() && RETARGET_ON_SIDE_SWITCH)) {
					target = null;
				}
			}
		}

		//If we need to retarget, check our retarget strategy and act accordingly
		if (target == null) {
			//With no retarget plan, the script just shuts itself off
			if (GUIDANCE_MODE_SECONDARY.equals("NONE")) {
				Global.getCombatEngine().removePlugin(this);
				return;
			}
			//With a disappear target plan, we get rid of both projectile and script
			else if (GUIDANCE_MODE_SECONDARY.equals("DISAPPEAR")) {
				Global.getCombatEngine().removeEntity(proj);
				Global.getCombatEngine().removePlugin(this);
				return;
			}
			//Otherwise, we run retargeting
			else {
				reacquireTarget();
			}
		}

		//Otherwise, we store the location of our target in case we need to retarget next frame
		else {lastTargetPos = new Vector2f(target.getLocation());}
	}

	//If we're using anything that needs a target, and our retargeting failed, just head in a straight line: no script is run
	if (!GUIDANCE_MODE_PRIMARY.contains("ONE_TURN") && target == null) {
		return;
	}

	//Otherwise, we start our guidance stuff...
	else {
		//Dumb one-turns just turn toward an angle, though they also need to compensate for offset velocity to remain straight
		if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_DUMB")) {
			float facingSwayless = proj.getFacing() - swayThisFrame;
			float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, targetAngle) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
			Vector2f pureVelocity = new Vector2f(proj.getVelocity());
			pureVelocity.x -= offsetVelocity.x;
			pureVelocity.y -= offsetVelocity.y;
			proj.setFacing(facingSwayless + swayThisFrame);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless+swayThisFrame).x + offsetVelocity.x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless+swayThisFrame).y + offsetVelocity.y;
		}

		//Target one-turns just turn to point towards their target's position
		else if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_TARGET")) {
			float facingSwayless = proj.getFacing() - swayThisFrame;
			float angleToHit = VectorUtils.getAngle(proj.getLocation(), targetPoint);
			float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
			proj.setFacing(facingSwayless + swayThisFrame);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
		}

		//Dumbchasers just try to point straight at their target at all times
		else if (GUIDANCE_MODE_PRIMARY.contains("DUMBCHASER")) {
			float facingSwayless = proj.getFacing() - swayThisFrame;
			Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
			float angleToHit = VectorUtils.getAngle(proj.getLocation(), Vector2f.add(target.getLocation(), targetPointRotated, new Vector2f(Misc.ZERO)));
			float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
			proj.setFacing(facingSwayless + swayThisFrame);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
		}

		//Interceptors use iterative calculations to find an intercept point to the target
		else if (GUIDANCE_MODE_PRIMARY.contains("INTERCEPT")) {
			//We use fewer calculation steps for projectiles that are very close, as they aren't needed at close distances
			int iterations = INTERCEPT_ITERATIONS;
				float facingSwayless = proj.getFacing() - swayThisFrame;
			Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
			float angleToHit = VectorUtils.getAngle(proj.getLocation(), Vector2f.add(getApproximateInterception(iterations), targetPointRotated, new Vector2f(Misc.ZERO)));
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
	if (VALID_TARGET_TYPES.contains("ASTEROID")) {
		for (CombatEntityAPI potTarget : CombatUtils.getAsteroidsWithinRange(centerOfDetection, TARGET_REACQUIRE_RANGE)) {
			if (potTarget.getOwner() != proj.getOwner() && Math.abs(VectorUtils.getAngle(proj.getLocation(), potTarget.getLocation()) - proj.getFacing()) < TARGET_REACQUIRE_ANGLE) {
				potentialTargets.add(potTarget);
			}
		}
	}
	if (VALID_TARGET_TYPES.contains("MISSILE")) {
		for (CombatEntityAPI potTarget : CombatUtils.getMissilesWithinRange(centerOfDetection, TARGET_REACQUIRE_RANGE)) {
			if (potTarget.getOwner() != proj.getOwner() && Math.abs(VectorUtils.getAngle(proj.getLocation(), potTarget.getLocation()) - proj.getFacing()) < TARGET_REACQUIRE_ANGLE) {
				potentialTargets.add(potTarget);
			}
		}
	}
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
		if (GUIDANCE_MODE_SECONDARY.contains("REACQUIRE_NEAREST")) {
			for (CombatEntityAPI potTarget : potentialTargets) {
				if (newTarget == null) {
					newTarget = potTarget;
				} else if (MathUtils.getDistance(newTarget, centerOfDetection) > MathUtils.getDistance(potTarget, centerOfDetection)) {
					newTarget = potTarget;
				}
			}
		} else if (GUIDANCE_MODE_SECONDARY.contains("REACQUIRE_RANDOM")) {
			newTarget = potentialTargets.get(MathUtils.getRandomNumberInRange(0, potentialTargets.size()-1));
		}

		//Once all that is done, set our target to the new target and select a new swarm point (if appropriate)
		target = newTarget;
		if (GUIDANCE_MODE_PRIMARY.contains("SWARM")) {
			applySwarmOffset();
		}
	}
    }

    //Iterative intercept point calculation: has option for taking more or less calculation steps to trade calculation speed for accuracy
    private Vector2f getApproximateInterception(int calculationSteps) {
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
}
