package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class NGCSRAPartisanStartScript extends BaseCommandPlugin {
    
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
        
        data.addScript(new Script() {
            @Override
            public void run() {
                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                
                boolean addCadre = false;
                
                FactionAPI shadow = Global.getSector().getFaction("shadow_industry");
                
                FactionAPI player = Global.getSector().getFaction("player");
                
                player.setRelationship("shadow_industry", 0.3f);
                player.setRelationship("kadur_remnant", 0.15f);
                player.setRelationship("pack", 0.15f);
                player.setRelationship("ORA", 0.15f);
                player.setRelationship("sylphon", 0.15f);
                player.setRelationship("dassault_mikoyan", 0.1f);
                player.setRelationship("SCY", 0.1f);
                player.setRelationship(Factions.PERSEAN, 0.1f);
                player.setRelationship(Factions.INDEPENDENT, 0.1f);
                
                player.setRelationship(Factions.DERELICT, -1f);
                player.setRelationship(Factions.REMNANTS, -1f);
                player.setRelationship("new_galactic_order", -1f);
                player.setRelationship("blade_breakers", -1f);
                player.setRelationship(Factions.LUDDIC_PATH, -0.75f);
                player.setRelationship("diableavionics", -0.65f);
                player.setRelationship("exigency", -0.65f);
                player.setRelationship("cabal", -0.65f);
                player.setRelationship(Factions.DIKTAT, -0.5f);
                player.setRelationship(Factions.HEGEMONY, -0.5f);
                player.setRelationship("exipirated", -0.65f);
                player.setRelationship("junk_pirates", -0.5f);
                player.setRelationship(Factions.PIRATES, -0.5f);
                player.setRelationship("blackrock_driveyards", -0.25f);
                player.setRelationship(Factions.LUDDIC_CHURCH, -0.2f);
                player.setRelationship("al_ars",-0.15f);
                player.setRelationship(Factions.TRITACHYON, -0.15f);
                
            }
        });
        return true;
    }
}
