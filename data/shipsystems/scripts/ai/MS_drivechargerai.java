package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_drivechargerai implements ShipSystemAIScript {

    private static final float SECONDS_TO_LOOK_AHEAD = 3f;
    private float THRESHHOLD;
    
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    
    private final CollectionUtils.CollectionFilter<DamagingProjectileAPI> filterMisses = new CollectionUtils.CollectionFilter<DamagingProjectileAPI>()
    {
        @Override
        public boolean accept(DamagingProjectileAPI proj) {// Exclude missiles and our own side's shots
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
    
    private static float damageReader(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0f;
        float shipFace = ship.getFacing();
    
        accumulator += beamReader(ship, damageWindowSeconds);
    
        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
    
            if(proj.getOwner() == ship.getOwner()) continue; // Ignore friendly projectiles
    
            Vector2f endPoint = new Vector2f(proj.getVelocity());
            endPoint.scale(damageWindowSeconds);
            Vector2f.add(endPoint, proj.getLocation(), endPoint);
    
            if((ship.getShield() != null && ship.getShield().isWithinArc(proj.getLocation()))
            || !CollisionUtils.getCollides(proj.getLocation(), endPoint,
                    new Vector2f(ship.getLocation()), ship.getCollisionRadius()))
                continue;
    
            if (shipFace - 45f <= Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation()))) ||
                    shipFace + 45f >= Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation())))) {
                accumulator += proj.getDamageAmount() + proj.getEmpAmount();
            }
        }
    
        return accumulator;
    }
    private static float beamReader(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0f;
        float shipFace = ship.getFacing();
    
        for (BeamAPI beam : Global.getCombatEngine().getBeams()) {
            if(beam.getDamageTarget() != ship) continue;
            float beamX = beam.getFrom().x;
            float beamY = beam.getFrom().y;
            float beamLoc = beamX + beamY;
    
            if (shipFace - 45f <= Math.abs(MathUtils.getShortestRotation(beamLoc, VectorUtils.getAngle(beam.getFrom(), ship.getLocation()))) ||
                    shipFace + 45f >= Math.abs(MathUtils.getShortestRotation(beamLoc, VectorUtils.getAngle(beam.getFrom(), ship.getLocation())))) {
                float dps = beam.getWeapon().getDerivedStats().getDamageOver30Sec() / 30;
                float emp = beam.getWeapon().getDerivedStats().getEmpPerSecond();
    
                accumulator += (dps + emp) * damageWindowSeconds;
            }
        }
    
        return accumulator;
    }
    
    //This just initializes the script.
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    //So here we will tell the ship how to make use of the system.
    /*Ship gets dodgy; check incoming damage or if something (like, say, and Onslaught) is coming at it at speed, 
    activate the system to get out of the way*/
    @Override
    public void advance(float amount, Vector2f position, Vector2f collisionDanger, ShipAPI target) {
        if (engine.isPaused()) {
            return;
        }
        
        tracker.advance(amount);
        Vector2f shipLoc = new Vector2f(ship.getLocation());

        //Once the interval has elapsed...
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            
            float incoming = damageReader(ship, SECONDS_TO_LOOK_AHEAD);
            //float incoming = MS_Utils.estimateIncomingDamage(ship);
            THRESHHOLD = ship.getHitpoints() * 0.1f;
            
            float hitRad = Math.max(ship.getCollisionRadius(), 1250f);
            
            List<DamagingProjectileAPI> nearbyThreats = CombatUtils.getProjectilesWithinRange(shipLoc, hitRad);
            for (DamagingProjectileAPI tmp : engine.getProjectiles()) {
                if (MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), ship.getCollisionRadius() + hitRad) && tmp.getDamage().getDamage() > 400f) {
                    nearbyThreats.add(tmp);
                }
            }
            nearbyThreats = CollectionUtils.filter(nearbyThreats, filterMisses);
            List<MissileAPI> nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + hitRad);
            for (MissileAPI missile : nearbyMissiles) {
                if (!missile.getEngineController().isTurningLeft() && !missile.getEngineController().isTurningRight() && missile.getDamage().getDamage() > 400f) {
                    continue;
                }

                nearbyThreats.add(missile);
            }
            
            /*if enough damage is incoming and we're dodging/maneuvering/retreating or if we're not behind the target, use system*/
            if ((ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE) || ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) || ship.isRetreating()) && 
                    !nearbyThreats.isEmpty() && (THRESHHOLD * MathUtils.getRandomNumberInRange(0.8f, 1.2f)) < incoming
                    /*|| (target.getFacing() < ship.getFacing() + 30f || target.getFacing() > ship.getFacing() + 30f) &&
                    (ship.getEngineController().isStrafingLeft() || ship.getEngineController().isStrafingRight()) && MathUtils.getRandomNumberInRange(0f, 1f) >= 0.75f*/)
            {
                ship.useSystem();
            }
        }
    }
}
