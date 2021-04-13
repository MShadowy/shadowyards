package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.MS_items;
import data.campaign.econ.MS_industries;
import com.fs.starfarer.api.impl.campaign.InstallableItemPlugins.MS_InstallableIndustryItemPlugin_RecInd;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.impl.campaign.ids.MSInd_ids.*;
import static com.fs.starfarer.api.impl.campaign.InstallableItemPlugins.MS_InstallableIndustryItemPlugin_RecInd.ITEM_EFFECTS;
import com.fs.starfarer.api.impl.campaign.econ.impl.IndustryItemUser;

public class MS_modularFac extends BaseIndustry implements IndustryItemUser {
    
        protected String currentUpgrade = null;
    
        @Override
        public void apply() {
		super.apply(true);
                applySpecialItemEffects(getSpecialItem());
                
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
                applySpecialItemEffects(null);
		
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(1));
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
        public IndustrySpecAPI getSpec() {
            if (spec == null) spec = Global.getSettings().getIndustrySpec(id);
            if (currentUpgrade != null) spec.setUpgrade(currentUpgrade);

            return spec;
        }
    
        @Override
        public void applyItemEffects(String id) {
            String upgradeID = null;
            switch (id) {
                case PARALLEL_TOOLING_PACKAGE:
                    upgradeID = PARALLEL_FABS;
                    break;
                case MILITARY_LOGISTICS_UPGRADE:
                    upgradeID = MIL_LINES;
                    break;
                case SPECIALIZED_SYSTEMS_FABRICATORS:
                    upgradeID = SHIPYARDS;
                    break;
            }

            currentUpgrade = upgradeID;
            getSpec().setUpgrade(upgradeID);
        }
        
        @Override
        public void unapplyItemEffects() {
            currentUpgrade = null;
            getSpec().setUpgrade(null);
        }
        
        public void setSpecialItemEffects(SpecialItemData special) {
            applySpecialItemEffects(special);
            this.special = special;
        }

        protected void applySpecialItemEffects(SpecialItemData data) {
            if (data != null) {
                MS_InstallableIndustryItemPlugin_RecInd.ItemEffect effect = ITEM_EFFECTS.get(data.getId());

                if (effect != null) {
                    effect.apply(this);
                }
            } else {
                unapplyItemEffects();
            }
        }
        
        @Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
            return getSpecialItem() == null &&
                    data != null &&
                    ITEM_EFFECTS.containsKey(data.getId());
	}
        
        @Override
        public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
            super.notifyBeingRemoved(mode, forUpgrade);
            if (getSpecialItem() != null && !forUpgrade) {

                CargoAPI cargo = getCargoForInteractionMode(mode);
                if (cargo != null) {
                    cargo.addSpecial(getSpecialItem(), 1);
                }
            }
        }
        
        @Override
        protected boolean addNonAICoreInstalledItems(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
            if (getSpecialItem() == null) return false;

            float opad = 10f;

            SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(getSpecialItem().getId());

            TooltipMakerAPI text = tooltip.beginImageWithText(spec.getIconName(), 48);
            MS_InstallableIndustryItemPlugin_RecInd.ItemEffect effect = ITEM_EFFECTS.get(getSpecialItem().getId());
            if (effect != null) {
                effect.addItemDescription(text, getSpecialItem(), InstallableIndustryItemPlugin.InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
            }
            tooltip.addImageWithText(opad);

            return true;
        }
        
        @Override
        public List<InstallableIndustryItemPlugin> getInstallableItems() {
            ArrayList<InstallableIndustryItemPlugin> list = new ArrayList<>();

            //this makes the item unremoveable while upgrading, hides the button
            if (getSpecialItem() != null && isUpgrading()) return list;

            list.add(new MS_InstallableIndustryItemPlugin_RecInd(this));
            return list;
        }

        @Override
        public void initWithParams(List<String> params) {
            super.initWithParams(params);

            for (String str : params) {
                if (ITEM_EFFECTS.containsKey(str)) {
                    setSpecialItem(new SpecialItemData(str, null));
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