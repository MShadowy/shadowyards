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
                new ArrayList<>(Arrays.asList(Conditions.OUTPOST, Conditions.STEALTH_MINEFIELDS, Conditions.POPULATION_4)),
                new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES, Industries.MILITARYBASE, MS_industries.ORBITAL3, Industries.SPACEPORT, Industries.POPULATION)),
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
                new ArrayList<>(Arrays.asList(MS_industries.ORBITAL1, Industries.LIGHTINDUSTRY, MS_industries.MEDICALCENTER, 
                        Industries.PATROLHQ, Industries.POPULATION, Industries.SPACEPORT)),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.SUBMARKET_OPEN)),
                0.3f);
        
        /*We'll add a pair of additional vanilla markets, 1 independent, 1 persean, which have 
        Modular Fabricators; the indie one in particular should be economically in good shape
        experience a boom and as a result be leaning towards joing the SRA coalition*/
        
        //here we add a couple additional sources of supply/demand for SRA goods
        //Every vanilla polity should have at least 1 solar array, usually on small markets
        //Most should have a modular fab somewhere
        
        /*for (MarketAPI mrkt : sector.getEconomy().getMarketsCopy()) {
            if (!mrkt.isHidden()) {
                sectorMarkets.add(mrkt.getId());
            }
        }
        
        //List<MarketAPI> cMarkets = Global.getSector().getEconomy().getMarkets(corvus);
        //List<SectorEntityToken> cMarkets = Global.getSector().getEntityById(string);
        for (String m : sectorMarkets) {
            /*SectorEntityToken mId = m.getPrimaryEntity();
            if (mId.getMarket().getId().equals("asharu")) {
                mId.getMarket().addIndustry(MS_industries.SOLAR);
            }
            MarketAPI market = sector.getEconomy().getMarket(m);
            if (market == null) {
                continue;
            }
            
            //Ignore planets we don't want to add the Solar arrays to
            if (!market.getId().contains("asharu")) {
                continue;
            } else {
                market.addIndustry(MS_industries.SOLAR);
            }
        }*/
        //String planetID = primaryEntity.getId();  
        //String marketID = planetID;
        
        /*List<PlanetAPI> corvusList = corvus.getPlanets();
        for (PlanetAPI p : corvusList) {
            if (p.getId().equals("asharu")) {
                MarketAPI market = p.getMarket();
                if (market != null) {
                    market.addIndustry(MS_industries.SOLAR);
                }
            }
        }
        /*List<PlanetAPI> arcadiaList = arcadia.getPlanets();
        for (PlanetAPI p : arcadiaList) {
            if (p.getId().equals("nomios")) {
                market = p.getMarket();
                market.addIndustry(MS_industries.SOLAR);
            }
        }
        List<PlanetAPI> askoniaList = askonia.getPlanets();
        for (PlanetAPI p : askoniaList) {
            if (p.getId().equals("cruor")) {
                market = p.getMarket();
                market.addIndustry(MS_industries.SOLAR);
            }
        }
        List<PlanetAPI> kumaList = kumari.getPlanets();
        for (PlanetAPI p : kumaList) {
            if (p.getId().equals("olinadu")) {
                market = p.getMarket();
                market.addIndustry(MS_industries.SOLAR);
            }
        }
        List<PlanetAPI> narakaList = naraka.getPlanets();
        for (PlanetAPI p : narakaList) {
            if (p.getId().equals("nachiketa")) {
                market = p.getMarket();
                market.addIndustry(MS_industries.SOLAR);
            }
        }*/
    }
}
