package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class MS_radiationAbsorbers extends BaseMarketConditionPlugin {
        
        @Override
        public void apply(String id) {
            
                market.getDemand(Commodities.RARE_METALS).getDemand().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_RARE_METALS_DEMAND);
		market.getDemand(Commodities.VOLATILES).getDemand().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_VOLATILES_DEMAND);
		market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_MACHINERY_DEMAND);
		
		market.getDemand(Commodities.MARINES).getDemand().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_MARINES_DEMAND);
		market.getDemand(Commodities.MARINES).getNonConsumingDemand().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_MARINES_DEMAND * MS_conditionData.CREW_MARINES_NON_CONSUMING_FRACTION);
		market.getDemand(Commodities.CREW).getDemand().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_CREW_DEMAND);
		market.getDemand(Commodities.CREW).getNonConsumingDemand().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_CREW_DEMAND * MS_conditionData.CREW_MARINES_NON_CONSUMING_FRACTION);
		
		market.getCommodityData(Commodities.MARINES).getSupply().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_MARINES);
		market.getCommodityData(Commodities.CREW).getSupply().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_CREW);
		
		market.getStability().modifyFlat(id, MS_conditionData.MS_RAD_ABSORBERS_STABILITY, "Radiation Absorbers");
        }
        
        @Override
        public void unapply(String id) {
            
                market.getDemand(Commodities.RARE_METALS).getDemand().unmodify(id);
		market.getDemand(Commodities.VOLATILES).getDemand().unmodify(id);
		market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().unmodify(id);
		
		market.getDemand(Commodities.MARINES).getDemand().unmodify(id);
		market.getDemand(Commodities.MARINES).getNonConsumingDemand().unmodify(id);
		market.getDemand(Commodities.CREW).getDemand().unmodify(id);
		market.getDemand(Commodities.CREW).getNonConsumingDemand().unmodify(id);
		
		market.getCommodityData(Commodities.MARINES).getSupply().unmodify(id);
		market.getCommodityData(Commodities.CREW).getSupply().unmodify(id);
		
		market.getStability().unmodify(id);
        }
}
