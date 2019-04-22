package data.missions.between;

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
        /*Between Scylla and Charybdis
        ~10 years after Drums in the Deep; the first Scyllas are coming online
        At around this time, thwarted due to the SRA embracing radical transhumanism as
        a cure to the virus bomb, a massive Pather force launches one of several follow up raids
        which ends up breaking the back of their movement for bit*/
        api.initFleet(FleetSide.PLAYER, "SYS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Shadowyards Combat Evaluation Unit");
        api.setFleetTagline(FleetSide.ENEMY, "Pather Attack Fleet");

		// These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Drive off the enemy raiders.");

		// Set up the player's fleet.
        // the SRA team is a Combat Evaluation Unit testing out the new Scylla phase cruiser; 
        // the Just Leaving is the third ship produced and much is expected of the design after the success of the Shamash
        api.addToFleet(FleetSide.PLAYER, "ms_scylla_Standard", FleetMemberType.SHIP, "SyS Just Leaving", true); //17
        api.addToFleet(FleetSide.PLAYER, "ms_charybdis_Balanced", FleetMemberType.SHIP, "SYS Wanders Like Thought", false).getCaptain().setPersonality(Personalities.STEADY); //18
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_Standard", FleetMemberType.SHIP, "SYS Syd Real", false).getCaptain().setPersonality(Personalities.CAUTIOUS); //5
        api.addToFleet(FleetSide.PLAYER, "ms_seski_BR", FleetMemberType.SHIP, "SYS Sass Master", false).getCaptain().setPersonality(Personalities.STEADY); //4
        api.addToFleet(FleetSide.PLAYER, "ms_shamash_Attack", FleetMemberType.SHIP, "SYS Ball o' Fire", false).getCaptain().setPersonality(Personalities.AGGRESSIVE); //7

		// Set up the enemy fleet.
        // The Pather assault group; it's not quite clear what they're expecting to accomplish
        // but this does represent a very significant concentration of firepower for them
        api.addToFleet(FleetSide.ENEMY, "dominator_d_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "enforcer_d_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "enforcer_d_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Outdated", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);

		// Set up the map.
        // 12000x8000 is actually somewhat small, making for a faster-paced mission.
        float width = 20000f;
        float height = 18000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

		// All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add a big nebula cloud
        api.addNebula(minX + width * 0.65f, minY + height * 0.45f, 4000);

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
        api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "sensor_array");
        api.addObjective(minX + width * 0.6f, minY + height * 0.7f, "nav_buoy");
        api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "sensor_array");
        api.addObjective(minX + width * 0.4f, minY + height * 0.3f, "nav_buoy");

		// Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of 
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        api.addAsteroidField(minY, minY, 25, 3000f, 20f, 70f, 120);

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(minX + width * 0.55f, minY + height * 0.25f, 200f, "ice_giant", 200f);
        api.addPlanet(minX + width * 0.6f, minY + height * 0.3f, 75f, "lava", 200f);
    }
}