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
        // Triune - 3 Enlils run down fleeing Hegemony operatives carrying stolen research data
        api.initFleet(FleetSide.PLAYER, "SYS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ESCAPE, true);

        api.setFleetTagline(FleetSide.PLAYER, "Shadowyards Interception team");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Convoy");

        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Disable the HSS Idolator");

                    // Set up the player's fleet.  Variant names come from the
        //The 3 Intercepts, 2 Enlils and a Shamash
        api.addToFleet(FleetSide.PLAYER, "ms_shamash_Attack", FleetMemberType.SHIP, "SYS Leitmotif", true);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_PD", FleetMemberType.SHIP, "SYS Standoffish", false);
        api.addToFleet(FleetSide.PLAYER, "ms_enlil_AF", FleetMemberType.SHIP, "SYS Alder", false);
        //And one of the scouts
        api.addToFleet(FleetSide.PLAYER, "ms_belet-seri_FS", FleetMemberType.SHIP, "SYS Electric Eye", false);
        
        api.defeatOnShipLoss("HSS Idolator");

        //vs Lashers, a hound and the Mule they're attempting to disable
        api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, "HSS Idolator", false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);

        
        float width = 14000f;
        float height = 20000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

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
        //we'll also use Yajna's background image
        api.setBackgroundSpriteName("graphics/backgrounds/yajnabg.jpg");

        // Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of 
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        api.addAsteroidField(minY, minY, 45, 2000f, 20f, 70f, 100);

        // Add some planets.  These are defined in data/config/planets.json.
        //Since this takes place in Yajna we'll use Yajna and Karma
        api.addPlanet(minX + width * 0.2f, minY + height * 0.8f, 60f, "star_orange", 300f);
        api.addPlanet(minX + width * 0.4f, minY + height * 0.6f, 200f, "rocky_unstable", 250f);
        
    }
}