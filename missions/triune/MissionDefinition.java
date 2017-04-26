package data.missions.triune;

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
        api.initFleet(FleetSide.PLAYER, "SYS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ESCAPE, true);

                    // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Shadowyards Interception team");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Convoy");

                    // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Disable the HSS Idolator");

                    // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_Attack", FleetMemberType.SHIP, "SYS Leitmotif", true);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_PD", FleetMemberType.SHIP, "SYS Standoffish", false);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_AF", FleetMemberType.SHIP, "SYS Alder", false);

        //api.addToFleet(FleetSide.PLAYER, "ms_skinwalker_wing", FleetMemberType.FIGHTER_WING, false, CrewXPLevel.ELITE);
        //api.addToFleet(FleetSide.PLAYER, "ms_skinwalker_wing", FleetMemberType.FIGHTER_WING, false, CrewXPLevel.ELITE);

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
        api.defeatOnShipLoss("HSS Idolator");

                    // Set up the enemy fleet.
        // It's got more ships than the player's, but they're not as strong.
        api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, "HSS Idolator", false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false);
        api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);

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
        float width = 14000f;
        float height = 20000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

                    // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 1000);

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
        // Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of 
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        api.addAsteroidField(minY, minY, 45, 2000f, 20f, 70f, 100);

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(minX + width * 0.2f, minY + height * 0.8f, 320f, "star_yellow", 300f);
        api.addPlanet(minX + width * 0.8f, minY + height * 0.8f, 256f, "desert", 250f);
        api.addPlanet(minX + width * 0.55f, minY + height * 0.25f, 200f, "cryovolcanic", 200f);
    }
}
