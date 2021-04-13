package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.campaign.econ.Industry;

public class UpgradedInd extends BaseIndustry {
    @Override
    public void apply() {
        super.apply(true);
    }
    
    //comment this out if you want the item to persist through the upgrade - it will be lost otherwise.
    //note that you will have to implement IndustryItemUser and the respective methods from ReconfigurableInd here as well if you do so.
    //this could be done by either extending ReconfiguranbleInd, by changing the relevant methods to not fire when the ID is not base, 
    //or by copying the relevant methods from the other class (bad form)
    
    protected void upgradeFinished(Industry previous) {
        sendBuildOrUpgradeMessage();
    }
}
