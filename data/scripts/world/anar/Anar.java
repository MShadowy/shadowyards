package data.scripts.world.anar;

import data.scripts.world.MS_Conditions;
import data.scripts.world.AddMarketplace;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import java.awt.Color;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import static com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.random;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain.TileParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.MS_industries;
import data.campaign.econ.MS_specialItems;
import java.util.ArrayList;
import java.util.Arrays;
import org.lwjgl.util.vector.Vector2f;

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
                3,
                new ArrayList<>(Arrays.asList(Conditions.NO_ATMOSPHERE, Conditions.HOT, Conditions.POPULATION_3, Conditions.LOW_GRAVITY)),
                new ArrayList<>(Arrays.asList(MS_industries.SOLAR, Industries.POPULATION, Industries.SPACEPORT)),
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
        
        MarketAPI euripidesMarket = AddMarketplace.addMarketplace("shadow_industry",
                euripides,
                new ArrayList<>(Arrays.asList((SectorEntityToken) station_pranaVayu)),
                "Euripides",
                7,
                new ArrayList<>(Arrays.asList(MS_Conditions.SEMI_ARID, Conditions.HABITABLE, Conditions.FARMLAND_ADEQUATE, 
                        Conditions.REGIONAL_CAPITAL, Conditions.POPULATION_7)),
                new ArrayList<>(Arrays.asList(MS_industries.ORBITAL3, Industries.PATROLHQ, MS_industries.MEDICALCENTER, Industries.FARMING, MS_industries.SHIPYARDS,
                    Industries.REFINING, Industries.LIGHTINDUSTRY, Industries.MEGAPORT, Industries.HEAVYBATTERIES, MS_industries.REDWINGS, Industries.POPULATION)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f
        );
        
        SectorEntityToken relay = system.addCustomEntity("anar_relay", // unique id
		"Anar Relay", // name - if null, defaultName from custom_entities.json will be used
		"comm_relay_makeshift", // type of object, defined in custom_entities.json
		"shadow_industry"); // faction
	relay.setCircularOrbit( system.getEntityById("anar"), 240+60, 3750, 381);
        
        SectorEntityToken sensor = system.addCustomEntity(null, null, "sensor_array_makeshift", "shadow_industry");
	sensor.setCircularOrbitPointingDown( system.getEntityById("anar"), 200, 8250, 560);
        
        SectorEntityToken nav = system.addCustomEntity(null, null, "nav_buoy_makeshift", "shadow_industry");
	nav.setCircularOrbitPointingDown( system.getEntityById("anar"), 120, 2500, 222);
        
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
        ProcgenUsedNames.notifyUsed("Melancholia");
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
        
        MarketAPI theraminMarket = AddMarketplace.addMarketplace("shadow_industry",
                theramin,
                new ArrayList<>(Arrays.asList((SectorEntityToken) station_gravitas)),
                "Theramin",
                6,
                new ArrayList<>(Arrays.asList(MS_Conditions.IRRADIATED_TERRAN, Conditions.HABITABLE, Conditions.EXTREME_WEATHER, 
                        Conditions.ORGANICS_COMMON, Conditions.FARMLAND_RICH, Conditions.RARE_ORE_ABUNDANT, Conditions.POPULATION_6)),
                new ArrayList<>(Arrays.asList(MS_industries.ORBITAL1, Industries.HEAVYBATTERIES, Industries.HIGHCOMMAND, 
                        Industries.MINING, MS_industries.PARALLEL_PRODUCTION, Industries.FARMING, Industries.POPULATION, Industries.SPACEPORT)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY)),
                0.3f
        );
        CargoAPI cargo = theraminMarket.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();
        cargo.addSpecial(new SpecialItemData("industry_bp", "ms_orbitalstation"), 1);
        
        AddMarketplace.addMarketplace(Factions.INDEPENDENT,
                melancholia,
                null,
                "Melancholia",
                3,
                new ArrayList<>(Arrays.asList(Conditions.ICE, Conditions.THIN_ATMOSPHERE, Conditions.FRONTIER, Conditions.VOLATILES_PLENTIFUL, 
                        Conditions.ORE_SPARSE, Conditions.RARE_ORE_SPARSE, Conditions.VICE_DEMAND, Conditions.POPULATION_3)),
                new ArrayList<>(Arrays.asList(Industries.MINING, Industries.POPULATION, Industries.SPACEPORT)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)), 
                0.3f
        );
        
        system.addAsteroidBelt(anar, 600, 18000, 1400, 600, 400, Terrain.ASTEROID_BELT, "Dusk Line");
        
        system.addRingBand(anar, "ringsMod", "dusty", 512f, 3, Color.white, 512f, 20000, 260);
        system.addRingBand(anar, "ringsMod", "rocky", 512f, 3, Color.white, 512f, 20000, 260);
        system.addAsteroidBelt(anar, 200, 14000, 1400, 200, 300, Terrain.ASTEROID_BELT, "Ergan Belt");
        
        //add hidden pirate base here
        //SectorEntityToken berins_stash = system.addCustomEntity("berins_stash", "Berins Stash", "station_shi_hiddenPirateBase", "pirates");
        //berins_stash.setCircularOrbitPointingDown(system.getEntityById("anar"), 72, 20100, 260);
        //berins_stash.setCustomDescriptionId("berins_stash");
        
        /*AddMarketplace.addMarketplace("pirates",
        berins_stash,
        null,
        "Berins Stash",
        3,
        new ArrayList<>(Arrays.asList(Conditions.FREE_PORT, Conditions.ORBITAL_STATION, Conditions.OUTPOST, Conditions.VICE_DEMAND, Conditions.ORGANIZED_CRIME,
        Conditions.HYDROPONICS_COMPLEX, Conditions.POPULATION_3)),
        new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
        0.3f
        );*/
        
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
        
        //StarSystemGenerator.addSystemwideNebula(system, StarAge.ANAR);

        system.autogenerateHyperspaceJumpPoints(true, true);
        
        nebular(system);
        cleanup(system);
    }
    
    void nebular(StarSystemAPI system) {
        int w = 128;
        int h = 128;
        
        StringBuilder string = new StringBuilder();
	for (int y = h - 1; y >= 0; y--) {
		for (int x = 0; x < w; x++) {
			string.append("x");
		}
	}
	SectorEntityToken nebula = system.addTerrain(Terrain.NEBULA, new TileParams(string.toString(),
			w, h,
			"terrain", "nebula_anar", 4, 4, null));
	nebula.getLocation().set(0, 0);
        
        NebulaTerrainPlugin nebulaPlugin = (NebulaTerrainPlugin)((CampaignTerrainAPI)nebula).getPlugin();
        NebulaEditor editor = new NebulaEditor(nebulaPlugin);
        
        editor.regenNoise();
        
        editor.noisePrune(0.75f);
        
        editor.regenNoise();
        
        for (PlanetAPI planet : system.getPlanets()) {
				
		if (planet.getOrbit() != null && planet.getOrbit().getFocus() != null &&
			planet.getOrbit().getFocus().getOrbit() != null) {
			// this planet is orbiting something that's orbiting something
			// its motion will be relative to its parent moving
			// don't clear anything out for this planet
			continue;
		}
				
		float clearThreshold = 0f; // clear everything by default
		float clearInnerRadius = 0f;
		float clearOuterRadius = 0f;
		Vector2f clearLoc = null;
	
				
		if (!planet.isStar() && !planet.isGasGiant()) {
			clearThreshold = 1f - Math.min(0f, planet.getRadius() / 300f);
			if (clearThreshold > 0.5f) clearThreshold = 0.5f;
		}
				
		Vector2f loc = planet.getLocation();
		if (planet.getOrbit() != null && planet.getOrbit().getFocus() != null) {
			Vector2f focusLoc = planet.getOrbit().getFocus().getLocation();
			float dist = Misc.getDistance(planet.getOrbit().getFocus().getLocation(), loc);
			float width = planet.getRadius() * 4f + 100f;
			if (planet.isStar()) {
				StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
				if (corona != null) {
					width = corona.getParams().bandWidthInEngine * 4f;
				}
			}
			clearLoc = focusLoc;
			clearInnerRadius = dist - width / 2f;
			clearOuterRadius = dist + width / 2f;
		} else if (planet.getOrbit() == null) {
			float width = planet.getRadius() * 4f + 100f;
			if (planet.isStar()) {
				StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
				if (corona != null) {
					width = corona.getParams().bandWidthInEngine * 4f;
				}
			}
			clearLoc = loc;
			clearInnerRadius = 0f;
			clearOuterRadius = width;
		}
				
		if (clearLoc != null) {
			float min = nebulaPlugin.getTileSize() * 2f;
			if (clearOuterRadius - clearInnerRadius < min) {
				clearOuterRadius = clearInnerRadius + min;
			}
			editor.clearArc(clearLoc.x, clearLoc.y, clearInnerRadius, clearOuterRadius, 0, 360f, clearThreshold);
		}
        }		
        
        float angleOffset = random.nextFloat() * 360f;
		editor.clearArc(0f, 0f, 30000, 31000 + 1000f * random.nextFloat(), 
				angleOffset + 0f, angleOffset + 360f * (2f + random.nextFloat() * 2f), 0.01f, 0f);
                
        // do some random arcs
	int numArcs = (int) (8f + 6f * random.nextFloat());
        
        for (int i = 0; i < numArcs; i++) {
		//float dist = 4000f + 10000f * random.nextFloat();
		float dist = 15000f + 15000f * random.nextFloat();
		float angle = random.nextFloat() * 360f;
			
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
		dir.scale(dist - (2000f + 8000f * random.nextFloat()));
		
		//float tileSize = nebulaPlugin.getTileSize();
		//float width = tileSize * (2f + 4f * random.nextFloat());
		float width = 800f * (1f + 2f * random.nextFloat());
			
		float clearThreshold = 0f + 0.5f * random.nextFloat();
		//clearThreshold = 0f;
			
		editor.clearArc(dir.x, dir.y, dist - width/2f, dist + width/2f, 0, 360f, clearThreshold);
	}
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