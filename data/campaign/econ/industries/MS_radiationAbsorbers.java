package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;

public class MS_radiationAbsorbers extends BaseIndustry {	
	@Override
	public boolean isFunctional() {
            return super.isFunctional() && market.getFactionId().equals("shadow_industry");
	}
        
        @Override
        public boolean isHidden() {
            return false;
        }
        
        @Override
	public boolean isAvailableToBuild() {
            return false;
	}
    
        @Override
        public void apply() {
            super.apply(true);
            int size = market.getSize();
            
            demand(Commodities.MARINES, size -3);
            demand(Commodities.CREW, size -2);
            demand(Commodities.RARE_METALS, size -2);
            demand(Commodities.VOLATILES, size -2);
            demand(Commodities.HEAVY_MACHINERY, size -1);
            
            supply(Commodities.MARINES, size -4);
            supply(Commodities.CREW, size -3);
            
            if (market.hasCondition(Conditions.IRRADIATED)) market.getHazard().modifyFlat(id, -0.25f, "Radiation Absorbers");
            
            //as they're critical to Theramins continued existence, whoever owns the planet will do a lot to keep them up and running
            Pair<String, Integer> deficit = getMaxDeficit(Commodities.MARINES, Commodities.CREW, Commodities.RARE_METALS, 
                    Commodities.VOLATILES, Commodities.HEAVY_MACHINERY);
            int maxDeficit = size -3;
            if (deficit.two > maxDeficit) deficit.two = maxDeficit;
            
            if (!isFunctional()) {
                //for now this will just clear supply
                supply.clear();
            }
        }
        
        @Override
        public void unapply() {
            super.unapply();
            //we'll call the colony deletion here when we figure that out
        }
        
        @Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
            //if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
            if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {	
                float REDUCTION = 0.25f;
		String totalStr = "-" + (int)Math.round(REDUCTION * 100f) + "%";
		Color h = Misc.getHighlightColor();
		if (REDUCTION < 0) {
                    h = Misc.getNegativeHighlightColor();
                    totalStr = "" + (int)Math.round(REDUCTION * 100f) + "%";
		}
		float opad = 10f;
		if (REDUCTION >= 0) {
                    tooltip.addPara("Reduces radiation environmental hazard: %s", opad, h, totalStr);
		}	
            }
	}
        
        @Override
        public float getPatherInterest() {
            return 4f + super.getPatherInterest();
        }
}
