package data.missions.shi_randomvsstock;

import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import data.missions.BaseRandomSRAMissionDefinition;

public class MissionDefinition extends BaseRandomSRAMissionDefinition {
    
    @Override
    public void defineMission(MissionDefinitionAPI api) {
        chooseFactions("shadow_industry", null);
        super.defineMission(api);
    }
}
