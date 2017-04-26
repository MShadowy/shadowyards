package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class MS_medCenter extends BaseMarketConditionPlugin {
    @Override
    public void apply(String id) {
        market.getDemand(Commodities.CREW).getDemand().modifyFlat(id, MS_conditionData.MS_MEDCENTER_CREW_DEMAND);
	market.getDemand(Commodities.CREW).getNonConsumingDemand().modifyFlat(id, MS_conditionData.MS_MEDCENTER_CREW_DEMAND * MS_conditionData.CREW_MARINES_NON_CONSUMING_FRACTION);
	float crewDemandMet = market.getDemand(Commodities.CREW).getClampedFractionMet();
        
        market.getDemand(Commodities.ORGANS).getDemand().modifyFlat(id, MS_conditionData.MS_MEDCENTER_ORGANS_DEMAND);
        
        market.getCommodityData("ms_clonedOrgans").getSupply().modifyFlat(id, MS_conditionData.MS_MEDCENTER_CLONED_ORGANS * (crewDemandMet));
    }
    
    @Override
    public void unapply(String id) {
        market.getDemand(Commodities.CREW).getDemand().unmodify(id);
        market.getDemand(Commodities.CREW).getNonConsumingDemand().unmodify(id);
        
        market.getDemand(Commodities.ORGANS).getDemand().unmodify(id);
        
        market.getCommodityData("ms_clonedOrgans").getSupply().unmodify(id);
    }
}
