package data.missions.stricken;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
		// Set up the fleets so we can add ships and fighter wings to them.
        /*Stricken
        Gigas, late 205; Lance Base nears completion
        A Hegemony Combat Patrol detects an SRA Strike fleet which has
        bypassed the outer patrols; should it reach the incomplete base they could wreak havoc*/
        api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "SYS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Hegemony Patrol");
        api.setFleetTagline(FleetSide.ENEMY, "SRA Raid Team");

		// These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Destroy or drive off the enemy");

	// The Hegemony PatCom
        // a well balanced little patrol <3
        api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, "HSS Pursuant", true);
        api.addToFleet(FleetSide.PLAYER, "condor_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "lasher_PD", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, false);
	
        // Numbers are about even but their ships should be a bit better on average
        api.addToFleet(FleetSide.ENEMY, "ms_morningstar_Assault", FleetMemberType.SHIP, "SYS Hypnotized", false);
        api.addToFleet(FleetSide.ENEMY, "ms_sargasso_Balanced", FleetMemberType.SHIP, "SYS Stand In", false);
        api.addToFleet(FleetSide.ENEMY, "ms_enlil_Attack", FleetMemberType.SHIP, "SYS Incongrous", false);
        api.addToFleet(FleetSide.ENEMY, "ms_enlil_Balanced", FleetMemberType.SHIP, "SYS Hob Knobber", false);
        api.addToFleet(FleetSide.ENEMY, "ms_inanna_Strike", FleetMemberType.SHIP, "SYS Severely Sarcastic", false);
        api.addToFleet(FleetSide.ENEMY, "ms_seski_BR", FleetMemberType.SHIP, "SYS Catch Me", false);
        api.addToFleet(FleetSide.ENEMY, "ms_seski_Attack", FleetMemberType.SHIP, "SYS Unhappenstance", false);

        float width = 22000f;
        float height = 18000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

		// All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.4f, minY + height * 0.3f, 2000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.7f, 1000);

		// And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }
        //it's in Gigas, so set up the BG
        api.setBackgroundSpriteName("graphics/backgrounds/velkabg.jpg");

	//add some objectives
        api.addObjective(minX + width * 0.8f, minY + height * 0.4f, "nav_buoy");
        api.addObjective(minX + width * 0.2f, minY + height * 0.6f, "nav_buoy");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "comm_relay");

        // We're near Kain, so put that in
        api.addPlanet(minX + width * 0.35f, minY + height * 0.45f, 200f, "frozen", 200f);
    }
}