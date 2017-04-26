package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class MS_Arcologies extends BaseMarketConditionPlugin {
    
    @Override
    public void apply (String id) {
        
        market.getDemand(Commodities.DOMESTIC_GOODS).getDemand().modifyMult(id, MS_conditionData.MS_ARCOLOGIES_DEMAND_MULT);
        market.getDemand(Commodities.LUXURY_GOODS).getDemand().modifyMult(id, MS_conditionData.MS_ARCOLOGIES_DEMAND_MULT);
        market.getDemand(Commodities.ORGANS).getDemand().modifyMult(id, MS_conditionData.MS_ARCOLOGIES_DEMAND_MULT);
        market.getDemand(Commodities.DRUGS).getDemand().modifyMult(id, MS_conditionData.MS_ARCOLOGIES_DEMAND_MULT);
    }
    
    @Override
    public void unapply (String id) {
        
        market.getDemand(Commodities.DOMESTIC_GOODS).getDemand().unmodify(id);
	market.getDemand(Commodities.LUXURY_GOODS).getDemand().unmodify(id);
	market.getDemand(Commodities.ORGANS).getDemand().unmodify(id);
	market.getDemand(Commodities.DRUGS).getDemand().unmodify(id);
    }
}
