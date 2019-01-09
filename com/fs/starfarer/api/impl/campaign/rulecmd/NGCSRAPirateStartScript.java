package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class NGCSRAPirateStartScript extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);

        data.addScript(new Script() {
            public void run() {
                
                SectorAPI sector = Global.getSector();
                
                FactionAPI player = Global.getSector().getFaction("player");
                
                player.setRelationship(Factions.PIRATES,0.15f);
                player.setRelationship("junk_pirates",0.15f);
                player.setRelationship("exipirated", 0.15f);

                player.setRelationship("diableavionics", -0.1f);
                player.setRelationship(Factions.LUDDIC_PATH, -0.1f);
                player.setRelationship("al_ars", -0.1f);

                player.setRelationship("syndicate_asp", -0.65f);
                player.setRelationship("dassault_mikoyan", -0.75f);
                player.setRelationship(Factions.HEGEMONY, -0.65f);
                player.setRelationship("kadur_remnant", -0.65f);
                player.setRelationship(Factions.LUDDIC_CHURCH, -0.65f);
                player.setRelationship("neutrinocorp", -0.65f);
                player.setRelationship("ORA", -0.7f);
                player.setRelationship("pack", -0.65f);
                player.setRelationship(Factions.PERSEAN, -0.65f);
                player.setRelationship("SCY", -0.7f);
                player.setRelationship("shadow_industry", -0.6f);
                player.setRelationship(Factions.DIKTAT, -0.65f);
                player.setRelationship("sylphon", -0.7f);
                player.setRelationship(Factions.TRITACHYON, -0.65f);
                player.setRelationship(Factions.INDEPENDENT, -0.65f);
                player.setRelationship("blackrock_driveyards", -0.65f);
                player.setRelationship("nomads", -0.65f);
                player.setRelationship(Factions.DERELICT, -1f);
                player.setRelationship(Factions.REMNANTS, -1f);
                player.setRelationship("gmda", -1f);
                player.setRelationship("nullorder", -1f);
                player.setRelationship("blade_breakers", -1f);
                player.setRelationship("interstellarimperium", -0.65f);
                player.setRelationship("HMI", -0.65f);
                player.setRelationship("fob", -0.65f);
                player.setRelationship("mayasura", -0.65f);
                player.setRelationship("exlane", -0.65f);

                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                CargoAPI cargo = fleet.getCargo();
                cargo.initPartialsIfNeeded();
                cargo.addCommodity(Commodities.DRUGS, 50);
            }
            
        });
        return true;
    }
}
