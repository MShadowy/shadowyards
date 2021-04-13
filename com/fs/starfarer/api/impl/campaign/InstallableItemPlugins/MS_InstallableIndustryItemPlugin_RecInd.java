package com.fs.starfarer.api.impl.campaign.InstallableItemPlugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.IndustryItemUser;
import data.campaign.econ.industries.MS_modularFac;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.MSInd_ids.*;

public class MS_InstallableIndustryItemPlugin_RecInd extends BaseInstallableIndustryItemPlugin {
    
    public interface ItemEffect {
        void apply(IndustryItemUser industry);
        
        void unapply(IndustryItemUser industry);
        
        void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode);
    }
    
    public static final Map<String, ItemEffect> ITEM_EFFECTS = new HashMap<String, ItemEffect>() {{
        put(PARALLEL_TOOLING_PACKAGE, new ItemEffect() {
            @Override
            public void apply(IndustryItemUser industry) {
                industry.applyItemEffects(PARALLEL_TOOLING_PACKAGE);
            }

            @Override
            public void unapply(IndustryItemUser industry) {
                industry.unapplyItemEffects();
            }

            public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
                String id = data.getId();
                String name = Global.getSettings().getSpecialItemSpec(id).getName();
                String pre = "";

                float pad = 0f;
                if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST ||
                        mode == InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
                    pre = name + ". ";
                } else if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED ||
                        mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP) {
                    pre = name + " currently installed. ";
                }
                if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP ||
                        mode == InstallableItemDescriptionMode.CARGO_TOOLTIP) {
                    pad = 10f;
                }

                text.addPara(pre + "A microfactory pre-programmed with the specifications and expert systems necessary to deploy a comprehensive upgrade package to Modular Fabricators to improve their civilian industrial output.",
                        pad,
                        Misc.getHighlightColor(),
                        "civilian industrial output.");
            }
        });
        
        put(MILITARY_LOGISTICS_UPGRADE, new ItemEffect() {
            @Override
            public void apply(IndustryItemUser industry) {
                industry.applyItemEffects(MILITARY_LOGISTICS_UPGRADE);
            }

            @Override
            public void unapply(IndustryItemUser industry) {
                industry.unapplyItemEffects();
            }

            public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
                String id = data.getId();
                String name = Global.getSettings().getSpecialItemSpec(id).getName();
                String pre = "";

                float pad = 0f;
                if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST ||
                        mode == InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
                    pre = name + ". ";
                } else if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED ||
                        mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP) {
                    pre = name + " currently installed. ";
                }
                if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP ||
                        mode == InstallableItemDescriptionMode.CARGO_TOOLTIP) {
                    pad = 10f;
                }

                text.addPara(pre + "A microfactory pre-programmed with the specifications and expert systems necessary to deploy a number of auxiliary industry sites suitable for the production of supplies and high quality military equipment.",
                        pad,
                        Misc.getHighlightColor(),
                        "supplies and high quality military equipment.");
            }
        });
        
        put(SPECIALIZED_SYSTEMS_FABRICATORS, new ItemEffect() {
            @Override
            public void apply(IndustryItemUser industry) {
                industry.applyItemEffects(SPECIALIZED_SYSTEMS_FABRICATORS);
            }

            @Override
            public void unapply(IndustryItemUser industry) {
                industry.unapplyItemEffects();
            }

            public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
                String id = data.getId();
                String name = Global.getSettings().getSpecialItemSpec(id).getName();
                String pre = "";

                float pad = 0f;
                if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST ||
                        mode == InstallableItemDescriptionMode.INDUSTRY_TOOLTIP) {
                    pre = name + ". ";
                } else if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_INSTALLED ||
                        mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP) {
                    pre = name + " currently installed. ";
                }
                if (mode == InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP ||
                        mode == InstallableItemDescriptionMode.CARGO_TOOLTIP) {
                    pad = 10f;
                }

                text.addPara(pre + "A microfactory pre-programmed with the specifications and expert systems necessary to deploy specialist facilities purpose built for constructing high quality spaceship parts.",
                        pad,
                        Misc.getHighlightColor(),
                        "high quality spaceship parts.");
            }
        });
    }};
    
    private final MS_modularFac industry;
    
    public MS_InstallableIndustryItemPlugin_RecInd(MS_modularFac industry) {
        this.industry = industry;
    }
    
    @Override
    public boolean isInstallableItem(CargoStackAPI stack) {
        if (!stack.isSpecialStack()) return false;
        
        return ITEM_EFFECTS.containsKey(stack.getSpecialDataIfSpecial().getId());
    }
    
    @Override
    public String getMenuItemTitle() {
        return getCurrentlyInstalledItemData() == null ? "Install Item..." : "Manage Item...";
    }
    
    @Override
    public String getUninstallButtonText() {
        return "Uninstall Item";
    }
    
    @Override
    public SpecialItemData getCurrentlyInstalledItemData() {
        return industry.getSpecialItem();
    }
    
    @Override
    public void setCurrentlyInstalledItemData(SpecialItemData data) {
        industry.setSpecialItem(data);
    }
    
    @Override
    public String getNoItemCurrentlyInstalledText() {
        return "No Item currently installed";
    }

    @Override
    public String getNoItemsAvailableText() {
        return "No Item available";
    }

    @Override
    public String getNoItemsAvailableTextRemote() {
        return "No Item available in storage";
    }

    @Override
    public String getSelectItemToAssignToIndustryText() {
        return "Select Item to install for " + industry.getCurrentName();
    }
    
    @Override
    public void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode) {
        ItemEffect effect = ITEM_EFFECTS.get(data.getId());
        if (effect != null) effect.addItemDescription(text, data, mode);
    }

    @Override
    public boolean isMenuItemTooltipExpandable() {
        return false;
    }

    @Override
    public float getMenuItemTooltipWidth() {
        return super.getMenuItemTooltipWidth();
    }

    @Override
    public boolean hasMenuItemTooltip() {
        return super.hasMenuItemTooltip();
    }

    @Override
    public void createMenuItemTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        float opad = 10f;

        tooltip.addPara("Items have different uses depending on the structure they are installed in.", 0f);

        SpecialItemData data = industry.getSpecialItem();
        if (data == null) {
            tooltip.addPara(getNoItemCurrentlyInstalledText() + ".", opad);
        } else {
            ItemEffect effect = ITEM_EFFECTS.get(data.getId());
            effect.addItemDescription(tooltip, data, InstallableItemDescriptionMode.INDUSTRY_MENU_TOOLTIP);
        }
    }
}
