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
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class NGCSRAMercStartScript extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
	/*data.addScriptBeforeTimePass(new Script() {
            public void run() {
		boolean merc = memory.getBoolean("$ngcSRAMercSelected");
		if (merc) {
                    Global.getSector().getMemoryWithoutUpdate().set("$spacerStart", true);
		}
            }
	});*/

        data.addScript(new Script() {
            public void run() {
                FactionAPI player = Global.getSector().getFaction("player");
                       
		boolean merc = memory.getBoolean("$ngcSRAMercSelected");
                
                player.setRelationship(Factions.INDEPENDENT,0.15f);
                
                player.setRelationship("junk_pirates",-0.5f);
                player.setRelationship("exipirated", -0.5f);
                player.setRelationship(Factions.LUDDIC_PATH,-0.5f);
                player.setRelationship(Factions.PIRATES,-0.5f);
                
                player.setRelationship(Factions.DERELICT,-1f);
                player.setRelationship(Factions.REMNANTS,-1f);
                player.setRelationship("nullorder",-1f);
                player.setRelationship("blade_breakers",-1f);
                
                if (merc) {
                    new SRAMercDebt();
                }
            }
        });
        return true;
    }
}
