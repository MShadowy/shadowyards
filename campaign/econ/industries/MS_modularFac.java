package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_industries;

public class MS_modularFac extends BaseIndustry {
        
        @Override
        public void apply() {
		super.apply(true);
                int size = market.getSize();
                
                boolean works = MS_industries.PARALLEL_PRODUCTION.equals(getId());
                int shipBonus = 0;
                float qualityBonus = 0.1f;
                if (works) {
                    qualityBonus = 0.4f;
                }
                
                demand(Commodities.METALS, size -1);
                demand(Commodities.RARE_METALS, size -3);
                
                supply(Commodities.HEAVY_MACHINERY, size - 2);
		supply(Commodities.SUPPLIES, size - 2);
		supply(Commodities.HAND_WEAPONS, size - 2);
		supply(Commodities.SHIPS, size - 2);
		if (shipBonus > 0) {
			supply(1, Commodities.SHIPS, shipBonus, "Orbital works");
		}
                
                Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS, Commodities.RARE_METALS);
		int maxDeficit = size -4;
                if (deficit.two > maxDeficit) deficit.two = maxDeficit;

		applyDeficitToProduction(2, deficit,
					Commodities.HEAVY_MACHINERY,
					Commodities.SUPPLIES,
					Commodities.HAND_WEAPONS,
					Commodities.SHIPS);

		if (qualityBonus > 0) {
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(1), qualityBonus, "Orbital works");
		}
		
		float stability = market.getPrevStability();
		if (stability < 5) {
			float stabilityMod = (stability - 5f) / 5f;
			stabilityMod *= 0.5f;
			//market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, "Low stability at production source");
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, getNameForModifier() + " - low stability");
		}
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
        }
        
        @Override
        public void unapply() {
		super.unapply();
        }
        
        @Override
	protected void addPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, IndustryTooltipMode mode) {
		super.addPostSupplySection(tooltip, hasSupply, mode);
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
	public float getPatherInterest() {
		return 2f + super.getPatherInterest();
	}
}
