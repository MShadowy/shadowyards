package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MS_civilianMods extends BaseHullMod {
    
    private static final Map fluxd = new HashMap();
    static {
	fluxd.put(HullSize.FRIGATE, -500f);
	fluxd.put(HullSize.DESTROYER, -1000f);
	fluxd.put(HullSize.CRUISER, -3000f);
        fluxd.put(HullSize.CAPITAL_SHIP, -6000f);
    }
    
    private static final Map fluxdAgain = new HashMap();
    static {
        fluxdAgain.put(HullSize.FRIGATE, -30f);
        fluxdAgain.put(HullSize.DESTROYER, -50f);
        fluxdAgain.put(HullSize.CRUISER, -100f);
        fluxdAgain.put(HullSize.CAPITAL_SHIP, -500f);
    }
    
    private static final Map SCargo = new HashMap();
    static {
        SCargo.put(HullSize.FRIGATE, 5f);
        SCargo.put(HullSize.DESTROYER, 25f);
        SCargo.put(HullSize.CRUISER, 50f);
        SCargo.put(HullSize.CAPITAL_SHIP, 200f);
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFluxCapacity().modifyFlat(id, (Float) fluxd.get(hullSize));
        stats.getFluxDissipation().modifyFlat(id, (Float) fluxdAgain.get(hullSize));
        stats.getCargoMod().modifyFlat(id, (Float) SCargo.get(hullSize));
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) return "" + ((Float) fluxd.get(hullSize)).intValue();
	if (index == 1) return "" + ((Float) fluxdAgain.get(hullSize)).intValue();
        if (index == 2) return "" + ((Float) SCargo.get(hullSize)).intValue();
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
