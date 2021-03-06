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
                SUPPLY = 2;
                break;
            case 8:
            case 9:
                SUPPLY = 3;
                break;
            case 10:
            default:
                SUPPLY = 0;
                break;
        }
        
        if (ITEM.getDemandClass().equals(Commodities.ORGANS)) {
            ITEM.setMaxDemand(ITEM.getMaxDemand() - SUPPLY);
        }
        
        if (ITEM.getCommodity().getId().equals(MS_items.GUTS)) {
            ITEM.setMaxDemand(SUPPLY);
        }
    }
 
    @Override
    public void unapply(String id) {
        super.unapply(id);
    }
}
