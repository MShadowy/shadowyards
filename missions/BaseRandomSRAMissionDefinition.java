package data.missions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV2;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParams;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.Collections;
import java.util.Comparator;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class BaseRandomSRAMissionDefinition implements MissionDefinitionPlugin {
    public static final Comparator<FleetMemberAPI> PRIORITY = new Comparator<FleetMemberAPI>()
    {
        // -1 means member1 is first, 1 means member2 is first
        @Override
        public int compare(FleetMemberAPI member1, FleetMemberAPI member2)
        {
            if (member1.isFlagship())
            {
                if (!member2.isFlagship())
                {
                    return -1;
                }
            }
            else
            {
                if (member2.isFlagship())
                {
                    return 1;
                }
            }
            if (!member1.isCivilian())
            {
                if (member2.isCivilian())
                {
                    return -1;
                }
            }
            else
            {
                if (!member2.isCivilian())
                {
                    return 1;
                }
            }
            if (!member1.isFighterWing())
            {
                if (member2.isFighterWing())
                {
                    return -1;
                }
            }
            else
            {
                if (!member2.isFighterWing())
                {
                    return 1;
                }
            }
            if (member1.getFleetPointCost() > member2.getFleetPointCost())
            {
                return -1;
            }
            else if (member1.getFleetPointCost() < member2.getFleetPointCost())
            {
                return 1;
            }
            if (member1.getCaptain() != null)
            {
                if (member2.getCaptain() == null)
                {
                    return -1;
                }
            }
            else
            {
                if (member2.getCaptain() != null)
                {
                    return 1;
                }
            }
            if (!member1.isFrigate())
            {
                if (member2.isFrigate())
                {
                    return -1;
                }
            }
            else
            {
                if (!member2.isFrigate())
                {
                    return 1;
                }
            }
            if (!member1.isDestroyer())
            {
                if (member2.isDestroyer())
                {
                    return -1;
                }
            }
            else
            {
                if (!member2.isDestroyer())
                {
                    return 1;
                }
            }
            if (!member1.isCruiser())
            {
                if (member2.isCruiser())
                {
                    return -1;
                }
            }
            else
            {
                if (!member2.isCruiser())
                {
                    return 1;
                }
            }
            if (!member1.isCapital())
            {
                if (member2.isCapital())
                {
                    return -1;
                }
            }
            else
            {
                if (!member2.isCapital())
                {
                    return 1;
                }
            }
            return member1.getSpecId().compareTo(member2.getSpecId());
        }
    };
    // Types of objectives that may be randomly used
    private static final String[] OBJECTIVE_TYPES =
    {
        "sensor_array", "nav_buoy", "comm_relay"
    };
    private static final Map<String, Float> QUALITY_FACTORS = new HashMap<>(11);
    
    
    static
    {
        QUALITY_FACTORS.put("shadow_industry", 0.85f);
        QUALITY_FACTORS.put(Factions.DIKTAT, 0.5f);
        QUALITY_FACTORS.put(Factions.HEGEMONY, 0.5f);
        QUALITY_FACTORS.put(Factions.INDEPENDENT, 0.5f);
        QUALITY_FACTORS.put(Factions.LIONS_GUARD, 0.75f);
        QUALITY_FACTORS.put(Factions.LUDDIC_CHURCH, 0.25f);
        QUALITY_FACTORS.put(Factions.LUDDIC_PATH, 0f);
        QUALITY_FACTORS.put(Factions.PIRATES, 0f);
        QUALITY_FACTORS.put(Factions.TRITACHYON, 0.85f);
    }
    private FactionAPI enemy;
    private boolean flagshipChosen = false;
    private FactionAPI player;
    private final Random rand = new Random();
    
    @Override
    public void defineMission(MissionDefinitionAPI api) {
        if (player == null || enemy == null) {
            chooseFactions(null, null);
        }
        
        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true, 5);

        api.setFleetTagline(FleetSide.PLAYER, player.getDisplayNameLong() + " forces");
        api.setFleetTagline(FleetSide.ENEMY, enemy.getDisplayNameLong() + " forces");
        
        // Fleet size randomization
        int size = 5 + (int) ((float) Math.random() * 45);
        float difficulty = 0.7f + rand.nextFloat() * 0.3f;

        // Actual fleet generation call
        int playerFP = generateFleet(player, api, FleetSide.PLAYER, (int) (size * difficulty), QUALITY_FACTORS.get(player.getId()));
        int enemyFP = generateFleet(enemy, api, FleetSide.ENEMY, size, QUALITY_FACTORS.get(enemy.getId()));

        // Set up the map
        float width = 13000f + 13000f * (size / 40);
        float height = 13000f + 13000f * (size / 40);
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        int objectiveCount = (int) Math.floor(size / 8f);

        while (objectiveCount > 0)
        {
            String type = OBJECTIVE_TYPES[rand.nextInt(3)];

            if (objectiveCount == 1)
            {
                api.addObjective(0, 0, type);
                objectiveCount -= 1;
            }
            else
            {
                float theta = (float) (Math.random() * Math.PI);
                double radius = Math.min(width, height);
                radius = radius * 0.1 + radius * 0.3 * Math.random();
                int x = (int) (Math.cos(theta) * radius);
                int y = (int) (Math.sin(theta) * radius);
                api.addObjective(x, -y, type);
                api.addObjective(-x, y, type);
                objectiveCount -= 2;
            }
        }
        
        //Set up a random chance of SRA related planets/backgrounds is either faction is Shadowyards
        if (Math.random() < 0.5 && (player.getId().contentEquals("shadow_industry") || enemy.getId().contentEquals("shadow_industry")))
        {

            // The possible planet backgrounds
            String[] planets =
            {
                "planet_euripides"
            };
            String planet = planets[(int) (Math.random() * planets.length)];

            float radius = 300f;
            
            api.addPlanet(0, 0, radius, planet, 200f, true);
        }
        
        // Show the factions versus and their FP
        api.addBriefingItem(player.getDisplayName() + "  (" + playerFP + ")   vs.  " + enemy.getDisplayName() + "  (" + enemyFP + ")");

        // Chance of generating a nebula
        float nebulaChance = MathUtils.getRandomNumberInRange(0, 100);

        // So basically half the time (if less than 50 out of 100)
        if (nebulaChance < 50)
        {
            // Do regular nebula generation
            float nebulaCount = 10 + (float) Math.random() * 30;
            float nebulaSize = (float) Math.random();

            for (int i = 0; i < nebulaCount; i++)
            {
                float x = (float) Math.random() * width - width / 2;
                float y = (float) Math.random() * height - height / 2;
                float nebulaRadius = (400f + (float) Math.random() * 1600f) * nebulaSize;
                api.addNebula(x, y, nebulaRadius);
            }

            api.addBriefingItem("Nebulosity:  " + (int) (((nebulaCount * nebulaSize) / 40f) * 100) + "%");

        }
        else
        {
            // Mention that there is no nebula, this line could be commented out if you don't want this line item added
            api.addBriefingItem("Nebulosity: N/A");
        }

        // Asteroid generation random chance
        float asteroidChance = MathUtils.getRandomNumberInRange(0, 100);

        // If chance is less than 50
        if (asteroidChance < 50)
        {

            // Do regular asteroid generation
            // Minimum asteroid speed between 15 and 50
            int minAsteroidSpeed = MathUtils.getRandomNumberInRange(15, 50);

            // Asteroid count
            int asteroidCount = 5 * size + (int) (size * 20 * Math.pow(Math.random(), 2));

            // Add the asteroid field
            api.addAsteroidField(
                    minX + width * 0.5f, // X
                    minY + height * 0.5f, // Y
                    rand.nextInt(90) - 45 + (rand.nextInt() % 2) * 180, // Angle
                    width * 0.25f + ((float) Math.random() * height / 2), // Width
                    minAsteroidSpeed, // Min speed
                    minAsteroidSpeed * 1.1f, // Max speed
                    asteroidCount); // Count

            api.addBriefingItem("Asteroid Density:  " + (int) ((asteroidCount / 1000f) * 100) + "%");
            api.addBriefingItem("Asteroid Speed:  " + minAsteroidSpeed);
        }
        else
        {
            // If not asteroid field, specify as N/A, you can comment this out
            api.addBriefingItem("Asteroid Density: N/A");
            api.addBriefingItem("Asteroid Speed: N/A");
        }
    }

    protected void chooseFactions(String playerFactionId, String enemyFactionId)
    {
        player = Global.getSector().getFaction(playerFactionId);
        enemy = Global.getSector().getFaction(enemyFactionId);

        List<FactionAPI> acceptableFactions = new ArrayList<>(11);

        // Instead of adding *all* factions, we are adding the vanilla factions only
        acceptableFactions.add(Global.getSector().getFaction(Factions.DIKTAT));
        acceptableFactions.add(Global.getSector().getFaction(Factions.HEGEMONY));
        acceptableFactions.add(Global.getSector().getFaction(Factions.INDEPENDENT));
        acceptableFactions.add(Global.getSector().getFaction(Factions.LIONS_GUARD));
        acceptableFactions.add(Global.getSector().getFaction(Factions.LUDDIC_CHURCH));
        acceptableFactions.add(Global.getSector().getFaction(Factions.LUDDIC_PATH));
        acceptableFactions.add(Global.getSector().getFaction(Factions.PIRATES));
        acceptableFactions.add(Global.getSector().getFaction(Factions.TRITACHYON));        

        // If the player faction is not null, and is not specified a parameter in the input, choose from one of the acceptable factions
        player = player != null ? player : acceptableFactions.get(rand.nextInt(acceptableFactions.size()));

        acceptableFactions.remove(player);

        enemy = enemy != null ? enemy : acceptableFactions.get(rand.nextInt(acceptableFactions.size()));
    }

    // Generate a fleet from the campaign fleet generator
    int generateFleet(FactionAPI faction, MissionDefinitionAPI api, FleetSide side, int minFP, float qf)
    {
        MarketAPI market = Global.getFactory().createMarket(String.valueOf((new Random()).nextLong()), String.valueOf((new Random()).nextLong()), 4);
        market.setFactionId(faction.getId());
        market.setBaseSmugglingStabilityValue(0);
        CampaignFleetAPI fleet = FleetFactoryV2.createFleet(new FleetParams(
                null,
                market,
                market.getFactionId(),
                null, // fleet's faction, if different from above, which is also used for source market picking
                FleetTypes.PATROL_LARGE,
                minFP, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // civilianPts
                0f, // utilityPts
                0f, // qualityBonus
                qf, // qualityOverride
                1f, // officer num mult
                0 // officer level bonus
                ));

        List<FleetMemberAPI> fleetList = fleet.getFleetData().getMembersListCopy();
        Collections.sort(fleetList, PRIORITY);
        
        for (FleetMemberAPI m : fleetList)
        {
            String variant = m.isFighterWing() ? m.getSpecId() : m.getVariant().getHullVariantId();
            api.addToFleet(side, variant, m.getType(), m.getShipName(), (!m.isFighterWing() && !flagshipChosen));

            if (!m.isFighterWing() && !flagshipChosen)
            {
                flagshipChosen = true;
            }
        }

        return fleet.getFleetPoints();
    }
}
