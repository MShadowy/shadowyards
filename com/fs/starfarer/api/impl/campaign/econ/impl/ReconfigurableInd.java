//courtesy of SirHartley
//this is the basic class, kept for reference purposes
//the actual implementation is in
//data.campaign.econ.industries.MS_modularFac
package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.InstallableItemPlugins.InstallableIndustryItemPlugin_RecInd;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.impl.campaign.ids.RecInd_ids.*;
import static com.fs.starfarer.api.impl.campaign.InstallableItemPlugins.InstallableIndustryItemPlugin_RecInd.ITEM_EFFECTS;

public class ReconfigurableInd extends BaseIndustry implements IndustryItemUser {
    
    String currentUpgrade = null;
    
    @Override
    public void apply() {
        super.apply(true);
        applySpecialItemEffects(getSpecialItem());
    }
    
    @Override
    public void unapply() {
        super.unapply();
        applySpecialItemEffects(null);
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
        switch (id){
            case ITEM_1:
                upgradeID = UPGRADE_1;
                break;
            case ITEM_2:
                upgradeID = UPGRADE_2;
                break;
            case ITEM_3:
                upgradeID = UPGRADE_3;
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
            InstallableIndustryItemPlugin_RecInd.ItemEffect effect = ITEM_EFFECTS.get(data.getId());
            
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
        InstallableIndustryItemPlugin_RecInd.ItemEffect effect = ITEM_EFFECTS.get(getSpecialItem().getId());
        effect.addItemDescription(text, getSpecialItem(), InstallableIndustryItemPlugin.InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
        tooltip.addImageWithText(opad);
        
        return true;
    }
    
    @Override
    public List<InstallableIndustryItemPlugin> getInstallableItems() {
        ArrayList<InstallableIndustryItemPlugin> list = new ArrayList<>();
        
        //this makes the item unremoveable while upgrading, hides the button
        if (getSpecialItem() != null && isUpgrading()) return list;
        
        list.add(new InstallableIndustryItemPlugin_RecInd(this));
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
}
