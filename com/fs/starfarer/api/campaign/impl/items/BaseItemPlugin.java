package com.fs.starfarer.api.campaign.impl.items;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.InstallableItemPlugins.MS_InstallableIndustryItemPlugin_RecInd;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class BaseItemPlugin extends BaseSpecialItemPlugin {

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult,
                       float glowMult, SpecialItemRendererAPI renderer) {
    }

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        return super.getPrice(market, submarket);
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource, false);
        float opad = 10f;

        MS_InstallableIndustryItemPlugin_RecInd.ItemEffect effect = MS_InstallableIndustryItemPlugin_RecInd.ITEM_EFFECTS.get(getId());
        if (effect != null) {
            effect.addItemDescription(tooltip, new SpecialItemData(getId(), null), InstallableItemDescriptionMode.CARGO_TOOLTIP);
        }

        addCostLabel(tooltip, opad, transferHandler, stackSource);

    }
}




