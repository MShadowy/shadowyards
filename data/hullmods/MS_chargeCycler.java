package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

public class MS_chargeCycler extends BaseHullMod {
    private final float RATE = 1.15f;
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) {
            return "" + (int) ((RATE - 1) * 100);
        }
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
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || !ship.isAlive()) {
            return;
        }
        for (WeaponAPI w : ship.getAllWeapons()) {
            //only bother with ammo regenerators
            
            float reloadRate = w.getSpec().getAmmoPerSecond();
            float nuCharge = reloadRate * RATE;
            if (w.getType() == WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0) {
                w.getAmmoTracker().setAmmoPerSecond(nuCharge);
            }
        }
    }
}
