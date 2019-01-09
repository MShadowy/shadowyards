package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
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
        SectorEntityToken stationTarget;
        PlanetAPI star; 
        
        if (planet != null) {
            if (!planet.getOrbitFocus().isStar()) {
                stationTarget = planet.getOrbitFocus();
                star = stationTarget.getStarSystem().getStar();
            } else {
                star = planet.getStarSystem().getStar();
            }
        } else {
            star = planet;
        }
        
        int BONUS;
        
        if (star != null) {
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
                case "star_red_dwarf":
                    BONUS = -1;
                    break;
                default:
                    BONUS = 0;
                    break;
            }
        } else {
            BONUS = 0;
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
            SectorEntityToken stationTarget;
            PlanetAPI star; 
        
            if (planet != null) {
                if (!planet.getOrbitFocus().isStar()) {
                    stationTarget = planet.getOrbitFocus();
                    star = stationTarget.getStarSystem().getStar();
                } else {
                    star = planet.getStarSystem().getStar();
                }
            } else {
                star = planet;
            }
        
            int BONUS = 0;
        
            if (star !=  null) {
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
                case "star_red_dwarf":
                    BONUS = -1;
                    break;
                default:
                    BONUS = 0;
                    break;
                }
            }
            
            if (star != null && star.getTypeId().equals("star_red_dwarf")) {
                tooltip.addPara("The dim light of this star reduces Capacitor production by %s", opad, h, "" + Math.abs(BONUS));
            } else {
                tooltip.addPara("An additional %s Capacitors are being produced due to the output of this star", opad, h, "" + Math.abs(BONUS));
            }
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
        
        @Override
	public boolean isAvailableToBuild() {
		if (!super.isAvailableToBuild()) return false;
                
                boolean notStar = false;
                PlanetAPI planet = market.getPlanetEntity();
                SectorEntityToken stationTarget;
                PlanetAPI star; 
        
                if (planet != null) {
                if (!planet.getOrbitFocus().isStar()) {
                    stationTarget = planet.getOrbitFocus();
                    star = stationTarget.getStarSystem().getStar();
                } else {
                    star = planet.getStarSystem().getStar();
                }
                } else {
                    star = planet;
                }
                
                if (star != null && (star.getTypeId().contains("nebula_center_old") || star.getTypeId().contains("nebula_center_average")
                        || star.getTypeId().contains("nebula_center_young") || star.getTypeId().contains("star_neutron") ||
                            star.getTypeId().contains("black_hole") || !star.isStar())) {
                    notStar = true;
                }
                
                if (notStar) {
                    return false;
                } else if (!notStar) {
                    return true;
                }
                
		return false;
	}

	@Override
	public String getUnavailableReason() {
		if (!super.isAvailableToBuild()) return super.getUnavailableReason();
                
		return "Can only build around a stable star";
	}
}
