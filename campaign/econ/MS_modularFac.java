package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class MS_modularFac extends BaseMarketConditionPlugin {
        
        @Override
        public void apply(String id) {
            
                //market.getDemand(Commodities.CREW).getDemand().modifyFlat(id, MS_conditionData.MS_MODULARFAC_CREW_DEMAND);
		//market.getDemand(Commodities.CREW).getNonConsumingDemand().modifyFlat(id, MS_conditionData.MS_MODULARFAC_CREW_DEMAND * MS_conditionData.CREW_MARINES_NON_CONSUMING_FRACTION);
		
		market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().modifyFlat(id, MS_conditionData.MS_MODULARFAC_MACHINERY_DEMAND);
		float crewDemandMet = getCrewDemandMet(market);

		
		market.getDemand(Commodities.ORGANICS).getDemand().modifyFlat(id, MS_conditionData.MS_MODULARFAC_ORGANICS * crewDemandMet);
		market.getDemand(Commodities.VOLATILES).getDemand().modifyFlat(id, MS_conditionData.MS_MODULARFAC_VOLATILES * crewDemandMet);
		market.getDemand(Commodities.METALS).getDemand().modifyFlat(id, MS_conditionData.MS_MODULARFAC_METALS * crewDemandMet);
		market.getDemand(Commodities.RARE_METALS).getDemand().modifyFlat(id, MS_conditionData.MS_MODULARFAC_RARE_METALS * crewDemandMet);
		
		float productionMult = getProductionMult(market, Commodities.ORGANICS, Commodities.VOLATILES, Commodities.METALS, Commodities.RARE_METALS) * crewDemandMet;
		
		market.getCommodityData(Commodities.HEAVY_MACHINERY).getSupply().modifyFlat(id, MS_conditionData.MS_MODULARFAC_MACHINERY * productionMult);
		market.getCommodityData(Commodities.SUPPLIES).getSupply().modifyFlat(id, MS_conditionData.MS_MODULARFAC_SUPPLIES * productionMult);
		market.getCommodityData(Commodities.HAND_WEAPONS).getSupply().modifyFlat(id, MS_conditionData.MS_MODULARFAC_HAND_WEAPONS * productionMult);
        }
        
        @Override
        public void unapply(String id) {
                market.getDemand(Commodities.ORGANICS).getDemand().unmodify(id);
		market.getDemand(Commodities.VOLATILES).getDemand().unmodify(id);
		market.getDemand(Commodities.METALS).getDemand().unmodify(id);
		market.getDemand(Commodities.RARE_METALS).getDemand().unmodify(id);
		market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().unmodify(id);
		
		market.getDemand(Commodities.CREW).getDemand().unmodify(id);
		market.getDemand(Commodities.CREW).getNonConsumingDemand().unmodify(id);
		
		market.getCommodityData(Commodities.HEAVY_MACHINERY).getSupply().unmodify(id);
		market.getCommodityData(Commodities.SUPPLIES).getSupply().unmodify(id);
		market.getCommodityData(Commodities.HAND_WEAPONS).getSupply().unmodify(id);
        }
}
