package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class MS_CCskirmishTactics extends BaseHullMod {
    
    private static final float TURN_SPEED = 10f;
    private static final float TURN_ACCELERATION = 20f;
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().modifyPercent(id, TURN_SPEED);
        stats.getTurnAcceleration().modifyPercent(id, TURN_ACCELERATION);
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) TURN_SPEED;
        if (index == 1) return "" + (int) TURN_ACCELERATION;
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
