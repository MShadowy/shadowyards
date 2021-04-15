package data.campaign.econ.industries.exerelin;

import data.campaign.econ.MS_industries;
import exerelin.world.ExerelinProcGen.ProcGenEntity;
import exerelin.world.industry.IndustryClassGen;

public class ms_redwingsExerelinCompat extends IndustryClassGen {
    
    public ms_redwingsExerelinCompat() {
        super(MS_industries.REDWINGS);
    }
    
    @Override
    public boolean canApply(ProcGenEntity entity) {
        if (!entity.isHQ && !entity.market.getFactionId().equals("shadow_industry"))
            return false;
        return super.canApply(entity);
    }
    
    @Override
    public float getWeight(ProcGenEntity entity) {
	return (1 + entity.market.getSize() / 5) * getFactionMult(entity);
    }
}
