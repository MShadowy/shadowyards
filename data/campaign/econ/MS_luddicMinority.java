package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import java.util.Arrays;

public class MS_luddicMinority extends BaseMarketConditionPlugin {
    
    private static final String [] luddicFactions = new String [] {
		"knights_of_ludd",
		"luddic_church",
		"luddic_path",
	};
    @Override
    public void apply(String id) {
        if (Arrays.asList(luddicFactions).contains(market.getFactionId())) {
            market.getStability().modifyFlat(id, MS_conditionData.MS_LUDDIC_MINORITY_RULEDBYLUDDIC_STABILITY, "Luddic minority");
        } else {
            market.getStability().modifyFlat(id, MS_conditionData.MS_LUDDIC_MINORITY_RULEDBYOTHER_STABILITY, "Luddic minority");
        }
        
        market.getDemand(Commodities.LUXURY_GOODS).getDemand().modifyMult(id, MS_conditionData.MS_LUDDIC_MINORITY_LUXURY_MULT);
    }
    
    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);
        
        market.getDemand(Commodities.LUXURY_GOODS).getDemand().unmodify(id);
    }
}
