package data.scripts.world.outposts;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import data.campaign.econ.MS_industries;
import data.scripts.world.AddMarketplace;
import java.util.ArrayList;
import java.util.Arrays;


public class Outposter {
    
    public void generate(SectorAPI sector) {
        StarSystemAPI askonia = sector.getStarSystem("Askonia");
        StarSystemAPI yma = sector.getStarSystem("Yma");
        
        //Military outpost in Askonia; built during the Askonian crisis support SRA fleets intervening in the system
        //Placed trailing Nortia around Askonia, it has resisted numerous Diktat attempts to expel the outpost
        SectorEntityToken tiger_post = askonia.addCustomEntity("tiger_post", "Outpost Tiger", "station_shi_med", "shadow_industry");
        tiger_post.setCircularOrbitPointingDown(askonia.getEntityById("Askonia"), 280 - 60, 10000, 600);
        tiger_post.setCustomDescriptionId("outpost_tiger");
        
        AddMarketplace.addMarketplace("shadow_industry", 
                tiger_post,
                null,
                "Outpost Tiger",
                3,
                new ArrayList<>(Arrays.asList(Conditions.OUTPOST, Conditions.STEALTH_MINEFIELDS, Conditions.POPULATION_3)),
                new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES, Industries.MILITARYBASE, Industries.STARFORTRESS_HIGH, Industries.SPACEPORT, Industries.POPULATION)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_OPEN)),
                0.3f);
        
        //A civilian station in Yma, built in a hurry to assist refugee's resulting from Hanan Pacha's destruction
        //Also, somewhat less charitably, in order to contest the Hegemonies inevitable attempt to gain control of Qaras
        SectorEntityToken udana_station = yma.addCustomEntity("udana_stations", "Udana Station", "station_shi_med", "shadow_industry");
        udana_station.setCircularOrbitPointingDown(yma.getEntityById("hanan_pacha"), 210, 600, 415);
        udana_station.setCustomDescriptionId("station_udana");
        
        AddMarketplace.addMarketplace("shadow_industry",
                udana_station,
                null,
                "Udana Station",
                5,
                new ArrayList<>(Arrays.asList(Conditions.LARGE_REFUGEE_POPULATION, Conditions.URBANIZED_POLITY, Conditions.POPULATION_5)),
                new ArrayList<>(Arrays.asList(Industries.ORBITALSTATION_HIGH, Industries.LIGHTINDUSTRY, MS_industries.MEDICALCENTER, 
                        Industries.PATROLHQ, Industries.POPULATION, Industries.SPACEPORT)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f);
    }
}
