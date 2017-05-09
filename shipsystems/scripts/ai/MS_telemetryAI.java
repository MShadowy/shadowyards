package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_telemetryAI implements ShipSystemAIScript {
    
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;
    
    private float nominalRange=0;
    private float activeRange=0;

    private final IntervalUtil tracker = new IntervalUtil(0.35f, 0.6f);
    
    private boolean runOnce = false;
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }
    
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }
        
        if (!runOnce){
            runOnce=true;
            List<WeaponAPI> weapons = ship.getAllWeapons();
            int i=0;
            for (WeaponAPI w : weapons) {
                if ((w.getType()==WeaponAPI.WeaponType.ENERGY || w.getType()==WeaponAPI.WeaponType.BALLISTIC || w.getType()!=WeaponAPI.WeaponType.MISSILE) && w.getRange()>200 && !w.hasAIHint(WeaponAPI.AIHints.PD)) {
                    nominalRange = nominalRange + w.getRange();
                    i++;
                } 
            }
            nominalRange = nominalRange/i;
            activeRange = nominalRange * 1.33f;
        }
        
        tracker.advance(amount);
        Vector2f shipLoc = new Vector2f(ship.getLocation());
        
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            
            if (target == null) return;
            
            boolean shouldUseSystem = false;
            
            List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(shipLoc, activeRange);
            for (ShipAPI tracking : nearbyShips) {
                float distance, closestDistance = Float.MAX_VALUE;
                for (ShipAPI tmp : AIUtils.getNearbyEnemies(ship, activeRange)) {
                    //sort closest enemy
                    distance = MathUtils.getDistance(tmp, ship.getLocation());  
                    if (distance < closestDistance)
                    {
                        closestDistance = distance;
                    }
                }
                //fighters will zip in and out of range too fast for it to be meaningful, so ignore them
                //and also dead guys and people on your side
                //there's no point in using it if yoiur target under your nominal range already, so don't
                if (tracking.getOwner() == ship.getOwner() || tracking.isHulk() || tracking.isFighter()
                        || !nearbyShips.isEmpty() && closestDistance < nominalRange || nearbyShips.isEmpty()) {
                    continue;
                } 
                //use the system if the enemy is close enough that the added range can hit them but not so close you waste it
                else if (!nearbyShips.isEmpty() && closestDistance > nominalRange && closestDistance < activeRange) {
                    shouldUseSystem = true;
                }
            }
            
            if (ship.getSystem().isActive() ^ shouldUseSystem) {
                ship.useSystem();
            }
        }
    }
}
