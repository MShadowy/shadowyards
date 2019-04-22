package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

//a straightforward system that simply boosts shield efficiency and damage absorbtion
public class MS_DefenseMatrix extends BaseShipSystemScript {
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - .5f * effectLevel);
        stats.getShieldTurnRateMult().modifyMult(id, 1.2f * effectLevel);
        stats.getShieldUnfoldRateMult().modifyPercent(id, 200 * effectLevel);
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getShieldTurnRateMult().unmodify(id);
        stats.getShieldUnfoldRateMult().unmodify(id);
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
		return new StatusData("shield absorbs 5x damage", false);
	}
        
        return null;
    }
}
