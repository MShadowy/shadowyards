package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_commodities;

public class MS_medCenter extends BaseIndustry {

    public void apply() {
        super.apply(true);
        
        int size = market.getSize();
        
        demand(Commodities.CREW, size -3);
        demand(Commodities.ORGANICS, size -1);
        
        supply(MS_commodities.GUTS, size -1);
        
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.CREW, Commodities.ORGANICS);
        
        applyDeficitToProduction(1, deficit, MS_commodities.GUTS);
        
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
