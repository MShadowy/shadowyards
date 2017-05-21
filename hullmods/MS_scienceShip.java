package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MS_scienceShip extends BaseHullMod {
    private final static float SENSOR_BONUS = 60f;
    private final static float VISIBILITY = 100f;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorStrength().modifyMult(id, 1f + SENSOR_BONUS * 0.01f);
        stats.getSensorProfile().modifyPercent(id, VISIBILITY);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) SENSOR_BONUS;
        if (index == 1) return "" + (int) VISIBILITY;
	return null;
    }
}
