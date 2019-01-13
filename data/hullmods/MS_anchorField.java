package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashSet;
import java.util.Set;

public class MS_anchorField extends BaseHullMod {
    public static final float VISIBILITY = 20f;
        
    private float check=0;
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static {
        // These hullmods will automatically be removed
        BLOCKED_HULLMODS.add("converted_hangar");
    }
    private String ERROR="Incompatible Hullmod Warning";
	
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	stats.getSensorProfile().modifyMult(id, 1 - VISIBILITY * 0.01f);
    }
        
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){        
        if (check>0) {     
            check-=1;
            if (check<1){
            ship.getVariant().removeMod(ERROR);   
            }
        }
        
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {                
                ship.getVariant().removeMod(tmp);      
                ship.getVariant().addMod(ERROR);
                check=3;
            }
        }
    }
	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) VISIBILITY + "%";
            return null;
    }
}
