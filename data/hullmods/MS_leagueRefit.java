package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MS_leagueRefit extends BaseHullMod {
    
    private static final Map fluxd = new HashMap();
    static {
	fluxd.put(HullSize.FRIGATE, -100f);
	fluxd.put(HullSize.DESTROYER, -250f);
	fluxd.put(HullSize.CRUISER, -500f);
        fluxd.put(HullSize.CAPITAL_SHIP, -1000f);
    }
    
    private static final Map fluxdAgain = new HashMap();
    static {
        fluxdAgain.put(HullSize.FRIGATE, -10f);
        fluxdAgain.put(HullSize.DESTROYER, -25f);
        fluxdAgain.put(HullSize.CRUISER, -50f);
        fluxdAgain.put(HullSize.CAPITAL_SHIP, -100f);
    }
    
    private static final Map slowed = new HashMap();
    static {
        slowed.put(HullSize.FRIGATE, -10f);
        slowed.put(HullSize.DESTROYER, -10f);
        slowed.put(HullSize.CRUISER, -15f);
        slowed.put(HullSize.CAPITAL_SHIP, -15f);
    }
    
    private static final Map Armr = new HashMap();
    static {
        Armr.put(HullSize.FRIGATE, 50f);
        Armr.put(HullSize.DESTROYER, 200f);
        Armr.put(HullSize.CRUISER, 500f);
        Armr.put(HullSize.CAPITAL_SHIP, 1000f);
    }
    
    private static final Map HPUp = new HashMap();
    static {
        HPUp.put(HullSize.FRIGATE, 25f);
        HPUp.put(HullSize.DESTROYER, 100f);
        HPUp.put(HullSize.CRUISER, 250f);
        HPUp.put(HullSize.CAPITAL_SHIP, 500f);
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFluxCapacity().modifyFlat(id, (Float) fluxd.get(hullSize));
        stats.getFluxDissipation().modifyFlat(id, (Float) fluxdAgain.get(hullSize));
        stats.getMaxSpeed().modifyFlat(id, (Float) slowed.get(hullSize));
        stats.getArmorBonus().modifyFlat(id, (Float) Armr.get(hullSize));
        stats.getHullBonus().modifyFlat(id, (Float) HPUp.get(hullSize), id);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) return "" + ((Float) fluxd.get(hullSize)).intValue();
	if (index == 1) return "" + ((Float) fluxdAgain.get(hullSize)).intValue();
	if (index == 2) return "" + ((Float) slowed.get(hullSize)).intValue();
        if (index == 3) return "" + ((Float) Armr.get(hullSize)).intValue();
        if (index == 4) return "" + ((Float) HPUp.get(hullSize)).intValue();
	return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}
