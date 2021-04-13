
package data.scripts.world.gigas;

import data.scripts.world.AddMarketplace;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.MS_industries;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import data.scripts.util.MS_Utils;

public class Gigas {
    
    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Gigas");
        system.getLocation().set(-6500, -1550);
        //system.getLocation().set(-5250, 4500);
	LocationAPI hyper = Global.getSector().getHyperspace();
                
        system.setBackgroundTextureFilename("graphics/backgrounds/velkabg.jpg");
        
        PlanetAPI vel = system.initStar("Gigas", "star_red_supergiant", 800f, 600f, 17f, 2f, 3.5f);
        system.setLightColor(new Color(255, 220, 200));
        
        //Primarily focused on mining; Gigas is the closest of SHI's major systems to Heg space.
        //Currently shadowyards is in the midst of attempting to evict a Hegemony forward operating base from the system.
        //Additionally a sizable pirate presence has taken up residence, taking advantage of the reduced ability of the SRA (and near total indifference of the Hegemony) to police the system.
        //Gigas is a red giant, and has a sizeable resource base, making it a highly valuable target to contest
        
        PlanetAPI vel1 = system.addPlanet("rubicante", vel, "Rubicante", "lava_minor", 160, 150f, 2400f, 210f);
        vel1.setCustomDescriptionId("planet_rubicante");
        SectorEntityToken auris_grip = system.addCustomEntity("auris_grip", "Auris Grip", "station_pirate_type", "pirates");
        auris_grip.setCircularOrbitPointingDown(system.getEntityById("rubicante"), 45, 400, 50);
        auris_grip.setCustomDescriptionId("station_auris");
        //SRA doctrine places industrial centers to "directly support" military facilities
        //The Modular Fabricators here survived the destruction relatively intact and were salvaged by the Pirates on Auris Grip
        AddMarketplace.addMarketplace("pirates",  
                auris_grip,  
                null,  
                "Auris Grip",  
                3,  
                new ArrayList<>(Arrays.asList(Conditions.FREE_PORT, Conditions.ORGANIZED_CRIME, Conditions.STEALTH_MINEFIELDS, Conditions.POPULATION_3)),  
                new ArrayList<>(Arrays.asList(MS_industries.MODULARFACTORIES, Industries.ORBITALSTATION, Industries.PATROLHQ, Industries.SPACEPORT, Industries.POPULATION)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),  
                0.3f  
        );
        
        //Rubicante has a large, trailing debris cloud present from the destroyed SRA mining base that used to orbit the planet
        //Riskily close to the pirate base
        DebrisFieldParams params = new DebrisFieldParams(
                        500f,
                        1f,
                        10000000f,
                        0f);
        params.source = DebrisFieldSource.MIXED;
        params.baseSalvageXP = 250;
        SectorEntityToken rubicanteDebrisField = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        rubicanteDebrisField.setSensorProfile(null);
        rubicanteDebrisField.setDiscoverable(null);
        rubicanteDebrisField.setCircularOrbit(vel, 170f, 2200f, 210f);
        
        //a massive asteroid field and dust could with patchy orange nebulae dominates the system center
        system.addAsteroidBelt(vel, 400, 4500, 1400, 600, 400, Terrain.ASTEROID_BELT, "Clashed Belt");
        system.addAsteroidBelt(vel, 400, 4900, 1400, 600, 400, Terrain.ASTEROID_BELT, "Clashed Belt");
        system.addAsteroidBelt(vel, 400, 5300, 1400, 600, 400, Terrain.ASTEROID_BELT, "Clashed Belt");
        system.addAsteroidBelt(vel, 400, 5700, 1400, 600, 400, Terrain.ASTEROID_BELT, "Clashed Belt");
        system.addRingBand(vel, "ringsMod", "rocky", 1024f, 1, Color.white, 1024f, 4550, 60f);
        system.addRingBand(vel, "ringsMod", "cloudy", 1024f, 1, Color.white, 1024f, 4550, 60f);
        system.addRingBand(vel, "ringsMod", "rocky", 1024f, 1, Color.white, 1024f, 4950, 60f);
        system.addRingBand(vel, "ringsMod", "cloudy", 1024f, 1, Color.white, 1024f, 4950, 60f);
        system.addRingBand(vel, "ringsMod", "rocky", 1024f, 1, Color.white, 1024f, 5350, 60f);
        system.addRingBand(vel, "ringsMod", "cloudy", 1024f, 1, Color.white, 1024f, 5350, 60f);
        
        //A debris field in the belt 
        params = new DebrisFieldParams(
                        500f,
                        1f,
                        10000000f,
                        0f);
        params.source = DebrisFieldSource.MIXED;
        params.baseSalvageXP = 250;
        SectorEntityToken belterDebrisField = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        belterDebrisField.setSensorProfile(100f);
        belterDebrisField.setDiscoverable(true);
        belterDebrisField.setCircularOrbit(vel, 30f, 4600f, 180f);
        //and some derelict craft, remains of one of the battles between the Heg and SRA a year ago
        MS_Utils.addDerelict(system, vel, "ms_enlil_Standard", ShipCondition.BATTERED, 4613f, false);
	MS_Utils.addDerelict(system, vel, "kite_Standard", ShipCondition.BATTERED, 4545f, false);
        MS_Utils.addDerelict(system, vel, "wolf_d_pirates_Attack", ShipCondition.WRECKED, 4877f, false);
        
        PlanetAPI vel2 = system.addPlanet("leviathan", vel, "Leviathan", "gas_giant", 92, 400f, 8600f, 170f);
        //vel2.setCustomDescriptionId("planet_leviathan");
        //moons, 1 gas shrouded (sturmvald) 1 large frozen (stillness), rings
        
        SectorEntityToken levi_field = system.addTerrain(Terrain.MAGNETIC_FIELD, 
                new MagneticFieldTerrainPlugin.MagneticFieldParams(vel2.getRadius() + 100f,
                        (vel2.getRadius() + 100f) / 2f,
                        vel2,
                        vel2.getRadius() + 10f,
                        vel2.getRadius() + 10f + 90f,
                        new Color(50, 20, 100, 40), // base color
                        0.3f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(140, 100, 235),
			new Color(180, 110, 210),
			new Color(150, 140, 190),
			new Color(140, 190, 210),
			new Color(90, 200, 170), 
			new Color(65, 230, 160),
			new Color(20, 220, 70)
                ));
        levi_field.setCircularOrbit(vel2, 0, 0, 100);
        
        PlanetAPI vel2a = system.addPlanet("sturmvald", vel2, "Sturmvald", "rocky_unstable", 92, 80f, 800f, 170f);
        vel2a.setCustomDescriptionId("planet_sturmvald");
        PlanetAPI stillness = system.addPlanet("stillness", vel2, "Stillness", "rocky_ice", 110, 120f, 1200f, 210f);
        stillness.setCustomDescriptionId("planet_stillness");
        stillness.setFaction("shadow_industry");
        
        AddMarketplace.addMarketplace("shadow_industry",  
                stillness,  
                null,  
                "Stillness",  
                5,
                new ArrayList<>(Arrays.asList(Conditions.ICE, Conditions.THIN_ATMOSPHERE, Conditions.VOLATILES_ABUNDANT,
                        Conditions.LARGE_REFUGEE_POPULATION, Conditions.POPULATION_5)),  
                new ArrayList<>(Arrays.asList(Industries.PATROLHQ, Industries.LIGHTINDUSTRY, Industries.MINING, Industries.REFINING, 
                        Industries.GROUNDDEFENSES, Industries.SPACEPORT, Industries.POPULATION)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f  
        );
        
        MS_Utils.addDerelict(system, vel, "ms_ninurta_Standard", ShipCondition.AVERAGE, 11900f, true);
        MS_Utils.addDerelict(system, vel, "ms_sargasso_Assault", ShipCondition.AVERAGE, 11000f, true);
        
        PlanetAPI vel3 = system.addPlanet("kain", vel, "Kain", "frozen", 211, 110f, 14500f, 243f);
        //vel3.setCustomDescriptionId("planet_kain");
        SectorEntityToken lance_base = system.addCustomEntity("lance_base", "Lance Base", "station_side02", "hegemony");
        lance_base.setCircularOrbitPointingDown(system.getEntityById("kain"), 45, 400, 50);
        lance_base.setCustomDescriptionId("station_lance");
        
        AddMarketplace.addMarketplace(Factions.HEGEMONY,  
                lance_base,  
                null,  
                "Lance Base",  
                3,  
                new ArrayList<>(Arrays.asList(Conditions.FRONTIER, Conditions.OUTPOST, Conditions.POPULATION_3)),  
                new ArrayList<>(Arrays.asList(Industries.MILITARYBASE, Industries.BATTLESTATION, Industries.POPULATION, Industries.SPACEPORT)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),  
                0.3f  
        );
        
        SectorEntityToken relay = system.addCustomEntity("gigas_relay", // unique id
		"Gigas Relay", // name - if null, defaultName from custom_entities.json will be used
		"comm_relay", // type of object, defined in custom_entities.json
		"shadow_industry"); // faction
	relay.setCircularOrbit(system.getEntityById("gigas"), 160+60, 1900, 210);
        SectorEntityToken loc = system.addCustomEntity(null, null, "stable_location", Factions.NEUTRAL);
	loc.setCircularOrbitPointingDown( system.getEntityById("gigas"), 200, 5000, 400);
        
        SectorEntityToken nav = system.addCustomEntity(null, null, "nav_buoy_makeshift", Factions.HEGEMONY);
	nav.setCircularOrbitPointingDown( system.getEntityById("gigas"), 45, 12500, 222);
        
        //a gate, derelict, disassembeled
        SectorEntityToken gate = system.addCustomEntity("gigas_wrecked", // unique id
				 "Gigas Gate", // name - if null, defaultName from custom_entities.json will be used
				 "wrecked_gate", // type of object, defined in custom_entities.json
				 null); // faction
	gate.setCircularOrbit(system.getEntityById("gigas"), 0, 6600, 110);
        
        SectorEntityToken v = system.getEntityById("gigas");
	JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("gigas_gate", "Gigas Jump Node");
	jumpPoint.setCircularOrbit(v, 160-60, 2100, 210);
	jumpPoint.setRelatedPlanet(vel1);
        
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);
        
        StarSystemGenerator.addSystemwideNebula(system, StarAge.OLD);

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