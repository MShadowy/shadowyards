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
import java.awt.Color;

public class MS_medCenter extends BaseIndustry implements MarketImmigrationModifier {
    
    @Override
    public void apply() {
        super.apply(true);
        
        /*int size = market.getSize();
        int bat = 1;
        if (size - 5 > 1) {
            bat = size - 5;
        }
        
        demand(Commodities.CREW, size -3);
        demand(Commodities.ORGANICS, size -1);
        demand(MS_items.BATTERIES, bat);
        
        supply(MS_items.GUTS, size -4);
        
        int WANT;
        int DEMAND = 0;
        int SUPPLY = size -4;
        float COST = 0;
            
        switch (market.getSize()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                WANT = 1;
                break;
            case 6:
                WANT = 2;
                break;
            case 7:
            case 8:
                WANT = 3;
                break;
            case 9:
            case 10:
                WANT = 4;
                break;
            default:
                WANT = 0;
                break;
        }
        
        List<CommodityOnMarketAPI> WIDGET = market.getAllCommodities();
        
        for (CommodityOnMarketAPI w : WIDGET) {
            if (!w.getId().contains(Commodities.ORGANS) || !w.getId().contains(MS_items.GUTS)) {
                continue;
            }
            
            if (w.getId().contains(Commodities.ORGANS)) {
                for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                    if (m != null) {
                        if (SUPPLY >= WANT) {
                            DEMAND += w.getMaxDemand() - WANT;
                        } else {
                            DEMAND += SUPPLY;
                        }
                    }
                }
            }
            if (w.getId().contains(MS_items.GUTS)) {
                COST = w.getCommodity().getBasePrice();
            }
        }
        
        float CREDS = (DEMAND * COST);
        float export = Global.getSettings().getIndustrySpec(id).getIncome();
        
        if (CREDS != 0) {
            export += CREDS;
            Global.getSettings().getIndustrySpec(id).setIncome(export);
        }
        
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.CREW, Commodities.ORGANICS);
        
        applyDeficitToProduction(1, deficit, MS_items.GUTS);*/
        
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
	float want = getDemand(Commodities.ORGANICS).getQuantity().getModifiedValue();
	float def = deficit.two;
	if (def > want) def = want;
		
		float mult = 1f;
		if (def > 0 && want > 0) {
			mult = (want - def) / want;
		}
		
		return getMaxPopGrowthBonus() * mult;
    }
	
    protected float getMaxPopGrowthBonus() {
	//return market.getSize() * 10f;
	return getSizeMult() * 2f;
    }
}