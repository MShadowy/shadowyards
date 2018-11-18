package data.campaign.econ.conditions;

import com.fs.starfarer.api.impl.campaign.econ.WorldFarming;
import data.campaign.econ.MS_conditionData;

public class MS_worldPartTerranPartRad extends WorldFarming {
    
    public MS_worldPartTerranPartRad() {
        super(MS_conditionData.MS_WORLD_PARTIAL_TERRAN_FARMING_MULT, MS_conditionData.MS_WORLD_PARTIAL_TERRAN_MACHINERY_MULT);
    }
    
}
