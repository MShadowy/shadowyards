package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollectionUtils.CollectionFilter;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.util.MS_Utils;
import java.util.Collections;
import java.util.ListIterator;


public class MS_mimirSlideDriveAI implements ShipSystemAIScript {
    
    private static final float SECONDS_FOR_PATH = 1.5f;
    private static final float SECONDS_TO_LOOK_AHEAD = 3f;
    private float bashNum = 0;
    
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
    //so, unlike the schism drive, this system is about avoiding damage, so purposefully colliding with ships is bad
    //the standard ai should avoid if at all possible collisions with friendly ships
    //and avoid colliding with enemy ships unless the ship thinks it would take more damage otherwise
    //using the system to boost around the battlefield is acceptable
    private final CollectionFilter<DamagingProjectileAPI> filterMisses = new CollectionFilter<DamagingProjectileAPI>()
    {
        @Override
        public boolean accept(DamagingProjectileAPI proj) {
            // Exclude missiles and our own side's shots
            if (proj.getOwner() == ship.getOwner() && (!(proj instanceof MissileAPI ) || !((MissileAPI) proj).isFizzling()))
            {
                return false;
            }
            
            if (proj instanceof MissileAPI) {
                MissileAPI missile = (MissileAPI) proj;
                if (missile.isFlare()) {
                    return false;
                }
            }
            // Only include shots that are on a collision path with us
            // Also ensure they aren't travelling AWAY from us ;)
            return (CollisionUtils.getCollides(proj.getLocation(), Vector2f.add(proj.getLocation(), (Vector2f) new Vector2f(proj.getVelocity()).scale(
                        SECONDS_TO_LOOK_AHEAD), null), ship.getLocation(), ship.getCollisionRadius())
                    && Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation()))) <= 90f);
        }
    };
    
    //Set up hull size mulitplies for caring about particular collisions
    private static final Map<ShipAPI.HullSize, Float> mag = new HashMap<>();
    static {
        mag.put(HullSize.FIGHTER, 0f); //the AI doesn't care about colliding with fighters at all (since it can't)
        mag.put(HullSize.FRIGATE, 0.4f);
        mag.put(HullSize.DESTROYER, 0.6f);
        mag.put(HullSize.CRUISER, 0.8f);
        mag.put(HullSize.CAPITAL_SHIP, 1f); //but cares about colliding with capital ships a lot
    }
    
    private String name;
    
    protected String getPersonality(FleetMemberAPI member) {
                
		PersonAPI captain = member.getCaptain();
		if (captain != null)
		{
                    switch (captain.getPersonalityAPI().getId()) {
                        case "timid":
                            name = "timid";
                            break;
                        case "cautious":
                            name = "cautious";
                            break;
                        case "steady":
                            name = "steady";
                            break;
                        case "aggressive":
                            name = "aggressive";
                            break;
                    }
		}
                
		return name;
    }

    
    private CombatEngineAPI engine;
    
    //private ShipwideAIFlags flags;
    private ShipAPI ship;
    //private PersonalityAPI behavior;
    private PersonAPI officer;
    private final boolean mission = false;

    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    
    @Override
    public void advance (float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }
        
        //FleetMemberAPI member;
        
        //officer = member.getCaptain();
        
        tracker.advance(amount);
        Vector2f shipLoc = new Vector2f(ship.getLocation());
        Vector2f shipDir = new Vector2f(ship.getVelocity());
        
        //reckless: Fly me closer, I want to hit them with my sword!
        //aggressive: wants to close, tries to get into a position to attack, cares a lot less about collisions, liberal usage
        //steady: default, refined, more self-preservationy version of past crash happy AI
        //cautious and timid personalities focus on avoiding damage
        //cautious: uses it to get around, but won't use it in most combat situations unless they're about to take a big hit
        //timid: used only in response to possible damage or if the route would take them away from combat
        //twitchy damage estimations, timid actually gives less fucks about collisions than any one else
        
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            
            float incoming = MS_Utils.estimateIncomingDamage(ship);
            
            boolean shouldUseSystem = false;
            float hitRad = Math.max(ship.getCollisionRadius(), 50f);
            
            List<DamagingProjectileAPI> nearbyThreats = CombatUtils.getProjectilesWithinRange(shipLoc, hitRad);
            for (DamagingProjectileAPI tmp : engine.getProjectiles()) {
                if (MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), ship.getCollisionRadius() + hitRad)) {
                    nearbyThreats.add(tmp);
                }
            }
            nearbyThreats = CollectionUtils.filter(nearbyThreats, filterMisses);
            List<MissileAPI> nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + hitRad);
            for (MissileAPI missile : nearbyMissiles) {
                if (!missile.getEngineController().isTurningLeft() && !missile.getEngineController().isTurningRight()) {
                    continue;
                }

                nearbyThreats.add(missile);
            }
            
            float crashRad = Math.max(ship.getCollisionRadius(), 1750f);
            
            List<ShipAPI> willCrash = CombatUtils.getShipsWithinRange(shipLoc, crashRad);
            if (!willCrash.isEmpty()) {
                if (shipDir.lengthSquared() <= 0.01f) {
                    shipDir = VectorUtils.getDirectionalVector(shipLoc, new Vector2f(ship.getVelocity()));
                    Vector2f.sub(shipDir, ship.getLocation(), shipDir);
                    if (shipDir.lengthSquared() <= 0.01f) {
                        shipDir = new Vector2f(1f, 0f);
                    }
                }
                shipDir.normalise();
                shipDir.scale(crashRad);
                Vector2f.add(shipDir, ship.getLocation(), shipDir);
                
                Collections.sort(willCrash, new CollectionUtils.SortEntitiesByDistance(ship.getLocation()));
                ListIterator<ShipAPI> iter = willCrash.listIterator();
                while (iter.hasNext()) {
                    ShipAPI tmp = iter.next();
                    if (tmp != ship && ship.getCollisionClass() != CollisionClass.NONE && !tmp.isFighter() && !tmp.isDrone()) {
                        Vector2f bash = intercept(ship.getLocation(), 1750f, tmp.getLocation(), tmp.getVelocity());
                        
                        if (bash == null) {
                            Vector2f projection = new Vector2f(tmp.getVelocity());
                            float scalar = MathUtils.getDistance(tmp.getLocation(), ship.getLocation()) / 1500f;
                            projection.scale(scalar);
                            Vector2f.add(tmp.getLocation(), projection, bash);
                        }
                        
                        if (bash !=null && ship.getLocation() != null) {
                            float areaChange = 1f;
                            float aMass = ship.getMass();
                            float bMass = tmp.getMass();
                            
                            if (CollisionUtils.getCollides(ship.getLocation(), shipDir, bash, tmp.getCollisionRadius() * 0.5f + ship.getCollisionRadius() *
                                                           0.75f * areaChange)) {
                                //steady officers would rather not hit things but care more about allies then enemies
                                //if ("steady".equals(name)) {
                                    if (tmp.getOwner() == ship.getOwner()) {
                                    //bashNum = mag.get(tmp.getHullSize()) * (bMass + aMass * 2f);
                                        bashNum = (bMass + aMass * 2f);
                                    } else {
                                        bashNum = (bMass + aMass);
                                    }
                                /*} //aggressive officers give less fucks about collisions
                                else if ("aggressive".equals(name)) {
                                    if (tmp.getOwner() == ship.getOwner()) {
                                    //bashNum = mag.get(tmp.getHullSize()) * (bMass + aMass * 2f);
                                        bashNum = (bMass + aMass * 1.5f);
                                    } else {
                                        bashNum = (bMass + aMass * 0.2f);
                                    }
                                } //timid and cautious officers don't want to collide at all
                                else {
                                    bashNum = (bMass + aMass * 2f);
                                }*/
                            }
                        }
                    }
                }
            }
            
            CombatFleetManagerAPI.AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(mission).getAssignmentFor(ship);
            Vector2f targetSpot;
            if (assignment != null && assignment.getTarget() != null) {
                targetSpot = assignment.getTarget().getLocation();
            } else {
                targetSpot = null;
            }
            
            //aggressive:
            //similar to stead, below; used to avoid danger if no collision risk or if damage from collision lower than danger
            //but much more liberal in using it to get around and will try and use it to aggressively close with targets
            /*if ("aggressive".equals(name)) {
                if (!nearbyThreats.isEmpty() && bashNum == 0 || !nearbyThreats.isEmpty() &&
                        (bashNum * MathUtils.getRandomNumberInRange(0.8f, 1.2f)) < incoming || nearbyThreats.isEmpty()
                                && bashNum == 0 && targetSpot == null && Math.random() > 0.25f || targetSpot != null 
                                        && bashNum <= 75 && Math.random() > 0.4f) {
                    shouldUseSystem = true;
                }
            }*/
            //steady:
            //if there's danger and there's no chance of collision, or if the danger would do more damage than a collision,
            //or if the ship is far away from combat and wants to get around quickly, use the system
            //else if ("steady".equals(name)) {
                if (!nearbyThreats.isEmpty() && bashNum == 0 || !nearbyThreats.isEmpty() && 
                        (bashNum * MathUtils.getRandomNumberInRange(0.8f, 1.2f)) < incoming || nearbyThreats.isEmpty() 
                                && bashNum == 0 && targetSpot == null && Math.random() > 0.5f ) {
                    shouldUseSystem = true;
                }
            //}
            //cautious:
            //system is used mostly reactively in response to potential damage 
            //will do so only if the incoming fire equals or exceeds the damage that would otherwise be taken in a collision
            /*else if ("cautious".equals(name)) {
                if (!nearbyThreats.isEmpty() && bashNum == 0 || !nearbyThreats.isEmpty() && 
                        (bashNum * MathUtils.getRandomNumberInRange(0.5f, 1f)) < incoming || nearbyThreats.isEmpty() 
                                && bashNum == 0 && targetSpot == null && Math.random() > 0.8f ) {
                    shouldUseSystem = true;
                }
            }
            //timid:
            //run away! run away!
            //only uses the system to evade damage, but is very twitchy with it
            else {
               if (!nearbyThreats.isEmpty() && bashNum == 0 || !nearbyThreats.isEmpty() && 
                        (bashNum * MathUtils.getRandomNumberInRange(0.1f, 0.7f)) < incoming ) {
                    shouldUseSystem = true;
                } 
            }*/
            //ideally this would also include a state for where the ship, if threatened with both a mortal blow and an unsurvivable crash,
            //would choose sudoku, but eh
            
            if (ship.getSystem().isActive() ^ shouldUseSystem) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.engine = engine;
        this.ship = ship;
    }
}
