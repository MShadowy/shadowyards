package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_telemetryAI implements ShipSystemAIScript {
    
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
        if (engine == null || engine.isPaused()) {
            return;
        }
        
        tracker.advance(amount);
        Vector2f shipLoc = new Vector2f(ship.getLocation());
        Vector2f shipDir = new Vector2f(ship.getVelocity());
        
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            
            float range = 0f;
            float totalWeaps = 0f;
            float totalRange = 0f;
            
            List<WeaponAPI> weapons = ship.getAllWeapons();
            
            for (WeaponAPI weapon : weapons) {
                if (weapon.isDisabled() || weapon.isDecorative() || weapon.getSlot().isHardpoint()) {
                    continue;
                }
                
                totalWeaps += 1;
                totalRange += weapon.getRange();
                
                range = totalRange / totalWeaps;
            }
            
            List<ShipAPI> enemies= AIUtils.getNearbyEnemies(ship, range * 1.25f);
            
            //if enemies are close enough to get broaught in range, use the system
            if (!enemies.isEmpty() && !system.isActive()) {
                ship.useSystem();
            }
        }
    }
}
