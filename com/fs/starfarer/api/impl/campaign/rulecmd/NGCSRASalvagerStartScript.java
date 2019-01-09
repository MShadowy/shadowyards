package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class NGCSRASalvagerStartScript extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);

        data.addScript(new Script() {
            public void run() {
                FactionAPI player = Global.getSector().getFaction("player");
                
                player.setRelationship(Factions.PERSEAN,0.15f);
                player.setRelationship(Factions.INDEPENDENT,0.15f);
                player.setRelationship("shadow_industry",0.15f);
                
                player.setRelationship(Factions.HEGEMONY,-0.15f);
                
                player.setRelationship("junk_pirates",-0.5f);
                player.setRelationship("exipirated", -0.5f);
                player.setRelationship(Factions.LUDDIC_PATH,-0.5f);
                player.setRelationship(Factions.PIRATES,-0.5f);
                
                player.setRelationship(Factions.DERELICT,-1f);
                player.setRelationship(Factions.REMNANTS,-1f);
                player.setRelationship("nullorder",-1f);
                player.setRelationship("blade_breakers",-1f);
            }
        });
        return true;
    }
}
