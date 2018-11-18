package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

public class MS_clonedParts extends BaseMarketConditionPlugin {
    
    @Override
    public void apply(String id) {
        market.getCommodityData("ms_clonedParts").getCommodityMarketData();
    }
    
    @Override
    public void unapply(String id) {
        market.getCommodityData("ms_clonedParts").addToStockpile(0);
    }
}
