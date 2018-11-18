package data.campaign.econ.conditions;

import com.fs.starfarer.api.impl.campaign.econ.WorldFarming;
import data.campaign.econ.MS_conditionData;

public class MS_worldSemiArid extends WorldFarming {
    
    public MS_worldSemiArid() {
        super(MS_conditionData.MS_WORLD_SEMI_ARID_FARMING_MULT, MS_conditionData.MS_WORLD_SEMI_ARID_MACHINERY_MULT);
    }

}
