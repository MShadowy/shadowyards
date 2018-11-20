package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;

public class MS_radiationAbsorbers extends BaseIndustry {
        @Override
	public boolean isHidden() {
		return !market.getFactionId().equals("shadow_industry");
	}
	
	@Override
	public boolean isFunctional() {
		return super.isFunctional() && market.getFactionId().equals("shadow_industry");
	}
        
        @Override
	public boolean isAvailableToBuild() {
		return false;
	}
	
        @Override
	public boolean showWhenUnavailable() {
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
}
