package data.missions.bolivian;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
		// Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "SYS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Piratical Flotilla");
        api.setFleetTagline(FleetSide.ENEMY, "Shadowyards Special Operations Detachment");

		// These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("For a moment there, I thought we were in trouble.");

		// Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, "ISS Moratorium", true);
        api.addToFleet(FleetSide.PLAYER, "condor_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, "ISS Grinning Adder", false);
        api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "enforcer_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vigilance_FS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, false);

		//api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
		// Mark both ships as essential - losing either one results
        // in mission failure. Could also be set on an enemy ship,
        // in which case destroying it would result in a win.
		// Set up the enemy fleet.
        // This is a horrendously one sided mission.  And the player is on the wrong side.
        api.addToFleet(FleetSide.ENEMY, "ms_scylla_Standard", FleetMemberType.SHIP, "SYS AproPow", false);
        api.addToFleet(FleetSide.ENEMY, "ms_charybdis_Balanced", FleetMemberType.SHIP, "SYS Rainy Face", false);
        api.addToFleet(FleetSide.ENEMY, "ms_morningstar_Standard", FleetMemberType.SHIP, "SYS Unfriendly Skies", false);
        api.addToFleet(FleetSide.ENEMY, "ms_morningstar_AF", FleetMemberType.SHIP, "SYS Mystery Machine", false);
        api.addToFleet(FleetSide.ENEMY, "ms_sargasso_Balanced", FleetMemberType.SHIP, "SYS Space Hole", false);
        api.addToFleet(FleetSide.ENEMY, "ms_enlil_Attack", FleetMemberType.SHIP, "SYS Little Mac", false);
        api.addToFleet(FleetSide.ENEMY, "ms_enlil_Attack", FleetMemberType.SHIP, "SYS Fire For Effect", false);
        api.addToFleet(FleetSide.ENEMY, "ms_enlil_Standard", FleetMemberType.SHIP, "SYS Rage Like Embers", false);
        api.addToFleet(FleetSide.ENEMY, "ms_seski_BR", FleetMemberType.SHIP, "SYS Going for a Ride", false);
        api.addToFleet(FleetSide.ENEMY, "ms_seski_Standard", FleetMemberType.SHIP, "SYS Slippery Devil", false);
        api.addToFleet(FleetSide.ENEMY, "ms_inanna_Standard", FleetMemberType.SHIP, "SYS Heel To!", false);
        api.addToFleet(FleetSide.ENEMY, "ms_inanna_Assault", FleetMemberType.SHIP, "SYS Shoot Some More", false);
        api.addToFleet(FleetSide.ENEMY, "ms_shamash_Attack", FleetMemberType.SHIP, "SYS Unvisibo", false);

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
        float width = 16000f;
        float height = 24000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

		// All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.65f, minY + height * 0.35f, 1800);
        api.addNebula(minX + width * 0.35f, minY + height * 0.65f, 1200);

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
        api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "nav_buoy");
        api.addObjective(minX + width * 0.5f, minY + height * 0.6f, "comm_relay");
        api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "sensor_array");
        api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "nav_buoy");

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(minX + width * 0.55f, minY + height * 0.25f, 200f, "cryovolcanic", 200f);
    }
}
