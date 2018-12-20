package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.HeavyIndustry;
import data.campaign.econ.MS_industries;

public class MS_heavyIndustryChecker extends HeavyIndustry {
    //this is just a temporary solution to get Fabs to block HI
    //Replace ASAP
    
    @Override
    public boolean isAvailableToBuild() {
        boolean hasModular = false;
        
        if (market.getPlanetEntity() != null && (market.hasIndustry(MS_industries.MODULARFACTORIES) || 
                market.hasIndustry(MS_industries.PARALLEL_PRODUCTION))) {
            hasModular = true;
        }
        
        if (hasModular) {
            return false;
        } else if (!hasModular) {
            return true;
        }
        
        return false;
    }

    @Override
    public String getUnavailableReason() {
        return "Modular Fabricators already present";
    }

    @Override
    public boolean showWhenUnavailable() {
        return true;
    }
}
