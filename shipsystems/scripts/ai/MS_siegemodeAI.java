package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_siegemodeAI implements ShipSystemAIScript {

    private ShipSystemAPI system;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private float siegeModeTrigger;
    private float siegeModeTriggerMin;
    private float siegeModeTriggerMax;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
        // technically it'd make sense to have these vary with "do I have extended shields?" as well, but careful testing indicates that I think this is good enough.
        if (ship.getShield().getType() == ShieldAPI.ShieldType.FRONT) {
            siegeModeTrigger = 265f;
            siegeModeTriggerMin = 120f;
            siegeModeTriggerMax = 240f;
        } else if (ship.getShield().getType() == ShieldAPI.ShieldType.OMNI) {
            siegeModeTrigger = 60f;
            siegeModeTriggerMin = 30f;
            siegeModeTriggerMax = 330f;
        } else { // no shield or phase cloak - this should not happen.
            siegeModeTrigger = 999f;
            siegeModeTriggerMin = 999f;
            siegeModeTriggerMax = 0f;
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        boolean shieldExtensionNeeded = false;
        if (ship.getShield().getActiveArc() > siegeModeTrigger) { // if shields haven't unfolded enough, then siege mode can't help.
            float facing = ship.getShield().getFacing(); // note that this is absolute facing, and thus works just fine with both front & omni shields
            if (missileDangerDir != null) {
                for (CombatEntityAPI entity : AIUtils.getNearbyEnemyMissiles(ship, 800f)) {
                    float relativeFacing = MathUtils.clampAngle(VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), entity.getLocation())) - facing);
                    if (relativeFacing < siegeModeTriggerMax && relativeFacing > siegeModeTriggerMin) {
                        shieldExtensionNeeded = true;
                        break;
                    }
                }
            }
            if (!shieldExtensionNeeded) {
                for (ShipAPI shp : AIUtils.getNearbyEnemies(ship, 1600f)) {
                    if ((shp.getFluxTracker().isOverloaded() && shp.getFluxTracker().getOverloadTimeRemaining() > 1.8f) || (shp.getFluxTracker().isVenting() && shp.getFluxTracker().getTimeToVent() > 1.8f)) {
                        continue; // skip this ship, it's not a threat.
                    }
                    float relativeFacing = MathUtils.clampAngle(VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), shp.getLocation())) - facing);
                    if (relativeFacing < siegeModeTriggerMax && relativeFacing > siegeModeTriggerMin) {
                        shieldExtensionNeeded = true;
                        break;
                    }
                }
            }
        }
        if (shieldExtensionNeeded || flags.hasFlag(ShipwideAIFlags.AIFlags.TURN_QUICKLY)) {
            activateSystem();
        } else {
            deactivateSystem();
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
