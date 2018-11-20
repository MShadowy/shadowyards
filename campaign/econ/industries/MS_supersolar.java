package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_commodities;

public class MS_supersolar extends BaseIndustry {

    @Override
    public void apply() {
	super.apply(true);
               
        int size = market.getSize();
        
        demand(Commodities.CREW, size-1);
        demand(Commodities.HEAVY_MACHINERY, size -1);
        
        supply(MS_commodities.BATTERIES, size);
        
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.CREW, Commodities.HEAVY_MACHINERY);
        
        applyDeficitToProduction(1, deficit, MS_commodities.BATTERIES);
                
        if (!isFunctional()) {
            supply.clear();
        }
    }

    @Override
    public void unapply() {
	super.unapply();
    }
    
    @Override
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
	return true;
    }

    @Override
    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
	return true;
    }
}
