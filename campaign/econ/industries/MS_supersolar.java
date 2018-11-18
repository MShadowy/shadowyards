package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import static data.campaign.econ.MS_commodities.BATTERIES;

public class MS_supersolar extends BaseIndustry {

        @Override
        public void apply() {
		super.apply(true);
                int size = market.getSize();
                
                demand(Commodities.CREW, size-1);
                supply(BATTERIES, size);
                
                Pair<String, Integer> deficit = getMaxDeficit(Commodities.CREW);
                applyDeficitToProduction(0, deficit, BATTERIES);
                
                if (!isFunctional()) {
                    supply.clear();
                }
	}

        @Override
	public void unapply() {
		super.unapply();
	}
        
        @Override
	public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltip(mode, tooltip, expanded);
        }
}
