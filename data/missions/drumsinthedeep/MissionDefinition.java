package data.missions.drumsinthedeep;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
		// Set up the fleets so we can add ships and fighter wings to them.
        /*Drums in the Deep; Pather Radicals launch a virus bomb attack on Euripides
        a few years after their success at Mayasura and some failed attempts to kill Tri-Tachyon*/
        
        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "SYS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Pather Crusaders");
        api.setFleetTagline(FleetSide.ENEMY, "SRA Patrol Group");

		// These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Overcome the Shadowyards Heretics");
        api.addBriefingItem("The Verdance carries our intrument of divine punishment; do not allow it to be destroyed");
        api.addBriefingItem("Carry out your holy mission");

	//The pather genociders carrying the virus bomb
        //Your ships a pretty numerous, but not that much more numerous
        //General force inferiority will probably see you take heavy casualties but should be winnable
        api.addToFleet(FleetSide.PLAYER, "brawler_pather_Raider", FleetMemberType.SHIP, "Verdance", true);
        api.addToFleet(FleetSide.PLAYER, "brawler_pather_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.PLAYER, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.PLAYER, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.PLAYER, "cerberus_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.PLAYER, "cerberus_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.PLAYER, "cerberus_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.PLAYER, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.PLAYER, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.PLAYER, "hound_luddic_path_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        
        api.defeatOnShipLoss("Verdance");
        //The SRA patrol group; their ships are broadly superior and they have destroyers
        //More than that, they have fighter cover, and you don't
        //But there are fewer of them overall; you'll have to work fast to overwhelm them and break through
        api.addToFleet(FleetSide.ENEMY, "ms_morningstar_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "ms_morningstar_PD", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "ms_shamash_EMP", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "ms_inanna_CS", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.CAUTIOUS);
        api.addToFleet(FleetSide.ENEMY, "ms_enlil_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "ms_enlil_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "ms_sargasso_Balanced", FleetMemberType.SHIP, false);
        
        float width = 24000f;
        float height = 20000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

		// All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 1000);
        api.addNebula(minX + width * 0.5f, minY + height * 0.75f, 4000);

		// And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }
        //We're in hyperspace, just outside of Anar so set the bg to hyperspace
        api.setBackgroundSpriteName("graphics/backgrounds/hyperspace_bg_cool.jpg");
        api.setHyperspaceMode(true);

		// Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
        api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "nav_buoy");
        api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "nav_buoy");

		// Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of 
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        // Add some planets.  These are defined in data/config/planets.json.
        //throw in three hyperspace jump nodes
        api.addPlanet(minX + width * 0.4f, minY + height * 0.3f, 150f, "star_yellow", 40f); //Anar
        api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 400f, "planet_euripides", 100f); //Euripides
        api.addPlanet(minX + width * 0.7f, minY + height * 0.7f, 100f, "ice_giant", 60f); //Calleach
    }
}