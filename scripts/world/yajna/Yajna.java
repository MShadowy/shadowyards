package data.scripts.world.yajna;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.world.AddMarketplace;
import data.scripts.world.MS_Conditions;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;


public class Yajna {
    
    public void generate (SectorAPI sector) {
        
        StarSystemAPI system = sector.createStarSystem("Yajna");
        system.getLocation().set(-3600, -200);
        LocationAPI hyper = Global.getSector().getHyperspace();
             
        system.setBackgroundTextureFilename("graphics/backgrounds/yajnabg.jpg");
        
        PlanetAPI yaj = system.initStar("Yajna", "star_orange", 600f, 400f, 17f, 2f, 3.5f);
        system.setLightColor(new Color(255, 230, 220));
        system.addRingBand(yaj, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 800, 40f, null, null);
        
        //Udgatri is a moonless gas giant which probably devoured all the inner system mass when the system formed
        PlanetAPI yaj1 = system.addPlanet("udgatri", yaj, "Udgatri", "ice_giant", 20, 500, 2100, 120);
        //Around Udgatri is Pillager Point, a large and successful Pirate haven, complete with Pirate "Military" base(!) and freeport
        SectorEntityToken pillager = system.addCustomEntity("pillager", "Pillager Point", "station_side06", "pirates");
        pillager.setCircularOrbitPointingDown(yaj1, 55, 800, 100);
        pillager.setCustomDescriptionId("pillager_point");
        
        AddMarketplace.addMarketplace("pirates",
                pillager,
                null,
                "Pillager Point",
                5,
                new ArrayList<>(Arrays.asList(Conditions.ORBITAL_STATION, Conditions.MILITARY_BASE, Conditions.VOLATILES_COMPLEX,
                    Conditions.HYDROPONICS_COMPLEX, Conditions.ORGANIZED_CRIME, Conditions.FREE_PORT, Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY)),
                0.3f
        );
        
        //Adhvaryu is a tiny rocky planetoid made from whatever Udgatri didn't eat
        //It's moon, Saptapadi, is almost as big as it is
        PlanetAPI yaj2 = system.addPlanet("adhvaryu", yaj, "Adhvaryu", "barren_castiron", 175, 28, 3000, 144);
        PlanetAPI yaj2a = system.addPlanet("saptapadi", yaj2, "Saptapadi", "barren_castiron", 35, 18, 200, 22);
        
        //Jnana is the SRA's major port in system and home to a major military base positioned to ward off Hegemony attack
        PlanetAPI jnana = system.addPlanet("jnana", yaj, "Jnana", "desert1", 218, 85, 4800, 270);
        PlanetAPI yaj3a = system.addPlanet("kanda", jnana, "Kanda", "barren3", 235, 30, 500, 90);
        jnana.setCustomDescriptionId("planet_jnana");
        
        AddMarketplace.addMarketplace("shadow_industry",
                jnana,
                null,
                "Jnana",
                5,
                new ArrayList<>(Arrays.asList(Conditions.DESERT, Conditions.INIMICAL_BIOSPHERE, Conditions.HABITABLE, Conditions.MILITARY_BASE, MS_Conditions.MODULARFAB, Conditions.SPACEPORT,
                    Conditions.HYDROPONICS_COMPLEX, Conditions.ORE_REFINING_COMPLEX, Conditions.ANTIMATTER_FUEL_PRODUCTION, Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY)),
                0.3f
        );
        
        //Hotri is a undeveloped but potentially very rich world which both the Leeague and SRA have been unsuccesfully courting--the locals are having none of that
        PlanetAPI hotri = system.addPlanet("hotri", yaj, "Hotri", "terran-eccentric", 110, 90, 5500, 295);
        SectorEntityToken hotri_port = system.addCustomEntity("hotri_port", "Hotri Port", "station_sporeship_derelict", "independent");
        hotri_port.setCircularOrbitPointingDown(hotri, 60, 400, 100);
        hotri.setCustomDescriptionId("planet_hotri");
        hotri_port.setCustomDescriptionId("hotri_port");
        
        AddMarketplace.addMarketplace("independent",
                hotri,
                new ArrayList<>(Arrays.asList((SectorEntityToken) hotri_port)),
                "Hotri",
                4,
                new ArrayList<>(Arrays.asList(Conditions.ARID, Conditions.MILD_CLIMATE, Conditions.HABITABLE, Conditions.ORBITAL_STATION, Conditions.FARMLAND_RICH, 
                        Conditions.RARE_ORE_ABUNDANT, Conditions.ORE_MODERATE, Conditions.COTTAGE_INDUSTRY, Conditions.RUINS_SCATTERED, Conditions.RURAL_POLITY, Conditions.POPULATION_4)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f
        );
        
        //Brahmin is a large and vigorously unpleasant radioactive world with an aggressively corrosive atmosphere and a warning beacon
        PlanetAPI yaj5 = system.addPlanet("brahmin", yaj, "Brahmin", "irradiated", 155, 110, 7000, 680);
        
        SectorEntityToken yaj5_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldTerrainPlugin.MagneticFieldParams(yaj5.getRadius() + 300f,
                        (yaj5.getRadius() + 250f) / 2f,
                        yaj5,
                        yaj5.getRadius() + 50f,
                        yaj5.getRadius() + 50f + 300f,
                        new Color(100, 20, 50, 40), // base color
                        0.7f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(235, 100, 140),
                        new Color(210, 110, 180),
			new Color(140, 190, 150),
			new Color(140, 190, 210),
			new Color(170, 200, 90), 
			new Color(65, 230, 160),
			new Color(70, 220, 20)
                ));
        yaj5_field.setCircularOrbit(yaj5, 0, 0, 100);
        
        //Thick rings bisecting the system
        system.addRingBand(yaj, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 7700, 40f, null, null);
        system.addRingBand(yaj, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 7900, 40f, null, null);
        system.addRingBand(yaj, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 8100, 40f, null, null);
        system.addAsteroidBelt(yaj, 600, 7900, 1400, 600, 400, Terrain.ASTEROID_BELT, "Center Belt");
        
        //Mantra is a large Persean controlled colony; while difficult to inhabit it has good access to volatiles and some organics, indicating it once housed a primitive biosphere
        PlanetAPI mantra = system.addPlanet("mantra", yaj, "Mantra", "toxic_cold", 70, 100, 8500, 470);
        PlanetAPI yaj6a = system.addPlanet("soma", mantra, "Soma", "frozen", 190, 35, 450, 100);
        PlanetAPI yaj6b = system.addPlanet("havir", mantra, "Havir", "frozen3", 70, 15, 700, 60);
        SectorEntityToken mandala_station = system.addCustomEntity("mandala_station", "Mandala Station", "station_side03", "persean");
        mandala_station.setCircularOrbitPointingDown(mantra, 75, 400, 100);
        mantra.setCustomDescriptionId("planet_mantra");
        
        AddMarketplace.addMarketplace("persean",
                mantra,
                new ArrayList<>(Arrays.asList((SectorEntityToken) mandala_station)),
                "Mantra",
                6,
                new ArrayList<>(Arrays.asList(Conditions.TOXIC_ATMOSPHERE, Conditions.COLD, Conditions.ORBITAL_STATION, Conditions.ORGANICS_TRACE, Conditions.VOLATILES_DIFFUSE,
                    Conditions.VOLATILES_COMPLEX, Conditions.LIGHT_INDUSTRIAL_COMPLEX, Conditions.POPULATION_6)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f
        );
        
        //Karma is a small mining outpost under SRA control
        PlanetAPI karma = system.addPlanet("karma", yaj, "Karma", "rocky_unstable", 95, 60, 9750, 1180);
        karma.setCustomDescriptionId("planet_karma");
        
        AddMarketplace.addMarketplace("shadow_industry",
                karma,
                null,
                "Karma",
                4,
                new ArrayList<>(Arrays.asList(Conditions.THIN_ATMOSPHERE, Conditions.COLD, Conditions.RARE_ORE_SPARSE, Conditions.ORE_MODERATE,
                    Conditions.ORE_COMPLEX, Conditions.HYDROPONICS_COMPLEX, Conditions.SPACEPORT, Conditions.POPULATION_4)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f
        );
        
        //Lastly, Maks Hole, named so as to mock the eponymous Mak, is a dinky pirate waystation hiding out in the nebula
        SectorEntityToken maks_hole = system.addCustomEntity("maks_hole", "Maks Hole", "station_pirate_type", "pirates");
        maks_hole.setCircularOrbitPointingDown(yaj, 115, 12500, 1270);
        maks_hole.setCustomDescriptionId("maks_hole");
        
        AddMarketplace.addMarketplace("pirates",
                maks_hole,
                null,
                "Maks Hole",
                3,
                new ArrayList<>(Arrays.asList(Conditions.ORBITAL_STATION, Conditions.FRONTIER, Conditions.VICE_DEMAND,
                    Conditions.FREE_PORT, Conditions.POPULATION_3)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY)),
                0.3f
        );
        
        SectorEntityToken relay = system.addCustomEntity("yajna_relay", // unique id
		"Yajna Relay", // name - if null, defaultName from custom_entities.json will be used
		"comm_relay", // type of object, defined in custom_entities.json
		"shadow_industry"); // faction
	relay.setCircularOrbit(system.getEntityById("yajna"), 218+60, 4800, 270);
        
        SectorEntityToken v = system.getEntityById("yajna");
	JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("yajna_gate", "Yajna Gate");
	jumpPoint.setCircularOrbit(v, 218-60, 4800, 270);
	jumpPoint.setRelatedPlanet(jnana);
        
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);
        
        StarSystemGenerator.addSystemwideNebula(system, StarAge.YOUNG);

        system.autogenerateHyperspaceJumpPoints(true, true);
        
        cleanup(system);
    }
    
    void cleanup(StarSystemAPI system){
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
	NebulaEditor editor = new NebulaEditor(plugin);        
        float minRadius = plugin.getTileSize() * 2f;
        
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);	     
//        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, system.getMaxRadiusInHyperspace()*1.25f, 0, 360, 0.25f);
    }
}
