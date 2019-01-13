package data.missions.theTest;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    
    @Override
    public void defineMission(MissionDefinitionAPI api) {
        
        api.initFleet(FleetSide.PLAYER, "SYS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);
        
        api.setFleetTagline(FleetSide.PLAYER, "Killer Banannas");
        api.setFleetTagline(FleetSide.ENEMY, "Wolves Needing Potassium");
        
        api.addBriefingItem("Nom Nom Nom");
        
        api.addToFleet(FleetSide.PLAYER, "ms_inanna_Standard", FleetMemberType.SHIP, "SYS Leitmotif", true);
        api.addToFleet(FleetSide.PLAYER, "ms_inanna_Strike", FleetMemberType.SHIP, "SYS Standoffish", false);
        api.addToFleet(FleetSide.PLAYER, "ms_inanna_CS", FleetMemberType.SHIP, "SYS Alder", false);
        
        api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_PD", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false);
        
        float width = 14000f;
        float height = 20000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);
    }
    
}
