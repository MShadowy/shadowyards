package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class MS_elySensors extends BaseHullMod {
    
    private static final float RANGEFINDER = 25f;
    private static final Map mag = new HashMap();
    static {
        mag.put(HullSize.FRIGATE, 360f);
        mag.put(HullSize.DESTROYER, 180f);
        mag.put(HullSize.CRUISER, 90f);
        mag.put(HullSize.CAPITAL_SHIP, 90f);
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSightRadiusMod().modifyPercent(id, RANGEFINDER);
        stats.getSensorStrength().modifyPercent(id, (Float) mag.get(hullSize));
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) return "" + (int) RANGEFINDER;
        if (index == 1) return "" + ((Float) mag.get(hullSize)).intValue();
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
