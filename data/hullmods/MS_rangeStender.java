package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class MS_rangeStender extends BaseHullMod {
    
    private final float EXTEND = 150f;
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, EXTEND);
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) EXTEND;
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
