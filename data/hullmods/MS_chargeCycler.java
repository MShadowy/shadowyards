package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;

public class MS_chargeCycler extends BaseHullMod {
    private float RATE = 1.1f;
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	return null;
    }
    
    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Must be installed on a Shadowyards ship";
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSpec().getHullId().startsWith("ms_"));
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || ship.isHulk()) {
            return;
        }
        
        for (WeaponAPI w : ship.getAllWeapons()) {
            float bCharge = w.getAmmoPerSecond();
            if (bCharge != 0) {
                float nCharge = bCharge + RATE;
            }
        }
    }
}
