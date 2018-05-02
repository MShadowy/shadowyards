package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MS_Utils;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_woopDriveAI implements ShipSystemAIScript {
    /*This system is used purely defensively.  The ship checks to see incoming damage, 
    or judges if the relative weight of firepower in it's area is too much relative to it support,
    and if it thinks it's too much uses the system to NOPE */    
    private static final float SECONDS_TO_LOOK_AHEAD = 3f;
    private static final float RANGE_TO_CHECK = 2500f;
    private static final float EDGE_CHECK = 700f;
    
    private final CollectionUtils.CollectionFilter<DamagingProjectileAPI> filterMisses = new CollectionUtils.CollectionFilter<DamagingProjectileAPI>()
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
    
    private float mapX;
    private float mapY;
    
    private ShipAPI ship;
    private boolean runOnce = false;

    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    
    @Override
    public void advance (float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) {
            return;
        }
        
        if (!runOnce) {
            runOnce = true;
            
            mapX = engine.getMapWidth();
            mapY = engine.getMapHeight();
        }
        
        FluxTrackerAPI fluxer = ship.getFluxTracker();
        tracker.advance(amount);
        Vector2f shipLoc = new Vector2f(ship.getLocation());
        
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            
            float incoming = MS_Utils.estimateIncomingDamage(ship);
            
            boolean shouldUseSystem = false;
            boolean clear = true;
            float hitRad = Math.max(ship.getCollisionRadius(), 100f);
            
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
            
            List<ShipAPI> ships = CombatUtils.getShipsWithinRange(shipLoc, EDGE_CHECK);
            for (ShipAPI s : ships) {
                if (MathUtils.isWithinRange(s.getLocation(), shipLoc, EDGE_CHECK) &&
                        VectorUtils.getAngle(s.getLocation(), shipLoc) > 170) {
                    clear = false;
                }
            }
            if ((ship.getLocation().x + EDGE_CHECK > mapX || ship.getLocation().y + EDGE_CHECK > mapY || 
                    ship.getLocation().x - EDGE_CHECK < mapX || ship.getLocation().y - EDGE_CHECK < mapY)) {
                clear = false;
            }
            
            /* if incoming damage would overload the ship, or if shields are down and the 
            damage is more than 25% of its remaining hitpoints, or if the ship has been ordered to
            retreat and enemy ships are too close, use the system*/
            boolean shield_on = false;
            if (ship.getShield() != null) shield_on = ship.getShield().isOn();
            if (!nearbyThreats.isEmpty() && shield_on && ship.getShield().isOn() && fluxer.getCurrFlux() >= fluxer.getMaxFlux() * 0.8f
                    && incoming >= fluxer.getMaxFlux() * 0.2f || !nearbyThreats.isEmpty() && !shield_on && ship.getShield().isOff() && incoming >=
                            (ship.getHitpoints() * 0.25f ) || ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.RUN_QUICKLY) &&
                                    !AIUtils.getNearbyEnemies(ship, RANGE_TO_CHECK).isEmpty() && clear == true)
            {
                shouldUseSystem = true;
            }

            // If system is inactive and should be active, enable it
            // If system is active and shouldn't be, disable it
            if (ship.getSystem().isActive() ^ shouldUseSystem)
            {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.ship = ship;
    }
}
