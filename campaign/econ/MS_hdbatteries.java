package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

public class MS_hdbatteries extends BaseMarketConditionPlugin {
 
    @Override
    public void apply(String id) {
        market.getCommodityData("ms_hdbatteries").getSupply().modifyFlat(id, 10000f);
    }
 
    @Override
    public void unapply(String id) {
        market.getCommodityData("ms_hdbatteries").getSupply().unmodify(id);
    }
 
}
