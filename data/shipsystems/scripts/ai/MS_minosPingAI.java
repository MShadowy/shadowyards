package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_minosPingAI implements ShipSystemAIScript {
    //straightfowrward but for the doing
    //AI looks to see if any hostile ships are in sensor range
    //or if your fleets ecm total is lower than the enemies -but-
    //the difference can be made up by using the system
    
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.35f, 0.6f);
    private CombatEngineAPI engine;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }
    
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }
        
        //get this ships location
        tracker.advance(amount);
        Vector2f shipLoc = new Vector2f(ship.getLocation());
        
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            
            List<ShipAPI> friends = AIUtils.getAlliesOnMap(ship);
            List<ShipAPI> enemies = AIUtils.getEnemiesOnMap(ship);
            
            float totalFriends = 0f;
            float allyECM = 0f;
            float enemyECM = 0f;
            
            for (ShipAPI friend : friends) {
                if (!friend.isHulk() || !friend.isFighter()) {
                    totalFriends += 1f;
                    allyECM += friend.getMutableStats().getEccmChance().base;
                }
            }
            
            for (ShipAPI enemy : enemies) {
                if (!enemy.isHulk() || !enemy.isFighter()) {
                    enemyECM += enemy.getMutableStats().getEccmChance().base;
                }
            }
            
            float sensorRange = ship.getMutableStats().getSensorStrength().base;
            List<ShipAPI> detected = AIUtils.getNearbyEnemies(ship, sensorRange);
            
            /*If we can't see any enemies, and the system is offline OR 
                if we're losing the ECM battle but a little boost could give us the edge*/
            if (detected.isEmpty() && !system.isActive() || enemyECM > allyECM && 
                    allyECM + totalFriends > enemyECM && !system.isActive()) {
                ship.useSystem();
            }
        }
    }
}
