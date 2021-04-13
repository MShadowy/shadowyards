//courtesy of Sir Hartley
//kept in its original state for reference purposes
package com.fs.starfarer.api.impl.campaign.InstallableItemPlugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.IndustryItemUser;
import com.fs.starfarer.api.impl.campaign.econ.impl.ReconfigurableInd;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.RecInd_ids.*;

/**
 * Very important for implementations of this to not store *any* references to campaign data in data members, since
 * this is stored in a static map and persists between game loads etc.
 * <p>
 * This is an example implementation for three installable industry items - adjacent to vanilla.
 */

public class InstallableIndustryItemPlugin_RecInd extends BaseInstallableIndustryItemPlugin {

    public interface ItemEffect {
        void apply(IndustryItemUser industry);

        void unapply(IndustryItemUser industry);

        void addItemDescription(TooltipMakerAPI text, SpecialItemData data, InstallableItemDescriptionMode mode);
    }

    public static final Map<String, ItemEffect> ITEM_EFFECTS = new HashMap<String, ItemEffect>() {{
        put(ITEM_1, new ItemEffect() {

            @Override
            public void apply(IndustryItemUser industry) {
                industry.applyItemEffects(ITEM_1);
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

                text.addPara(pre + "Your item 1 description goes here.",
                        pad,
                        Misc.getHighlightColor(),
                        "goes here.");
            }
        });

        put(ITEM_2, new ItemEffect() {

            @Override
            public void apply(IndustryItemUser industry) {
                industry.applyItemEffects(ITEM_2);
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

                text.addPara(pre + "Your item 2 description goes here.",
                        pad,
                        Misc.getHighlightColor(),
                        "goes here.");
            }
        });

        put(ITEM_3, new ItemEffect() {

            @Override
            public void apply(IndustryItemUser industry) {
                industry.applyItemEffects(ITEM_3);
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

                text.addPara(pre + "Your item 3 description goes here.",
                        pad,
                        Misc.getHighlightColor(),
                        "goes here.");
            }
        });
    }};

    private final ReconfigurableInd industry;

    public InstallableIndustryItemPlugin_RecInd(ReconfigurableInd industry) {
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



