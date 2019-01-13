package data.campaign.econ;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class MS_gutsExporter extends BaseMarketConditionPlugin {
 
    CommodityOnMarketAPI ITEM;
    
    @Override
    public void apply(String id) {
        super.apply(id);
        
        int SUPPLY;
            
        switch (market.getSize()) {
            case 3:
            case 4:
            case 5:
                SUPPLY = 1;
                break;
            case 6:
            case 7:
            case 8:
                SUPPLY = 2;
                break;
            case 9:
            case 10:
                SUPPLY = 3;
                break;
            default:
                SUPPLY = 0;
                break;
        }
        
        if (ITEM.getDemandClass().equals(Commodities.ORGANS)) {
            ITEM.setMaxDemand(ITEM.getMaxDemand() - SUPPLY);
        }
        
        if (ITEM.getCommodity().equals(MS_items.GUTS)) {
            ITEM.setMaxDemand(SUPPLY);
        }
    }
 
    @Override
    public void unapply(String id) {
        super.unapply(id);
    }
}
