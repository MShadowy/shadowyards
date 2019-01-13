package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_tagAI implements ShipSystemAIScript {
    /*
    TAG System AI; the Belet should use this pretty aggressively, particularly on anything bigger
    than a Destroyer; Frigates are low priority since they're dodgy little things, Fighters
    are right out
    Shield hits are no good--the TAG can't latch--so unless we can -potentially- hit the hull
    it won't fire
    */
    private static final float RANGE = 800f;
    private static final float RANGE_CHECK = 1000f;
    private static final float ANGLE_TOLERANCE = 10f;
    private static final float RANDOM_USE_CHANCE = 0.9f;
    private static final float FLUX_TOLERANCE = 0.9f;

    private static final float PRESENCE_CAP = 100f;
    private static final float PRESENCE_FLOOR = 0f;
    private static final float FIGHTER_WEIGHT = 1f;
    private static final float FRIGATE_WEIGHT = 4f;
    private static final float DESTROYER_WEIGHT = 8f;
    private static final float CRUISER_WEIGHT = 16f;
    private static final float CAPITAL_WEIGHT = 32f;

    /*
        tongo's changes:
        - made loose floats into static variables for easy editing
        - added the getAlliedPresence() function
        - use the getAlliedPresence in comparing against RANDOM_USE_CHANCE

        the goal was to guarantee that the TAG will be used if conditions are met and there is enough support
        1v1 behavior should be unchanged.
     */
    
    private final IntervalUtil tracker = new IntervalUtil(0.25f, 0.5f);
    private float priority = 0f;
    
    private ShipAPI ship;
    private ShipSystemAPI system;
    
    private static Vector2f intercept(Vector2f point, float speed, Vector2f target, Vector2f targetVel) {
        Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        float a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - speed * speed;
        float b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        float c = difference.x * difference.x + difference.y * difference.y;

        Vector2f solutionSet = quad(a, b, c);

        Vector2f intercept = null;
        if (solutionSet != null) {
            float bestFit = Math.min(solutionSet.x, solutionSet.y);
            if (bestFit < 0) {
                bestFit = Math.max(solutionSet.x, solutionSet.y);
            }
            if (bestFit > 0) {
                intercept = new Vector2f(target.x + targetVel.x * bestFit, target.y + targetVel.y * bestFit);
            }
        }

        return intercept;
    }

    private static Vector2f quad(float a, float b, float c) {
        Vector2f solution = null;
        if (Float.compare(Math.abs(a), 0) == 0) {
            if (Float.compare(Math.abs(b), 0) == 0) {
                solution = (Float.compare(Math.abs(c), 0) == 0) ? new Vector2f(0, 0) : null;
            } else {
                solution = new Vector2f(-c / b, -c / b);
            }
        } else {
            float d = b * b - 4 * a * c;
            if (d >= 0) {
                d = (float) Math.sqrt(d);
                float e = 2 * a;
                solution = new Vector2f((-b - d) / e, (-b + d) / e);
            }
        }
        return solution;
    }
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (Global.getCombatEngine().isPaused() || Global.getCombatEngine() == null) {
            return;
        }

        tracker.advance(amount);
        Vector2f shipLoc = new Vector2f(ship.getLocation());
        
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            
            boolean shouldUseSystem = false;
            
            List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(shipLoc, RANGE);
            for (ShipAPI tracking : nearbyShips) {
                if (tracking.getOwner() == ship.getOwner()) {
                    continue;
                }
                Vector2f track = intercept(ship.getLocation(), RANGE_CHECK, tracking.getLocation(), tracking.getVelocity());
                if (track !=null && ship.getLocation() != null) {
                    priority = MathUtils.getRandomNumberInRange(0.3f, 0.6f);
                }
                float angle = MathUtils.getShortestRotation(ship.getFacing(), Vector2f.angle(ship.getLocation(), tracking.getLocation()));
                ShieldAPI shield = tracking.getShield();
                FluxTrackerAPI fluxer = tracking.getFluxTracker();
                
                //if we can hit the ship and they aren't phased or whatever
                if (tracking.getCollisionClass() != CollisionClass.NONE && angle < ANGLE_TOLERANCE) {
                    float usageChance = (float) (Math.random() - (getAlliedPresence(tracking, RANGE) / 100));
                    
                    if (usageChance < RANDOM_USE_CHANCE) {
                        shouldUseSystem = true;
                    }
                }
                /*if (tracking.getCollisionClass() != CollisionClass.NONE && angle < ANGLE_TOLERANCE) {
                    float usageChance = (float) (Math.random() - (getAlliedPresence(tracking, RANGE) / 100));
                    
                    if (shield != null) { //if the target has shields we want to try and make sure the shot hits
                        if (fluxer.isOverloadedOrVenting() && Math.random() < RANDOM_USE_CHANCE || shield.isOn() 
                                && !shield.isWithinArc(VectorUtils.getDirectionalVector(tracking.getLocation(), shipLoc)) 
                                && Math.random() < priority || shield.isOff() && fluxer.getCurrFlux() >= fluxer.getMaxFlux() * FLUX_TOLERANCE
                                && Math.random() < (priority / 2)) {
                            shouldUseSystem = true;
                        }
                    } else if (shield != null && shield.getType() == ShieldType.PHASE) { //no point in using it on phase ships or other wierdoes
                        if (fluxer.isOverloadedOrVenting() && usageChance < RANDOM_USE_CHANCE) {
                            shouldUseSystem = true;
                        } //unless we know they'll be able to take damage long enough for it to have some meaning
                    } else { //doesn't have a shield so no need to get fancy
                        if (usageChance < RANDOM_USE_CHANCE) {
                            shouldUseSystem = true;
                        }
                    } 
                }*/
                
                if (ship.getSystem().isActive() ^ shouldUseSystem) {
                    ship.useSystem();
                }
            }
        }
    }
        
    /**
     * Rates the amount of allied ships around a target from an arbitrary floor and cap, weighting larger ships more heavily.
     * @param target
     * @param range - the range around the target to look for allied ships
     * @return the rating
     */
    public float getAlliedPresence(ShipAPI target, float range) {
        List<ShipAPI> allies = AIUtils.getNearbyAllies(ship, range);
        float presence = 0f;

        for (ShipAPI ally : allies) {
            if (!ally.isRetreating()) {
                switch (ally.getHullSize()) {
                    case FIGHTER:
                        presence += FIGHTER_WEIGHT;
                        break;
                    case FRIGATE:
                        presence += FRIGATE_WEIGHT;
                        break;
                    case DESTROYER:
                        presence += DESTROYER_WEIGHT;
                        break;
                    case CRUISER:
                        presence += CRUISER_WEIGHT;
                        break;
                    case CAPITAL_SHIP:
                        presence += CAPITAL_WEIGHT;
                        break;
                }
            }
        }

        if (presence > PRESENCE_CAP)
            return PRESENCE_CAP;
        else if (presence < PRESENCE_FLOOR)
            return PRESENCE_FLOOR;
        else
            return presence;
    }
    
}
