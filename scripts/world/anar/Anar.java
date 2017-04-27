package data.scripts.world.anar;

import data.scripts.world.MS_Conditions;
import data.scripts.world.AddMarketplace;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import java.awt.Color;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.Arrays;

public class Anar {
    
    public void generate(SectorAPI sector) {
        
        StarSystemAPI system = sector.createStarSystem("Anar");
        system.getLocation().set(-9000, 3400);
	LocationAPI hyper = Global.getSector().getHyperspace();
        
        system.setBackgroundTextureFilename("graphics/backgrounds/anarbg.jpg");
        
        PlanetAPI anar = system.initStar("anar", "star_yellow", 600f, 350f, 11f, 1.3f, 2f);//initStar("anar", "star_yellow", 600f);

        PlanetAPI anar1 = system.addPlanet("lambence", anar, "Lambence", "barren", 155, 55, 1200, 53);
        anar1.setFaction("shadow_industry");
        anar1.setCustomDescriptionId("planet_lambence");
        
        AddMarketplace.addMarketplace("shadow_industry",
                anar1,
                null,
                "Lambence",
                4,
                new ArrayList<>(Arrays.asList(Conditions.NO_ATMOSPHERE, Conditions.HOT, MS_Conditions.MEGA_SOLAR_ARRAYS, Conditions.POPULATION_4, Conditions.LOW_GRAVITY)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f
        );
        
        PlanetAPI anar2 = system.addPlanet("wallow", anar, "Wallow", "toxic", 20, 180, 2500, 222);
        
        PlanetAPI euripides = system.addPlanet("euripides", anar, "Euripides", "planet_euripides", 245, 160, 3900, 381);
        PlanetAPI anar3a = system.addPlanet("aeschylus", euripides, "Aeschylus", "cryovolcanic", 235, 40, 500, 62);
        system.addRingBand(euripides, "misc", "rings_dust0", 256f, 1, Color.white, 256, 550, 40f);
        system.addRingBand(euripides, "misc", "rings_dust0", 256f, 1, Color.white, 256, 650, 60f);
        system.addRingBand(euripides, "ringsMod", "dusty", 1024f, 2, Color.white, 1024f, 700, 80f);
        euripides.setCustomDescriptionId("planet_euripides");
        anar3a.setCustomDescriptionId("planet_aeschylus");
        euripides.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "euripides"));
        euripides.getSpec().setGlowColor(new Color(255,255,255,255));
	euripides.getSpec().setUseReverseLightForGlow(true);
        euripides.applySpecChanges();
        euripides.setFaction("shadow_industry");
        euripides.setInteractionImage("illustrations", "nanshe_desert");
        
        SectorEntityToken station_pranaVayu = system.addCustomEntity("prana_vayu", "Prana Vayu Shipyards", "station_shi_prana", "shadow_industry");
        station_pranaVayu.setCircularOrbitPointingDown(system.getEntityById("euripides"), 45, 400, 50);
        station_pranaVayu.setCustomDescriptionId("station_prana");
        
        AddMarketplace.addMarketplace("shadow_industry",
                euripides,
                new ArrayList<>(Arrays.asList((SectorEntityToken) station_pranaVayu)),
                "Euripides",
                8,
                new ArrayList<>(Arrays.asList(MS_Conditions.SEMI_ARID, Conditions.HABITABLE, Conditions.FARMLAND_ADEQUATE, Conditions.ORE_MODERATE, 
                        Conditions.RARE_ORE_SPARSE, Conditions.ORBITAL_STATION, Conditions.SPACEPORT, Conditions.ORE_COMPLEX, Conditions.ORE_REFINING_COMPLEX,
                    Conditions.ORE_REFINING_COMPLEX, Conditions.ORE_REFINING_COMPLEX, Conditions.LIGHT_INDUSTRIAL_COMPLEX, MS_Conditions.MODULARFAB, MS_Conditions.MODULARFAB, 
                        MS_Conditions.MODULARFAB, MS_Conditions.MEDCENTER, Conditions.REGIONAL_CAPITAL, Conditions.POPULATION_8)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f
        );
        
        SectorEntityToken relay = system.addCustomEntity("anar_relay", // unique id
		"Anar Relay", // name - if null, defaultName from custom_entities.json will be used
		"comm_relay", // type of object, defined in custom_entities.json
		"shadow_industry"); // faction
	relay.setCircularOrbit( system.getEntityById("anar"), 240+60, 3750, 381);
        
        SectorEntityToken neb1 = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
                "   xx" +
                " x x " +
                " xxx " +
                "xx   " +
                "x xx " +
                "  xxx" +
                "   x ",        
                5,7,
                "terrain", "nebula_anar", 4, 4, "Anar Drifts"));
        neb1.getLocation().set(euripides.getLocation().x + 1800f, euripides.getLocation().y + 300f);
        neb1.setCircularOrbit(anar, 160f, 5200, 520);
        
        SectorEntityToken neb2 = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
                "    xxx   " +
                " xxx xxxx " +
                "   xxxxxxx" +
                "xxxx xxx  " +
                "x       xx",        
                10,5,
                "terrain", "nebula_anar", 4, 4, "Anar Drifts"));
        neb2.getLocation().set(euripides.getLocation().x + 1800f, euripides.getLocation().y + 300f);
        neb2.setCircularOrbit(anar, 250f, 10400, 520);
        
        PlanetAPI anar4 = system.addPlanet("calleach", anar, "Calleach", "ice_giant", 235, 300, 12000, 766);
        anar4.setCustomDescriptionId("planet_calleach");
        system.addRingBand(anar4, "ringsMod", "dusty", 1024f, 1, Color.white, 1024f, 1650, 40f);
        system.addRingBand(anar4, "ringsMod", "rocky", 1024f, 1, Color.white, 1024f, 1750, 60f);
        system.addRingBand(anar4, "misc", "rings_ice0", 256f, 2, Color.white, 128f, 1750, 80f);
        system.addRingBand(anar4, "misc", "rings_dust0", 256f, 1, Color.white, 128f, 1800, 120f);
        PlanetAPI anar4a = system.addPlanet("cinderbox", anar4, "Cinderbox", "lava", 300, 60, 700, 88);
        anar4a.setCustomDescriptionId("planet_cinderbox");
        PlanetAPI theramin = system.addPlanet("theramin", anar4, "Theramin", "terran", 240, 120, 1200, 246);
        theramin.setCustomDescriptionId("planet_theramin");
        theramin.setFaction("shadow_industry");
        PlanetAPI melancholia = system.addPlanet("melancholia", anar4, "Melancholia", "cryovolcanic", 200, 80, 2100, 492);
        melancholia.setCustomDescriptionId("planet_melancholia");
        melancholia.setFaction("independent");
        
        SectorEntityToken station_gravitas = system.addCustomEntity("gravitas", "Gravitas Research Post", "station_shi_med", "shadow_industry");
        station_gravitas.setCircularOrbitPointingDown(system.getEntityById("theramin"), 45, 300, 50);
        
        SectorEntityToken calleach_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(anar4.getRadius() + 300f,
                        (anar4.getRadius() + 300f) / 2f,
                        anar4,
                        anar4.getRadius() + 50f,
                        anar4.getRadius() + 50f + 300f,
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
        calleach_field.setCircularOrbit(anar4, 0, 0, 100);
        
        AddMarketplace.addMarketplace("shadow_industry",
                theramin,
                new ArrayList<>(Arrays.asList((SectorEntityToken) station_gravitas)),
                "Theramin",
                6,
                new ArrayList<>(Arrays.asList(MS_Conditions.IRRADIATED_TERRAN, Conditions.HABITABLE, Conditions.EXTREME_WEATHER, Conditions.ORBITAL_STATION, Conditions.MILITARY_BASE, Conditions.HEADQUARTERS, Conditions.ORGANICS_COMPLEX,
                    Conditions.FARMLAND_RICH, Conditions.SHIPBREAKING_CENTER, MS_Conditions.ABSORBERS, Conditions.VOLATILES_DEPOT, Conditions.POPULATION_6)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY)),
                0.3f
        );
        
        AddMarketplace.addMarketplace(Factions.INDEPENDENT,
                melancholia,
                null,
                "Melancholia",
                3,
                new ArrayList<>(Arrays.asList(Conditions.ICE, Conditions.THIN_ATMOSPHERE, Conditions.FRONTIER, Conditions.VOLATILES_COMPLEX, Conditions.VICE_DEMAND, Conditions.POPULATION_3)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)), 
                0.3f
        );
        
        system.addAsteroidBelt(anar, 600, 18000, 1400, 600, 400, Terrain.ASTEROID_BELT, "Dusk Line");
        
        system.addRingBand(anar, "ringsMod", "dusty", 512f, 3, Color.white, 512f, 20000, 260);
        system.addRingBand(anar, "ringsMod", "rocky", 512f, 3, Color.white, 512f, 20000, 260);
        system.addAsteroidBelt(anar, 200, 14000, 1400, 200, 300, Terrain.ASTEROID_BELT, "Ergan Belt");
        
        //add hidden pirate base here
        SectorEntityToken berins_stash = system.addCustomEntity("berins_stash", "Berins Stash", "station_shi_hiddenPirateBase", "pirates");
        berins_stash.setCircularOrbitPointingDown(system.getEntityById("anar"), 72, 20100, 260);
        berins_stash.setCustomDescriptionId("berins_stash");
        
        AddMarketplace.addMarketplace("pirates",
                berins_stash,
                null,
                "Berins Stash",
                3,
                new ArrayList<>(Arrays.asList(Conditions.FREE_PORT, Conditions.ORBITAL_STATION, Conditions.OUTPOST, Conditions.VICE_DEMAND, Conditions.ORGANIZED_CRIME,
                        Conditions.HYDROPONICS_COMPLEX, Conditions.POPULATION_3)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f
        );
        
        SectorEntityToken hideaway = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        600f, // min radius
			900f, // max radius
			22, // min asteroid count
			35, // max asteroid count
			4f, // min asteroid radius 
			16f, // max asteroid radius
			"Gliese Field")); 
        hideaway.setCircularOrbit(system.getEntityById("anar"), 72, 14100, 260);
        //eventually to be replaced by a mysterious field which reduces detaction ability for detected fleets
        //as well as pings you with false sensor readings while nearby/within
        //also, will effect a significantly larger area
        
        SectorEntityToken a3 = system.getEntityById("euripides");
	JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("euripides_gate", "Euripides Gate");
	jumpPoint.setCircularOrbit(system.getEntityById("anar"), 240-60, 3750, 381);
	jumpPoint.setRelatedPlanet(a3);
        
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);
        
        //StarSystemGenerator.addSystemwideNebula(system, StarAge.AVERAGE);

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
