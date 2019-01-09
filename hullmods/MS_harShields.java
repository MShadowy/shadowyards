package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class MS_harShields extends BaseHullMod {
    
    private static final Map<HullSize, Float> MAG = new HashMap<>();
    public static String HS_ICON = "graphics/shi/icons/tactical/ms_hsConduitIcon.png";
    public static String HS_BUFFID = "ms_harShields";
    public static String HS_NAME = "Harmonic Shield Conduits";
    
    static {
        MAG.put(HullSize.FIGHTER, 0.8f);
        MAG.put(HullSize.FRIGATE, 0.8f);
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
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || ship.isHulk()) {
            return;
        }
        
        FluxTrackerAPI fluxTracker = ship.getFluxTracker();
        float harShieldsBonus = 100f * (MAG.get(ship.getHullSize()) * (getFluxCurve(fluxTracker.getHardFlux() / fluxTracker.getMaxFlux(), 1.4f)));

        ship.getMutableStats().getFluxDissipation().modifyPercent("harShields", 100f * (MAG.get(ship.getHullSize()) * (getFluxCurve(fluxTracker.getHardFlux() / fluxTracker.getMaxFlux(), 1.4f))));
        
        if (ship == Global.getCombatEngine().getPlayerShip() && fluxTracker.getHardFlux() > 0) {
            Global.getCombatEngine().maintainStatusForPlayerShip(HS_BUFFID, HS_ICON, HS_NAME, "Flux dissipation increased by"+(int) harShieldsBonus+"%", true);
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
