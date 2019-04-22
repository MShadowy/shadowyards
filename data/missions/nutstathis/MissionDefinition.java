package data.missions.nutstathis;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
		// Set up the fleets so we can add ships and fighter wings to them.
        /*Nuts to This
        An SRA Techmining expedition returning from the outer reaches is ambushed by a
        plausibly deniable Tri-Tachyon mercenary group*/
        api.initFleet(FleetSide.PLAYER, "SYS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Shadowyards Techmining Detail");
        api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon Mercenaries");

		// These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat all enemy forces");

		// Set up the player's fleet.
        // An SRA techmining detail returning from the the black
        api.addToFleet(FleetSide.PLAYER, "ms_minos_standard", FleetMemberType.SHIP, "SYS Silius", true);
        api.addToFleet(FleetSide.PLAYER, "ms_solidarity_Fast", FleetMemberType.SHIP, "SYS Stunt Double", false);
        api.addToFleet(FleetSide.PLAYER, "ms_lambent_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_Standard", FleetMemberType.SHIP, "SYS Traveler", false);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_AF", FleetMemberType.SHIP, "SYS Bad Star", false);
        api.addToFleet(FleetSide.PLAYER, "ms_seski_Standard", FleetMemberType.SHIP, "SYS Off White", false);
        
		// Set up the enemy fleet.
        // TriTachyon funded mercenaries, they're looking for something
        api.addToFleet(FleetSide.ENEMY, "medusa_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brawler_tritachyon_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);

        float width = 24000f;
        float height = 20000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

		// All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.45f, minY + height * 0.65f, 2000);
        api.addNebula(minX + width * 0.55f, minY + height * 0.35f, 1000);

		// And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "nav_buoy");
        api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "nav_buoy");

	
        api.addPlanet(minX + width * 0.55f, minY + height * 0.25f, 200f, "cryovolcanic", 200f);
    }
}