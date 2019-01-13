package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MS_elySensors extends BaseHullMod {
    
    private static final float RANGEFINDER = 25f;
    private static final float SENSOR_BOOST = 50F;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSightRadiusMod().modifyPercent(id, RANGEFINDER);
        stats.getSensorStrength().modifyPercent(id, SENSOR_BOOST);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) return "" + (int) RANGEFINDER;
        if (index == 1) return "" + (int) SENSOR_BOOST;
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
