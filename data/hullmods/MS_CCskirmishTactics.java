package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

public class MS_CCskirmishTactics extends BaseHullMod {
    
    private static final String HULLMOD_ID = "ms_skirmishTactics";
    private static final float TURN_SPEED = 10f;
    private static final float TURN_ACCELERATION = 20f;
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().modifyPercent(id, TURN_SPEED);
        stats.getTurnAcceleration().modifyPercent(id, TURN_ACCELERATION);
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        if (!"shadow_industry".equals(Misc.getCommissionFactionId()))
        {
            ship.getVariant().removeMod(HULLMOD_ID);
        }
        if (ship.getVariant().hasHullMod("CHM_commission"))
        {
            ship.getVariant().removeMod("CHM_commission");
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) TURN_SPEED;
        if (index == 1) return "" + (int) TURN_ACCELERATION;
	return null;
    }
}
