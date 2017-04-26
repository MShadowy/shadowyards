package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class MS_sunkenCities extends BaseMarketConditionPlugin {
    
    @Override
    public void apply (String id) {
        
        market.getDemand(Commodities.ORGANICS).getDemand().modifyMult(id, MS_conditionData.MS_SUNKEN_CITIES_DEMAND_MULT);
        market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().modifyMult(id, MS_conditionData.MS_SUNKEN_CITIES_DEMAND_MULT);
        market.getDemand(Commodities.METALS).getDemand().modifyMult(id, MS_conditionData.MS_SUNKEN_CITIES_DEMAND_MULT);
        market.getDemand(Commodities.DOMESTIC_GOODS).getDemand().modifyMult(id, MS_conditionData.MS_SUNKEN_CITIES_DEMAND_MULT);
    }
    
    @Override
    public void unapply (String id) {
        
        market.getDemand(Commodities.ORGANICS).getDemand().unmodify(id);
	market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().unmodify(id);
	market.getDemand(Commodities.METALS).getDemand().unmodify(id);
	market.getDemand(Commodities.DOMESTIC_GOODS).getDemand().unmodify(id);
    }
}
