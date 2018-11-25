package data.campaign.econ.industries;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI.MarketInteractionMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_items;
import data.campaign.econ.MS_industries;
import data.campaign.econ.industries.MS_NanoforgePlugin.MS_NanoforgeEffect;

public class MS_modularFac extends BaseIndustry {
        
        @Override
        public void apply() {
		super.apply(true);
                int size = market.getSize();
                
                boolean works = MS_industries.PARALLEL_PRODUCTION.equals(getId());
                int shipBonus = 0;
                float qualityBonus = 0.1f;
                if (works) {
                    qualityBonus = 0.25f;
                }
                
                demand(Commodities.METALS, size -1);
                demand(Commodities.RARE_METALS, size -3);
                demand(MS_items.BATTERIES, size -5);
                
                supply(Commodities.HEAVY_MACHINERY, size - 3);
		supply(Commodities.SUPPLIES, size - 3);
		supply(Commodities.HAND_WEAPONS, size - 3);
		supply(Commodities.SHIPS, size - 3);
		if (shipBonus > 0) {
			supply(1, Commodities.SHIPS, shipBonus, "Massively parallel fabricators");
		}
                
                Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS, Commodities.RARE_METALS, MS_items.BATTERIES);
		int maxDeficit = size -4;
                if (deficit.two > maxDeficit) deficit.two = maxDeficit;

		applyDeficitToProduction(2, deficit,
					Commodities.HEAVY_MACHINERY,
					Commodities.SUPPLIES,
					Commodities.HAND_WEAPONS,
					Commodities.SHIPS);

                applyNanoforgeEffects();
                
		if (qualityBonus > 0) {
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(1), qualityBonus, "Massively parallel fabricators");
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
                
                if (nanoforge != null) {
			MS_NanoforgeEffect effect = MS_NanoforgePlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(1));
        }
        
        @Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);
		
		if (previous instanceof MS_modularFac) {
			setNanoforge(((MS_modularFac) previous).getNanoforge());
		}
	}
        
        protected void applyNanoforgeEffects() {
		
		if (nanoforge != null) {
			MS_NanoforgeEffect effect = MS_NanoforgePlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
			if (effect != null) {
				effect.apply(this);
			}
		}
	}
        
        protected SpecialItemData nanoforge = null;
        
        public void setNanoforge(SpecialItemData nanoforge) {
		if (nanoforge == null && this.nanoforge != null) {
			MS_NanoforgeEffect effect = MS_NanoforgePlugin.NANOFORGE_EFFECTS.get(this.nanoforge.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		this.nanoforge = nanoforge;
	}

	public SpecialItemData getNanoforge() {
		return nanoforge;
	}
	
        @Override
	public SpecialItemData getSpecialItem() {
		return nanoforge;
	}
	
        @Override
	public void setSpecialItem(SpecialItemData special) {
		nanoforge = special;
	}
	
	@Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		if (nanoforge != null && Items.CORRUPTED_NANOFORGE.equals(nanoforge.getId()) &&
				data != null && Items.PRISTINE_NANOFORGE.equals(data.getId())) {
			return true;
		}
		
		return nanoforge == null && 
				data != null &&
				MS_NanoforgePlugin.NANOFORGE_EFFECTS.containsKey(data.getId());
	}
	
	@Override
	protected void addPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, IndustryTooltipMode mode) {
		super.addPostSupplySection(tooltip, hasSupply, mode);
	}
        
        @Override
	public void notifyBeingRemoved(MarketInteractionMode mode, boolean forUpgrade) {
		super.notifyBeingRemoved(mode, forUpgrade);
		if (nanoforge != null && !forUpgrade) {
			CargoAPI cargo = getCargoForInteractionMode(mode);
			if (cargo != null) {
				cargo.addSpecial(nanoforge, 1);
			}
		}
	}
        
        @Override
	protected boolean addNonAICoreInstalledItems(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		if (nanoforge == null) return false;

		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
		
		SpecialItemSpecAPI nanoforgeSpec = Global.getSettings().getSpecialItemSpec(nanoforge.getId());
		
		TooltipMakerAPI text = tooltip.beginImageWithText(nanoforgeSpec.getIconName(), 48);
		MS_NanoforgeEffect effect = MS_NanoforgePlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
		effect.addItemDescription(text, nanoforge, InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
		tooltip.addImageWithText(opad);
		
		return true;
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
	public List<InstallableIndustryItemPlugin> getInstallableItems() {
		ArrayList<InstallableIndustryItemPlugin> list = new ArrayList<>();
		list.add(new MS_NanoforgePlugin(this));
		return list;
	}

	@Override
	public void initWithParams(List<String> params) {
		super.initWithParams(params);
		
		for (String str : params) {
			if (MS_NanoforgePlugin.NANOFORGE_EFFECTS.containsKey(str)) {
				setNanoforge(new SpecialItemData(str, null));
				break;
			}
		}
	}

	@Override
	public List<SpecialItemData> getVisibleInstalledItems() {
		List<SpecialItemData> result = super.getVisibleInstalledItems();
		
		if (nanoforge != null) {
			result.add(nanoforge);
		}
		
		return result;
	}
	
        @Override
	public float getPatherInterest() {
		float base = 2f;
		if (nanoforge != null) base += 4f;
		return 2f + super.getPatherInterest();
	}
}
