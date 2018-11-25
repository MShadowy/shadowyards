package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_items;
import java.awt.Color;
//import java.util.List;

public class MS_medCenter extends BaseIndustry implements MarketImmigrationModifier {
    
    @Override
    public void apply() {
        super.apply(true);
        
        int size = market.getSize();
        
        demand(Commodities.CREW, size -3);
        demand(Commodities.ORGANICS, size -1);
        
        supply(MS_items.GUTS, size -4);
        
        /*int SUPPLY;
            
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
        
        List<CommodityOnMarketAPI> WIDGET = market.getAllCommodities();
        
        for (CommodityOnMarketAPI w : WIDGET) {
            if (!w.getId().contains(Commodities.ORGANS) || !w.getId().contains(MS_items.GUTS)) {
                continue;
            }
            
            if (w.getId().contains(Commodities.ORGANS)) {
                w.setMaxDemand(w.getMaxDemand() - SUPPLY);
            }
            if (w.getId().contains(MS_items.GUTS)) {
                w.setMaxDemand(SUPPLY);
            }
        }*/
        
        /*if (WIDGET.equals(Commodities.ORGANS)) {
            ITEM.setMaxDemand(ITEM.getMaxDemand() - SUPPLY);
        }
        
        if (WIDGET.equals(MS_items.GUTS)) {
            ITEM.setMaxDemand(SUPPLY);
        }*/
        
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.CREW, Commodities.ORGANICS);
        
        applyDeficitToProduction(1, deficit, MS_items.GUTS);
        
        if (!isFunctional()) {
            supply.clear();
        }
    }
    
    @Override
    public void unapply() {
        super.unapply();
    }
    
    @Override
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
	return true;
    }

    @Override
    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
	return true;
    }
    
    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
	return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }
	
    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
	if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
            Color h = Misc.getHighlightColor();
            float opad = 10f;
	
            float bonus = getPopulationGrowthBonus();
            float max = getMaxPopGrowthBonus();
	
            tooltip.addPara("Population growth: %s (max for colony size: %s)", opad, h, "+" + Math.round(bonus), "+" + Math.round(max));
	}
    }
    
    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
	if (isFunctional()) {
            incoming.getWeight().modifyFlat(getModId(), getPopulationGrowthBonus(), getNameForModifier());
        }
    }

    protected float getPopulationGrowthBonus() {
	Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS);
	float demand = getDemand(Commodities.ORGANICS).getQuantity().getModifiedValue();
	float def = deficit.two;
	if (def > demand) def = demand;
		
		float mult = 1f;
		if (def > 0 && demand > 0) {
			mult = (demand - def) / demand;
		}
		
		return getMaxPopGrowthBonus() * mult;
    }
	
    protected float getMaxPopGrowthBonus() {
	//return market.getSize() * 10f;
	return getSizeMult() * 2f;
    }
}
