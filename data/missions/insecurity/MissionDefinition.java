package data.missions.insecurity;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        /*Insecurity
        Early in the Askonia Crisis, the SRA is setting up Outpost Tiger when one 
        of the numerous factions vying for control after the disaster attacks 
        their incomplete outpost*/
        api.initFleet(FleetSide.PLAYER, "SYS", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

	
        api.setFleetTagline(FleetSide.PLAYER, "Shadowyards Security Taskforce");
        api.setFleetTagline(FleetSide.ENEMY, "Askonian Separatists");

		// These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Drive off the Separatist Raiders");
        api.addBriefingItem("The SYS Eyelasher must survive");

        //Both fleets get to be bigger
        
	//The SRA Security detail isn't quite as large as it probably should be
        //So it's reinforced with independent mercenaries
        //If at all possible it'd be nice to have them actually tied down defending the outpost
        api.addToFleet(FleetSide.PLAYER, "ms_clade_Standard", FleetMemberType.SHIP, "SYS Eyelasher", true);
        api.addToFleet(FleetSide.PLAYER, "ms_solidarity_Standard", FleetMemberType.SHIP, "ISS Lyre", true);
        api.addToFleet(FleetSide.PLAYER, "ms_solidarity_Fast", FleetMemberType.SHIP, "SYS Footpad", false);
        api.addToFleet(FleetSide.PLAYER, "ms_morningstar_AF", FleetMemberType.SHIP, "ISS Soyuz", false);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_Attack", FleetMemberType.SHIP, "SYS Clearfist", false);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_AF", FleetMemberType.SHIP, "SYS Tesla Coil", false);
        api.addToFleet(FleetSide.PLAYER, "ms_inanna_Strike", FleetMemberType.SHIP, "SYS Odd Side Out", false);
        api.addToFleet(FleetSide.PLAYER, "ms_seski_BR", FleetMemberType.SHIP, "SYS Blighter", false);
        api.addToFleet(FleetSide.PLAYER, "ms_ashnan_Fast", FleetMemberType.SHIP, "SYS Get Out", false);

        //api.defeatOnShipLoss("Station");

        //The Separatist fleet has a slightly larger number of ships; their quality/tech level is quite uneven
        api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "mule_d_pirates_Smuggler", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_d_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "cerberus_d_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "mudskipper2_Hellbore", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Standard", FleetMemberType.SHIP, false);

		// Set up the map.
        // 12000x8000 is actually somewhat small, making for a faster-paced mission.
        float width = 22000f;
        float height = 18000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

		// All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.6f, minY + height * 0.3f, 1400);
        api.addNebula(minX + width * 0.4f, minY + height * 0.7f, 1200);

		// And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }
        api.setBackgroundSpriteName("graphics/backgrounds/background4.jpg");

	api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "sensor_array");
        api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "nav_buoy");

	
        api.addAsteroidField(minY, minY, 45, 2000f, 20f, 70f, 100);

        //We're in Askonia, so make sure the stars match up
        api.addPlanet(minX + width * 0.2f, minY + height * 0.8f, 320f, "star_yellow", 300f);
        api.addPlanet(minX + width * 0.8f, minY + height * 0.8f, 256f, "desert", 250f);
        api.addPlanet(minX + width * 0.55f, minY + height * 0.25f, 200f, "cryovolcanic", 200f);
    }
}