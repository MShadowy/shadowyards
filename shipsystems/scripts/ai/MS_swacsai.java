//You can check if a ship is a fighter with ship.isFighter(). If you want to do something to a whole wing, you'd need to use ship.getWingMembers() and apply it to each of them
//Credit goes to Psiyon for his firecontrol AI script.
package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.util.IntervalUtil;

public class MS_swacsai implements ShipSystemAIScript {

    private ShipSystemAPI system;
    private ShipAPI ship;

    //Sets an interval for once every 1-1.5 seconds. (meaning the code will only run once this interval has elapsed, not every frame)
    private final IntervalUtil tracker = new IntervalUtil(1f, 1.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);

        //Once the interval has elapsed...
        if (tracker.intervalElapsed()) {
            //Activ_range is the range at which the AOE benefits are applied. Should match the radius from the other script.
            float activ_range = 5000f;

            int ships_friendly = 0;

            //Sets up a temporary ship object.

            //Iterates through all ships on the map.
            for (ShipAPI shp : Global.getCombatEngine().getShips()) {
                //We don't care about this ship if it's disabled, or if it's not a fighter.
                if (shp.isHulk() && !shp.isFighter()) {
                    continue;
                }

                //If the distance to the ship is less than or equal to the activ_range...
                if (MathUtils.getDistance(shp, shp) <= (activ_range)) {
                    //If the owner of ship_tmp is on the same side and is a fighter, turn the system on
                    if (shp.getOwner() == shp.getOwner()) {
                        ships_friendly++;
                    }
                }

                //If there's a friendly fighter around, we
                if (ships_friendly >= 1 && !system.isActive()) {
                    activateSystem();
                } else if (ships_friendly == 0 && system.isActive()) {
                    deactivateSystem();
                } else {
                    return;
                }
            }
        }
    }

    private void deactivateSystem() {
        if (system.isOn()) {
            ship.useSystem();
        }
    }

    private void activateSystem() {
        if (!system.isOn()) {
            ship.useSystem();
        }
    }
}
