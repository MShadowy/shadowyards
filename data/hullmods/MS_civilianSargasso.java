package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MS_civilianSargasso extends BaseHullMod {
    
    private static final float UNCAPACITANCE = -500f;
    private static final float RETENTION = -45f;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFluxCapacity().modifyFlat(id, UNCAPACITANCE);
        stats.getFluxDissipation().modifyFlat(id, RETENTION);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + ((Float) UNCAPACITANCE).intValue();
        if (index == 1) return "" + ((Float) RETENTION).intValue();
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
