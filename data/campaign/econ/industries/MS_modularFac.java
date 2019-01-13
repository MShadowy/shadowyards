package data.campaign.econ.industries;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_items;
import data.campaign.econ.MS_industries;
import data.campaign.econ.industries.MS_NanoforgePlugin.MS_NanoforgeEffect;

public class MS_modularFac extends HeavyIndustry {
        
        @Override
        public void apply() {
		super.apply(true);
                int size = market.getSize();
                
                boolean prod = MS_industries.PARALLEL_PRODUCTION.equals(getId());
                int shipBonus = 0;
                float qualityBonus = 0.1f;
                if (prod) {
                    qualityBonus = 0.25f;
                }
                
                if (market.hasIndustry(MS_industries.MODULARFACTORIES) || market.hasIndustry(MS_industries.PARALLEL_PRODUCTION)) {
                    demand(Commodities.METALS, size -1);
                    demand(Commodities.RARE_METALS, size -3);
                    demand(MS_items.BATTERIES, size -2);
                
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
        
        @Override
        protected void applyNanoforgeEffects() {
		
		if (nanoforge != null && (market.hasIndustry(MS_industries.MODULARFACTORIES) ||
                        market.hasIndustry(MS_industries.PARALLEL_PRODUCTION))) {
			MS_NanoforgeEffect effect = MS_NanoforgePlugin.NANOFORGE_EFFECTS.get(nanoforge.getId());
			if (effect != null) {
				effect.apply(this);
			}
		}
	}
        
        @Override
        public void setNanoforge(SpecialItemData nanoforge) {
		if (nanoforge == null && this.nanoforge != null && (market.hasIndustry(MS_industries.MODULARFACTORIES) ||
                        market.hasIndustry(MS_industries.PARALLEL_PRODUCTION))) {
			MS_NanoforgeEffect effect = MS_NanoforgePlugin.NANOFORGE_EFFECTS.get(this.nanoforge.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		this.nanoforge = nanoforge;
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
	public boolean isAvailableToBuild() {
		if (!super.isAvailableToBuild()) return false;
                
                boolean hasHeavy = false;
                if (market.getPlanetEntity() != null && (market.hasIndustry(Industries.HEAVYINDUSTRY) ||
                        market.hasIndustry(Industries.ORBITALWORKS))) {
                    hasHeavy = true;
                }
                
                if (hasHeavy) {
                    return false;
                } else if (!hasHeavy) {
                    return true;
                }
                
		return false;
	}

	@Override
	public String getUnavailableReason() {
		if (!super.isAvailableToBuild()) return super.getUnavailableReason();
                
		return "Heavy Industry is already present";
	}
}
