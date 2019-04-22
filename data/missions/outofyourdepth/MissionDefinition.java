package data.missions.outofyourdepth;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        /*Out of your Depth
        League techminers en route to PL Space get ambushed by a sizeable Pather Force
        Time to run*/
        api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ESCAPE, false, 8);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "PLSS Urchin, escorting merchant convoy");
        api.setFleetTagline(FleetSide.ENEMY, "Suspected Luddic Path forces.");

        //Player Mission briefing bullet points.
        api.addBriefingItem("Evade Pather forces");
        api.addBriefingItem("At least 50% of the convoy must escape.");
        api.addBriefingItem("The ISS Urchin, ISS Iconoclat, and ISS Altair IV must survive");

        //First, the player side; a League convoy with light escort.
        api.addToFleet(FleetSide.PLAYER, "ms_elysium_L_PD", FleetMemberType.SHIP, "PLSS Urchin", true);
        api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, "PLSS Kestrel", false);
        api.addToFleet(FleetSide.PLAYER, "ms_sargasso_L_Support", FleetMemberType.SHIP, "PLSS Warden", false);
        api.addToFleet(FleetSide.PLAYER, "vigilance_FS", FleetMemberType.SHIP, "PLSS Peltast", false);
        api.addToFleet(FleetSide.PLAYER, "gemini_Standard", FleetMemberType.SHIP, "ISS Iconoclast", false);
        api.addToFleet(FleetSide.PLAYER, "buffalo_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "buffalo_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "buffalo_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "buffalo_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "colossus_Standard", FleetMemberType.SHIP, "ISS Altair IV", false);
        api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false);

        api.defeatOnShipLoss("PLSS Urchin");
        api.defeatOnShipLoss("ISS Iconoclast");
        api.defeatOnShipLoss("ISS Altair IV");

        // Pather jerks; there are many of them with some surprisingly heavy support
        api.addToFleet(FleetSide.ENEMY, "dominator_d_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "enforcer_d_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);

        float width = 16000f;
        float height = 24000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.addNebula(minX + width * 0.85f, minY + height * 0.6f, 2000);
        api.addNebula(minX + width * 0.4f, minY + height * 0.8f, 3000);
        api.addNebula(minX + width * 0.15f, minY + height * 0.3f, 1400);

        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(minX + width * 0.5f, minY + height * 0.8f, "nav_buoy");
        api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "comm_relay");
        api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "sensor_array");
        api.addObjective(minX + width * 0.5f, minY + height * 0.2f, "nav_buoy");

        api.addAsteroidField(minY, minY, 45, 2000f, 20f, 70f, 100);

        api.addPlanet(minX + width * 0.55f, minY + height * 0.25f, 200f, "cryovolcanic", 200f);
    }
}