package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import static data.campaign.econ.MS_commodities.BATTERIES;

public class MS_hdbatteries extends BaseMarketConditionPlugin {
 
    @Override
    public void apply(String id) {
            market.getCommodityData(BATTERIES).getCommodityMarketData();
    }
 
    @Override
    public void unapply(String id) {
        market.getCommodityData(BATTERIES).addToStockpile(0);
    }
 
}
