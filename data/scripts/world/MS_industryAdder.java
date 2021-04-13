package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.campaign.econ.MS_industries;
import java.util.HashMap;
import java.util.Map;

public class MS_industryAdder {
    
    public static void run(){
        addSolar();
        addFabs();
        //addMeds();
    }    
    
    public static void addSolar() {
        HashMap<String, String> h = new HashMap<>();
        h.put("asharu", null);
        h.put("nomios", null);
        h.put("olinadu", null);
        h.put("tigra_city", null);
        
        for (Map.Entry<String, String> entry : h.entrySet()) {
            MarketAPI m;
            
            if(Global.getSector().getEconomy().getMarket(entry.getKey()) != null){
                m = Global.getSector().getEconomy().getMarket(entry.getKey());
                if (!m.hasIndustry(MS_industries.SOLAR)
                        && !m.isPlayerOwned()
                        && !m.getFaction().getId().equals(Global.getSector().getPlayerFaction().getId())) {

                    m.addIndustry(MS_industries.SOLAR);
                    m.getIndustry(MS_industries.SOLAR).setAICoreId(entry.getValue());
                }
            }
        }
    }
    
    public static void addFabs() {
        HashMap<String, String> h = new HashMap<>();
        h.put("eldfell", null);
        
        for (Map.Entry<String, String> entry : h.entrySet()) {
            MarketAPI m;
            
            if(Global.getSector().getEconomy().getMarket(entry.getKey()) != null){
                m = Global.getSector().getEconomy().getMarket(entry.getKey());
                if (!m.hasIndustry(MS_industries.MODULARFACTORIES)
                        && !m.isPlayerOwned()
                        && !m.getFaction().getId().equals(Global.getSector().getPlayerFaction().getId())) {

                    m.addIndustry(MS_industries.MODULARFACTORIES);
                    m.getIndustry(MS_industries.MODULARFACTORIES).setAICoreId(entry.getValue());
                }
            }
        }
    }
    
    public static void addMeds() {
        HashMap<String, String> h = new HashMap<>();
        h.put("fikenhild", null);
        h.put("qaras", null);
        h.put("baetis", null);
        
        for (Map.Entry<String, String> entry : h.entrySet()) {
            MarketAPI m;
            
            if(Global.getSector().getEconomy().getMarket(entry.getKey()) != null){
                m = Global.getSector().getEconomy().getMarket(entry.getKey());
                if (!m.hasIndustry(MS_industries.MEDICALCENTER)
                        && !m.isPlayerOwned()
                        && !m.getFaction().getId().equals(Global.getSector().getPlayerFaction().getId())) {

                    m.addIndustry(MS_industries.MEDICALCENTER);
                    m.getIndustry(MS_industries.MEDICALCENTER).setAICoreId(entry.getValue());
                }
            }
        }
    }
}
