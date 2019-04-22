package data.missions.cad;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        /*Cease and Desist
        A cocky pirate lord in the very first Thresher takes on some bounty hunting chumps*/
        
        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Thresher and her escorts");
        api.setFleetTagline(FleetSide.ENEMY, "Bounty Hunters");

		// These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Destroy these fools.");
        api.addBriefingItem("The Thresher must survive.");

		// Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        api.addToFleet(FleetSide.PLAYER, "msp_thresher_Common", FleetMemberType.SHIP, "Thresher", true);
        api.addToFleet(FleetSide.PLAYER, "msp_potniaBis_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "msp_carmine_Common", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "msp_carmine_Common", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "msp_carmine_Raider", FleetMemberType.SHIP, "Clayton", false);
        api.addToFleet(FleetSide.PLAYER, "msp_southpaw_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "msp_southpaw_Balanced", FleetMemberType.SHIP, false);

		// Mark thresher as essential - losing it results
        // in mission failure. Could also be set on an enemy ship,
        // in which case destroying it would result in a win.
        api.defeatOnShipLoss("ISS Thresher");

		// Set up the enemy fleet.
        // It's got more ships than the player's, but they're not as strong.
        api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);

		//api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false).getCaptain().setPersonality("suicidal");
        //api.addToFleet(FleetSide.ENEMY, "hound_Assault", FleetMemberType.SHIP, false);
        //api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
		//api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
		// Set up the map.
        // 12000x8000 is actually somewhat small, making for a faster-paced mission.
        float width = 18000f;
        float height = 16000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

		// All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.55f, minY + height * 0.4f, 2000);
        api.addNebula(minX + width * 0.4f, minY + height * 0.55f, 1000);

		// And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

		// Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.
        api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "sensor_array");
        api.addObjective(minX + width * 0.6f, minY + height * 0.7f, "nav_buoy");
        api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "comm_relay");

		// Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of 
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        api.addAsteroidField(minY, minY, 30, 1300f, 25f, 50f, 70);

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(minX + width * 0.55f, minY + height * 0.25f, 200f, "cryovolcanic", 200f);
    }
}