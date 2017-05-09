package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_reisAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.35f, 0.6f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        CombatEntityAPI entity = (ShipAPI) ship;

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (system.isActive() || system.getCooldownRemaining() > 0 || ship.getFluxTracker().isOverloadedOrVenting()) {
                return;
            }

            float sysGo = 0f;

            List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(ship.getLocation(), 800f);
            for (ShipAPI engine : nearbyShips) {
                if (engine.getOwner() == ship.getOwner() || engine.isFighter()) {
                    continue;
                }

                List<ShipEngineAPI> shipEngines = engine.getEngineController().getShipEngines();

                for (ShipEngineAPI shipEngine : shipEngines) {
                    // So we deterimine if we can hit the target (i.e. - is in front of us), and if so, fire the system
                    if (engine.getOwner() != ship.getOwner() && shipEngine.isDisabled() == false 
                            && MathUtils.getShortestRotation(entity.getFacing(), Vector2f.angle(ship.getLocation(), engine.getLocation())) < 10f
                            && MathUtils.getDistance(engine, ship.getLocation()) < 700f) {
                        continue;
                    }

                    sysGo += 2f;
                }
            }

            if (sysGo >= 1f) {
                ship.useSystem();
            }
        }
    }
}
