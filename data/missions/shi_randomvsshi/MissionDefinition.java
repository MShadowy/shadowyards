package data.missions.shi_randomvsshi;

import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import data.missions.BaseRandomSRAMissionDefinition;

public class MissionDefinition extends BaseRandomSRAMissionDefinition {
    
    @Override
    public void defineMission(MissionDefinitionAPI api) {
        chooseFactions(null, "shadow_industry");
        super.defineMission(api);
    }
}
