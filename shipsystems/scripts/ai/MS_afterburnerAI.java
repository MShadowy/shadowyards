//Credit goes to Psiyon for his firecontrol AI script.
package data.shipsystems.scripts.ai;

import java.util.List;

import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollectionUtils.CollectionFilter;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.CombatUtils;

public class MS_afterburnerAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private IntervalUtil tracker = new IntervalUtil(0.1f, 0.5f);
    private float range = 800f;
    private final CollectionFilter filterGoals = new CollectionFilter() {
        @Override
        public boolean accept(Object t) {
            CombatEntityAPI entity = (CombatEntityAPI) t;

            // Exclude shots and missiles
            if (entity instanceof DamagingProjectileAPI || entity instanceof MissileAPI) {
                return false;
            }

            //Only count ships that are being targeted by this craft, waypoints and objectives
            return (entity == ship.getShipTarget() || entity instanceof BattleObjectiveAPI);
        }
    };

    //This just initializes the script.
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }

    //So here we will tell the ship how to make use of the system.
    @Override
    public void advance(float amount, Vector2f position, Vector2f collisionDanger, ShipAPI target) {
        tracker.advance(amount);
        Vector2f shipLoc = ship.getLocation();

        if (tracker.intervalElapsed()) {
            //Can we use this system this frame?
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            boolean shouldUseSystem = true;

            List<CombatEntityAPI> goalThings = CombatUtils.getEntitiesWithinRange(shipLoc, range);
            goalThings = CollectionUtils.filter(goalThings, filterGoals);
            goalThings.addAll(AIUtils.getEnemiesOnMap(ship));

            if (!goalThings.isEmpty()) {
                shouldUseSystem = false;
            }

            // If system is inactive and should be active, enable it
            // If system is active and shouldn't be, disable it
            if (shouldUseSystem) {
                ship.useSystem();
            }
        }
    }
}
