package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class MS_supersolar extends BaseMarketConditionPlugin {

        @Override
        public void apply(String id) {
		market.getDemand(Commodities.CREW).getDemand().modifyFlat(id, MS_conditionData.MS_SUPER_SOLAR_CREW);
		market.getDemand(Commodities.CREW).getNonConsumingDemand().modifyFlat(id, MS_conditionData.MS_SUPER_SOLAR_CREW * MS_conditionData.CREW_MARINES_NON_CONSUMING_FRACTION);
		float crewDemandMet = market.getDemand(Commodities.CREW).getClampedFractionMet();
		
		market.getCommodityData("ms_hdbatteries").getSupply().modifyFlat(id, MS_conditionData.MS_SUPER_SOLAR_HDBATTERIES * crewDemandMet);
		market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().modifyFlat(id, MS_conditionData.MS_SUPER_SOLAR_MACHINERY);
	}

        @Override
	public void unapply(String id) {
		market.getCommodityData(Commodities.VOLATILES).getSupply().unmodify(id);
		market.getDemand(Commodities.HEAVY_MACHINERY).getDemand().unmodify(id);
		
		market.getDemand(Commodities.CREW).getDemand().unmodify(id);
		market.getDemand(Commodities.CREW).getNonConsumingDemand().unmodify(id);
	}
 
}
