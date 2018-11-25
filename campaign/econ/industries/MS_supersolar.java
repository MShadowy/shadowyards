package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_items;
import java.awt.Color;

public class MS_supersolar extends BaseIndustry {
    
    @Override
    public void apply() {
	super.apply(true);
               
        int size = market.getSize();
        
        PlanetAPI planet = market.getPlanetEntity();
        PlanetAPI star = planet.getStarSystem().getStar();
        
        int BONUS;
        
        switch (star.getTypeId()) {
            case "star_orange":
            case "star_orange_giant":
                BONUS = 1;
                break;
            case "star_yellow":
                BONUS = 2;
                break;
            case "star_blue_giant":
            case "star_blue_supergiant":
                BONUS = 3;
                break;
            default:
                BONUS = 0;
                break;
        }
        
        demand(Commodities.CREW, size-1);
        demand(Commodities.HEAVY_MACHINERY, size -2);
        
        supply(MS_items.BATTERIES, size +BONUS);
        
        Pair<String, Integer> deficit = getMaxDeficit(Commodities.CREW, Commodities.HEAVY_MACHINERY);
        
        applyDeficitToProduction(1, deficit, MS_items.BATTERIES);
        
        if (!isFunctional()) {
            supply.clear();
        }
    }

    @Override
    public void unapply() {
	super.unapply();
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
            
            PlanetAPI planet = market.getPlanetEntity();
            PlanetAPI star = planet.getStarSystem().getStar();
        
            int BONUS;
        
            switch (star.getTypeId()) {
                case "star_orange":
                case "star_orange_giant":
                    BONUS = 1;
                    break;
                case "star_yellow":
                    BONUS = 2;
                    break;
                case "star_blue_giant":
                case "star_blue_supergiant":
                    BONUS = 3;
                    break;
                default:
                    BONUS = 0;
                    break;
            }
	
            tooltip.addPara("An additional %s Capacitors are being produced due to the output of this star", opad, h, "" + Math.abs(BONUS));
	}
    }
    
    @Override
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
	return true;
    }

    @Override
    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
	return true;
    }
    
    public void getSolarProductionBoost(int BONUS) {
        
    }
}
