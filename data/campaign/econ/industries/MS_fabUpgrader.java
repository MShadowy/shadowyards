package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_industries;
import data.campaign.econ.MS_items;
import java.awt.Color;

public class MS_fabUpgrader extends BaseIndustry  {
    @Override
    public void apply() {
        super.apply(true);
        
        int size = market.getSize();
        float SHIP_QUALITY_BONUS = 0.1f;
        int HEAVY_MACHINERY_SUPPLY = 3;
        int SUPPLY_QUANTITY = 3;
        int HAND_WEAPON_SUPPLY = 3;
        int SHIP_SUPPLY = 3;
                
        //adjust outputs according to each specialization
        //average production in everything, small ship quality bonus, but no weaknesses
        if (market.hasIndustry(MS_industries.PARALLEL_PRODUCTION)) {
            SHIP_QUALITY_BONUS = 0.0f;
            HEAVY_MACHINERY_SUPPLY = 0;
            SUPPLY_QUANTITY = 2;
        }
        if (market.hasIndustry(MS_industries.MILITARY_LINES)) {
            SHIP_QUALITY_BONUS = 0.2f;
            HEAVY_MACHINERY_SUPPLY = 4;
            SUPPLY_QUANTITY = 0;
            HAND_WEAPON_SUPPLY = 1;
            SHIP_SUPPLY = 4;
        }
        if (market.hasIndustry(MS_industries.SHIPYARDS)) {
            SHIP_QUALITY_BONUS = 0.6f;
            SUPPLY_QUANTITY = 4;
            HAND_WEAPON_SUPPLY = 4;
            SHIP_SUPPLY = 0;
        }
                
        if (market.hasIndustry(MS_industries.MODULARFACTORIES) || market.hasIndustry(MS_industries.PARALLEL_PRODUCTION)
                        || market.hasIndustry(MS_industries.MILITARY_LINES) || market.hasIndustry(MS_industries.SHIPYARDS)) {
            demand(Commodities.METALS, size -1);
            demand(Commodities.RARE_METALS, size -3);
            demand(MS_items.BATTERIES, size -2);
                
            supply(Commodities.HEAVY_MACHINERY, size - HEAVY_MACHINERY_SUPPLY);
            supply(Commodities.SUPPLIES, size - SUPPLY_QUANTITY);
            supply(Commodities.HAND_WEAPONS, size - HAND_WEAPON_SUPPLY);
            supply(Commodities.SHIPS, size - SHIP_SUPPLY);
                
            Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS, Commodities.RARE_METALS, MS_items.BATTERIES);
            int maxDeficit = size -3;
            if (deficit.two > maxDeficit) deficit.two = maxDeficit;

            applyDeficitToProduction(2, deficit,
			Commodities.HEAVY_MACHINERY,
			Commodities.SUPPLIES,
			Commodities.HAND_WEAPONS,
			Commodities.SHIPS);
                
            if (SHIP_QUALITY_BONUS > 0) {
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(1), SHIP_QUALITY_BONUS, "Massively parallel fabricators");
            }
		
            float stability = market.getPrevStability();
            if (stability < 5) {
		float stabilityMod = (stability - 5f) / 5f;
		stabilityMod *= 0.5f;
			
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, getNameForModifier() + " - low stability");
            }
        }
                
	if (!isFunctional()) {
            supply.clear();
            unapply();
	}
    }
    
    @Override
    public void unapply() {
	super.unapply();
		
	market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
	market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(1));
    }
    
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
	return true;
    }

    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
	return true;
    }
        
    @Override
    protected boolean canImproveToIncreaseProduction() {
	return true;
    }
    
    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        //if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {	
            float SHIP_QUALITY_BONUS = 0.1f;
            if (MS_industries.SHIPYARDS.equals(getId())) SHIP_QUALITY_BONUS = 0.4f;
            if (MS_industries.PARALLEL_PRODUCTION.equals(getId())) SHIP_QUALITY_BONUS = 0.0f;
                
            float total = SHIP_QUALITY_BONUS;
            String totalStr = "+" + (int)Math.round(total * 100f) + "%";
            Color h = Misc.getHighlightColor();
            if (total < 0) {
                h = Misc.getNegativeHighlightColor();
                totalStr = "" + (int)Math.round(total * 100f) + "%";
            }
            float opad = 10f;
            if (total >= 0) {
                tooltip.addPara("Ship quality: %s", opad, h, totalStr);
                tooltip.addPara("*Quality bonus only applies for the largest ship producer in the faction.", 
				Misc.getGrayColor(), opad);
            }	
        }
    }
    
    //comment this out if you want the item to persist through the upgrade - it will be lost otherwise.
    //note that you will have to implement IndustryItemUser and the respective methods from ReconfigurableInd here as well if you do so.
    //this could be done by either extending ReconfiguranbleInd, by changing the relevant methods to not fire when the ID is not base, 
    //or by copying the relevant methods from the other class (bad form)
    
    protected void upgradeFinished(Industry previous) {
        sendBuildOrUpgradeMessage();
    }
}
