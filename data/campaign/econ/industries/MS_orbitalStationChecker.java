package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import data.campaign.econ.MS_industries;

public class MS_orbitalStationChecker extends OrbitalStation {
    
    @Override
    public boolean isAvailableToBuild() {
        SectorAPI sector = Global.getSector();
        
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI shadow = sector.getFaction("shadow_industry");
        
        boolean canBuild = market.getPlanetEntity() != null &&
                (player.getRelationshipLevel(shadow).isAtWorst(RepLevel.WELCOMING) ||
                    Global.getSector().getPlayerFaction().knowsIndustry(getId()));
        
        return canBuild;
    }

    @Override
    public String getUnavailableReason() {
        return "Station type unavailable.";
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }
}
