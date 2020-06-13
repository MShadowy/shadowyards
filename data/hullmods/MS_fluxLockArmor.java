package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class MS_fluxLockArmor extends BaseHullMod {
    //applies a before armor damage reduction based on the ships total flux when it starts venting; follows a very similar curve to the HSC
    private static final Map<HullSize, Float> MAG = new HashMap<>();
    private final Map<ShipAPI, Float> MOD = new HashMap<>();
    public static String FL_ICON = "graphics/shi/icons/tactical/ms_hsConduitIcon.png";
    public static String FL_BUFFID = "ms_fluxLock";
    public static String FL_NAME = "Flux Lock Plating";
    //private ShipAPI ship;
    
    static {
        MAG.put(HullSize.FIGHTER, 0.7f);
        MAG.put(HullSize.FRIGATE, 0.7f);
        MAG.put(HullSize.DESTROYER, 0.6f);
        MAG.put(HullSize.CRUISER, 0.5f);
        MAG.put(HullSize.CAPITAL_SHIP, 0.4f);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
	if (index == 0) {
            return "" + (int) (MAG.get(HullSize.FRIGATE) * 100f);
        }
        if (index == 1) {
            return "" + (int) (MAG.get(HullSize.DESTROYER) * 100f);
        }
        if (index == 2) {
            return "" + (int) (MAG.get(HullSize.CRUISER) * 100f);
        }
        if (index == 3) {
            return "" + (int) (MAG.get(HullSize.CAPITAL_SHIP) * 100f);
        }
        return null;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Must be installed on a Shadowyards ship";
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSpec().getHullId().startsWith("ms_") || ship.getHullSpec().getHullId().startsWith("msp_"));
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || ship.isHulk()) {
            return;
        }
        
        FluxTrackerAPI fluxTracker = ship.getFluxTracker();

        if (ship.getFluxTracker().isVenting()) {
            float fluxAtVent = ship.getFluxTracker().getCurrFlux();
            float fluxLockBonus = 100f;
            float displayBonus = 100f;
            
            if (MOD.containsKey(ship)) {
                fluxLockBonus = 1f - (MAG.get(ship.getHullSize()) * (getFluxCurve(MOD.get(ship) / fluxTracker.getMaxFlux(), 1.4f)));
                displayBonus = 100f * (MAG.get(ship.getHullSize()) * (getFluxCurve(MOD.get(ship) / fluxTracker.getMaxFlux(), 1.4f)));
            } else {
                MOD.put(ship, fluxAtVent);
            }
            
            ship.getMutableStats().getHullDamageTakenMult().modifyMult("fluxLock", fluxLockBonus);
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("fluxLock", fluxLockBonus);
            
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(FL_BUFFID, FL_ICON, FL_NAME, "Incoming damage reduced by "+(int) displayBonus+"%", true);
            }
        } else {
            MOD.remove(ship);
        }
    }
    
    private static float getFluxCurve(float ratio, float curveStrength) {
        //this method returns a value that is adjusted to the amount of flux.
        //the ratio is the hardFlux / totalFlux , and the curve strength determines how quickly the curve rises
        float A = curveStrength * ratio + 1;
        float result = -1 / (A * A) + 1;
        return result;
    }
}
