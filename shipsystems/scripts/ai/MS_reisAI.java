package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
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

public class MS_reisAI implements ShipSystemAIScript {
    /*ToFix:
    The AI should be a little more accurate with it
    Much more importantly the AI needs to not fire it when it could potentially hit allies
    
    To this end it's probably best to just rewrite much of how the AI works so it handles better*/
    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private boolean runOnce = false;
    private boolean allyBlock = false;
    
    private List<WeaponAPI> weapons;

    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    
    private static final float LOOK_AHEAD_TIME = 0.067f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused() || engine == null) {
            return;
        }
        
        //weapons don't change mid-battle so don't waste a bunch of cycles rechecking all the guns
        if (!runOnce) {
            weapons = ship.getAllWeapons();
            
            runOnce = true;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            boolean shouldUseSystem = false;
            
            for (WeaponAPI w : weapons) {
                //we're not gonna bother with non-reis weapons
                if (!w.getId().equals("ms_stopper1")) {
                    continue;
                }
                
                List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(ship.getLocation(), 800f);
                for (ShipAPI drive : nearbyShips) {
                    //don't bother picking off fighters with this, ignore the dead
                    if (drive.isFighter() || drive.isHulk()) {
                        continue;
                    }
                    
                    Vector2f loc = drive.getLocation();
                    
                    Vector2f ahead = new Vector2f(loc).translate(drive.getVelocity().getX() * LOOK_AHEAD_TIME,
                            drive.getVelocity().getY() * LOOK_AHEAD_TIME);
                    
                    //don't shoot your allies!
                    allyBlock = drive.getOwner() == ship.getOwner()
                            && MathUtils.getShortestRotation(w.getArcFacing(), Vector2f.angle(w.getLocation(), ahead)) < 10f
                            && MathUtils.getDistance(ahead, w.getLocation()) < 600f;

                    List<ShipEngineAPI> shipEngines = drive.getEngineController().getShipEngines();
                    for (ShipEngineAPI shipEngine : shipEngines) {
                        /*So we determine if we can hit the target; if we can, the engines aren't already disabled,
                        and we aren't blocked by a friendly ship fire the system*/
                        if (drive.getOwner() != ship.getOwner() && shipEngine.isDisabled() == false 
                                && MathUtils.getShortestRotation(w.getArcFacing(), Vector2f.angle(w.getLocation(), ahead)) < 10f
                                && MathUtils.getDistance(ahead, w.getLocation()) < 600f && !allyBlock) {
                            shouldUseSystem = true;
                        }
                    }
                }
            }

            if (ship.getSystem().isActive() ^ shouldUseSystem) {
                ship.useSystem();
            }
        }
    }
}
