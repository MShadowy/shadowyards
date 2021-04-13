package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import data.campaign.econ.MS_industries;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MS_responseUtils {
    // basically just a collection of utility functions to get fleets stats
    // for use with the Redwings reinforcement fleets and pirate/pather base attack groups
    public static final String REDWINGS_MAP_KEY = "redwings_ReinforcementAndPirateResponse";
    private static final float INITIAL_RESERVE_SIZE_MULT = 0.75f;
    public static final float MIN_SPAWN_FP = 40f;
    
    private final String shadow = "shadow_industry";
    
    protected Map<String, Float> reserveStrength = new HashMap<>();
    protected Map<String, Float> reserves = new HashMap<>();
    
    public Random red_random;
    public Random redFleetRandom() 
    {
	return red_random;
    }
    
    public float reinforcements = 0;
    
    public static int redwingsFleetGenPoints(CampaignFleetAPI fleet)
    {
        int points = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
        {
            points += redwingsFleetGenPoints(member);
        }
        return points;
    }
    
    public static int redwingsFleetGenPoints(FleetMemberAPI member)
    {
        ShipAPI.HullSize size = member.getHullSpec().getHullSize();
        switch (size) {
            case CAPITAL_SHIP:
                return 8;
            case CRUISER: 
                return 4;
            case DESTROYER: 
                return 2;
            case FIGHTER:
            case FRIGATE:
                return 1;
            default:
                return 1;
        }
    }
    
    public static float getDaysToOrbit(CampaignFleetAPI fleet)
    {
        float daysToOrbit = 0.0F;
        if (fleet.getFleetPoints() <= 50.0F) {
            daysToOrbit = 2.0F;
        } else if (fleet.getFleetPoints() <= 100.0F) {
            daysToOrbit = 4.0F;
        } else if (fleet.getFleetPoints() <= 150.0F) {
            daysToOrbit = 6.0F;
        } else {
            daysToOrbit = 8.0F;
        }
        daysToOrbit *= (0.5F + (float)Math.random() * 0.5F);
        return daysToOrbit;
    }
    
    /*public void addReinforcementPoints(float addPoints) {
        if (!SectorManager.getManager().isHardMode()) {
            addPoints *= 0.5f;
        }
        reinforcements += addPoints;
        float pointsToSpawn = fleetSpawn;
    }*/
    
    public static float getMaxRedwingsSize(MarketAPI market, boolean raw)
    {
	int marketSize = market.getSize();
		
	float baseSize = marketSize * 5 + 8;
	float size = baseSize;
	if (raw) return size;
		
	if (market.hasIndustry(Industries.PATROLHQ)) size += baseSize * 0.05;
	if (market.hasIndustry(Industries.MILITARYBASE)) size += baseSize * 0.1;
	if (market.hasIndustry(Industries.HIGHCOMMAND)) size += baseSize * 0.2;
	if (market.hasIndustry(Industries.HEAVYINDUSTRY)) size += baseSize * 0.05;
	if (market.hasIndustry(Industries.ORBITALWORKS)) size += baseSize * 0.1;
	if (market.hasIndustry(Industries.MEGAPORT)) size += baseSize * 0.1;
	if (market.hasIndustry(MS_industries.MODULARFACTORIES)) size += baseSize * 0.03;
	if (market.hasIndustry(MS_industries.PARALLEL_PRODUCTION)) size += baseSize * 0.02;
	if (market.hasIndustry(MS_industries.MILITARY_LINES)) size += baseSize * 0.05;
	if (market.hasIndustry(MS_industries.SHIPYARDS)) size += baseSize * 0.09;
		
	size += baseSize;
		
	size += getPlayerLevelFPBonus();
		
	return size;
    }
    
    public static float modifyRedwingsSize(MarketAPI market, float delta) {
        MS_responseUtils manager = getManager();
        if (manager == null) return 0f;
        String mID = market.getId();
        if (!manager.reserves.containsKey(mID)) return 0f;
        float current = getRedwingsSize(market);
        float newVal = current + delta;
        float max = getMaxRedwingsSize(market, false);
        if (newVal > max) newVal = max;
        manager.reserves.put(mID, newVal);
        return newVal - current;
    }
    
    public static float getRedwingsSize(MarketAPI market) {
        if (market == null) return -1f;
        MS_responseUtils manager = getManager();
        if (manager == null) return -1f;
        String mID = market.getId();
        Map<String, Float> reserves = manager.reserves;
        if (!reserves.containsKey(mID)) {
            //probably fake, ignore
            if (!market.isInEconomy()) return -1f;
            
            reserves.put(mID, getMaxRedwingsSize(market, false)*INITIAL_RESERVE_SIZE_MULT);
        }
        return reserves.get(mID);
    }
    
    public static MS_responseUtils getManager() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        MS_responseUtils manager = (MS_responseUtils)data.get(REDWINGS_MAP_KEY);
        return manager;
    }
    
    public static MS_responseUtils create() {
        MS_responseUtils saved = getManager();
        if (saved != null) return saved;
        
        Map<String, Object> data = Global.getSector().getPersistentData();
        MS_responseUtils manager = new MS_responseUtils();
        data.put(REDWINGS_MAP_KEY, manager);
        return manager;
    }
    
    public static float getPlayerLevelFPBonus()
    {
        return Global.getSector().getPlayerPerson().getStats().getLevel() * 1f;
    }
        
    public CampaignFleetAPI getRedwingsFleet (MarketAPI origin, int points) {
        String fleetFaction = "redwings";
        
        String name = "";
        points *= 5;
        
        if (points <= 90) name = "Redwings Interception Unit";
        else if (points >= 270) name = "Redwings Battlegroup";
        else name = "Redwings Task Force";
        
        FleetParamsV3 fleetParams = new FleetParamsV3(origin, fleetFaction,
                points, //combat units
                0,      //freighters
                0,      //tankers
                0,      //transports
                0,      //liners
                0,      //utility
                0.5f); //quality mod
        fleetParams.random = redFleetRandom();
        
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(fleetParams);
        if (fleet == null) return null;
        
        fleet.setFaction(shadow, true);
        fleet.setName(name);
        fleet.setAIMode(true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        if (origin.getFaction().isHostileTo(Factions.PLAYER)) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true, 5);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true, 5);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, true, 5);
        }
        
        return fleet;
    }
}
