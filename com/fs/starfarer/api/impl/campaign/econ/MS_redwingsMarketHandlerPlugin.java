package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import data.campaign.econ.MS_industries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MS_redwingsMarketHandlerPlugin implements EveryFrameScript {
    
    private static final List<String> SHIP_SELECT = new ArrayList<>(Arrays.asList(new String[]{
        "ms_ninurta",
        "ms_enlil_redwing",
        "ms_seski_redwing",
        "ms_shamash_redwing",
        "ms_morningstar_redwing",
        "ms_clade_redwing",
        "ms_elysium_redwing",
        "ms_scylla_redwing",
        "ms_charybdis_redwing",
        "ms_mimir_redwing",
        "ms_skadi_redwing",
        "ms_vardr_redwing"
    }));
    
    private RedwingsMarketListener listener;
    
    protected boolean cInit = false;
    protected boolean bInit = false;
    
    @Override
    public void advance (float amount) {
        SectorAPI sector = Global.getSector();
        
        if (sector == null) return;
        
        //Ensure we have a listener
        if (listener == null) {
            listener = new RedwingsMarketListener();
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
    
    private class RedwingsMarketListener extends BaseCampaignEventListener {
        private List<MarketAPI> targetMarkets = new ArrayList<>();
        private boolean hasRedwings = false;
        SectorAPI sector = Global.getSector();
        
        private RedwingsMarketListener() {
            super(true);
        }
        
        @Override
        public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
            MarketAPI commandMarket = null;
            for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                if (m.hasIndustry(MS_industries.REDWINGS)) {
                    commandMarket = m;
                }
            }
            
            if (commandMarket != null) {
                hasRedwings = true;
            }
            
            //Redwings need their command structure to exist:
            if (hasRedwings) {
                if (!targetMarkets.contains(market)) {
                    FactionAPI shadow = sector.getFaction("shadow_industry");
                    if (market.getFactionId().contains(shadow.getId())) {
                        if (market.getSubmarket(Submarkets.GENERIC_MILITARY) != null) {
                            CargoAPI cargo = market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();
                            if (cargo != null) {
                                int size = market.getSize();
                                for (int i = 0; i < size; i++) {
                                    //run a random against a set float
                                    //on success we add a random ship from the redwings lineup
                                    if (Math.random() > 0.8f) {
                                        Random rand = new Random();
                                        String shipToAdd = SHIP_SELECT.get(rand.nextInt(SHIP_SELECT.size())) + "_Hull";
                                        String name = shadow.pickRandomShipName();
                                        if (shipToAdd != null) {
                                            //log.debug("Adding Ship: type = " + shipToAdd + " at " + market.getName());
                                            cargo.addMothballedShip(FleetMemberType.SHIP, shipToAdd, name);
                                        }
                                    }
                                }
                                
                                cInit = true;
                            }  
                            
                            if (cInit) {
                                cargo.initMothballedShips(shadow.getId());
                            }
                        }

                        CargoAPI blackM = market.getSubmarket(Submarkets.SUBMARKET_BLACK).getCargo();
                        if (blackM != null) {
                            //same odds, but fewer chances
                            for (int i = 0; i < 2; i++) {
                                if (Math.random() > 0.9f) {
                                    Random rand = new Random();
                                    String shipToAdd = SHIP_SELECT.get(rand.nextInt(SHIP_SELECT.size())) + "_Hull";
                                    String name = shadow.pickRandomShipName();
                                    if (shipToAdd != null) {
                                        blackM.addMothballedShip(FleetMemberType.SHIP, shipToAdd, name);
                                    }
                                }
                            }
                            
                            bInit = true;
                        }
                        
                        if (bInit) {
                            blackM.initMothballedShips(shadow.getId());
                        }

                        targetMarkets.add(market);
                    }
                }
            }
        }
        
        @Override
        public void reportEconomyMonthEnd() {
            targetMarkets.clear();
        }
    }
}
