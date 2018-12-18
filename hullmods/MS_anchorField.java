package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MS_anchorField extends BaseHullMod {
        public static final float VISIBILITY = 0.8f;
        public static final float REDUCTION = 0.2f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorProfile().modifyMult(id, VISIBILITY);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "" + (int) ((1f - REDUCTION) * 100f) + "%";
		return null;
	}
}
