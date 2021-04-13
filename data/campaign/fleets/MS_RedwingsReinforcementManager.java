package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetStubAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolFleetManagerV2.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MS_responseUtils;
import static data.scripts.util.MS_responseUtils.getRedwingsSize;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

public class MS_RedwingsReinforcementManager extends BaseCampaignEventListener implements EveryFrameScript {
    
    private static final float INITIAL_RESERVE_SIZE_MULT = 0.75f;
    private static final float RESERVE_INCREMENT_PER_DAY = 0.08f;
    private static final float RESERVE_MARKET_STABILITY_DIVISOR = 5f;
    public static final float MIN_SPAWN_FP = 40f;
    
    public static Logger log = Global.getLogger(MS_RedwingsReinforcementManager.class);
    
    protected Map<String, Float> reserveStrength = new HashMap<>();
    public Map<String, Float> reserves = new HashMap<>();
    protected Random random = new Random();
    
    private final String shadow = "shadow_industry";
    private final String redComm = "ms_redwingsCommand";
    //private String INTEL_MEM_KEY = "intelligence notifier";
    private transient MarketAPI source;
    
    private final List<ReinforcementFleetData> activeFleets = new LinkedList();
    private final IntervalUtil tracker;
    private final IntervalUtil checker = new IntervalUtil(7f, 7f); //update which markets are part of the faction every week
    //private final IntervalUtil intelDelay = new IntervalUtil(4f, 7f);
    
    //private RollingAverageTracker SRAPatrolBattlesLost;
    
    private MS_responseUtils utils;
        
    public MS_RedwingsReinforcementManager() {
        super(true);
        
        float interval = Global.getSettings().getFloat("averagePatrolSpawnInterval");
        
        this.tracker = new IntervalUtil(interval * 0.75f, interval * 1.25f);
        
        reserves = new HashMap<>();
        List<MarketAPI> factionMarkets = Global.getSector().getEconomy().getMarketsCopy();
        for (MarketAPI fm : factionMarkets) {
            if (fm.getFactionId().contains(shadow) && fm.hasIndustry(redComm)) {
                reserves.put(fm.getId(), MS_responseUtils.getMaxRedwingsSize(fm, false)*INITIAL_RESERVE_SIZE_MULT);
                source = fm;
            }
        }
    }
    
    public void registerReinforcementFleetAI(CampaignFleetAPI fleet, MarketAPI origin, MarketAPI protect) {
        ReinforcementFleetData data = new ReinforcementFleetData(fleet);
	data.startingFleetPoints = fleet.getFleetPoints();
	data.sourceMarket = origin;
        data.protectedMarket = protect;
	data.source = origin.getPrimaryEntity();
	this.activeFleets.add(data);
		
	MS_RedwingsReinforcementFleetAI ai = new MS_RedwingsReinforcementFleetAI(fleet, data);
	fleet.addScript(ai);
    }
    
    public CampaignFleetAPI spawnRedwingsReinforcement(MarketAPI origin, MarketAPI protect, SectorEntityToken spawn) {
        float rSize = MS_responseUtils.getRedwingsSize(origin);
        int maxFP = (int)rSize;
        if (maxFP < MIN_SPAWN_FP) {
            log.info(origin.getName() + " has insufficient FP for response fleet: " + maxFP);
            return null;
        }
        
        CampaignFleetAPI fleet = utils.getRedwingsFleet(origin, maxFP);
        if (fleet == null) return null;
        
        registerReinforcementFleetAI(fleet, origin, protect);
        
        if (spawn == null) spawn = origin.getPrimaryEntity();
        spawn.getContainingLocation().addEntity(fleet);
        fleet.setLocation(spawn.getLocation().x, spawn.getLocation().y);
        
        log.info("Spawned " + fleet.getNameWithFaction() + " of size " + maxFP);
        reserves.put(origin.getId(), 0f);
        
        return fleet;
    }
    
    public void updateReserves(float days) {
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) return;
        
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        for (MarketAPI m : markets) {
            if (!reserves.containsKey(m.getId()))
                reserves.put(m.getId(), MS_responseUtils.getMaxRedwingsSize(m, false)*INITIAL_RESERVE_SIZE_MULT);
            
            int mSize = m.getSize();
            
            float baseInc = mSize * (0.5f + (m.getStabilityValue()/RESERVE_MARKET_STABILITY_DIVISOR));
            float increment = baseInc;
            
            if (m.hasIndustry(Industries.HIGHCOMMAND)) increment += baseInc * 0.25f;
            
            increment += baseInc;
            
            increment = increment * RESERVE_INCREMENT_PER_DAY * days;
            float newVal = Math.min(getRedwingsSize(m) + increment, MS_responseUtils.getMaxRedwingsSize(m, false));
            
            reserves.put(m.getId(), newVal);
        }
    }
    
    float lastReserveUpdateAge = 0f;
    @Override
    public void advance(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        
        //set up a dynamically updating list for general Nex compatibility
        checker.advance(days);
        List<MarketAPI> factionMarkets = new ArrayList<>();
        List<MarketAPI> commandMarket = new ArrayList<>();
        if (checker.intervalElapsed()) {
            for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                if (m.isInEconomy() && m.getFactionId().contains(shadow)) {
                    if (!factionMarkets.contains(m)) {
                        factionMarkets.add(m);
                    }
                }
                
                if (factionMarkets.contains(m) && (!m.getFactionId().contains(shadow) || m.hasCondition(Conditions.DECIVILIZED))) {
                    factionMarkets.remove(m);
                }
            }
            
            for (MarketAPI m2 : factionMarkets) {
                if (m2.hasIndustry(redComm)) commandMarket.add(m2);
            }
        }
        //if the redwings don't exist, we don't bother
        if (commandMarket.isEmpty()) return;
        
	lastReserveUpdateAge += days;
	if (lastReserveUpdateAge >= 1)
	{
            lastReserveUpdateAge -= 1;
            updateReserves(1);
	}
	
	this.tracker.advance(days);
        //List<String> intelKey = new ArrayList<>();
	if (this.tracker.intervalElapsed()) {
            for (MarketAPI fm : factionMarkets) {
                //track the FP lost compared to total deployed FP
                float market_deployed_FP = 0;
                float market_FP_lost = 0;
                
                // we need to get the patrol fleets so we can see how they're doing
                List<PatrolFleetData> patrols = new ArrayList<>();
                List<FleetStubAPI> patrolFleets = Global.getSector().getStarSystem(fm.getStarSystem().getBaseName()).getFleetStubs();
                for (FleetStubAPI pf : patrolFleets) {
                    PatrolFleetData p = (PatrolFleetData) pf;
                    
                    if (p == null || p.stub.getFleet() != pf) {
                        log.info("Could not find patrol fleet data stub");
                    } else {
                        log.info("Found patrol fleet data stub for " + p.toString());
                    }
                    
                    if (pf.getFleet().getFaction().getId().contains(shadow) && p != null && p.sourceMarket.getId().contains(fm.getId())) {
                        patrols.add(p);
                    }
                }
                
                for (PatrolFleetData data : patrols) {
                    float startingFP = data.startingFleetPoints;
                    FleetStubAPI fleet = data.stub;
                    int currFP = fleet.getFleet().getFleetPoints();
                    if (fleet.getFleet().isDespawning()) {
                        market_deployed_FP = market_deployed_FP - startingFP;
                    } else {
                        market_deployed_FP += startingFP;
                    }
                    
                    if (currFP < startingFP) {
                        float FP_lost = startingFP - currFP;
                        market_FP_lost += FP_lost;
                    }
                }
                
                if (market_FP_lost > market_deployed_FP * 0.4f) {
                    spawnRedwingsReinforcement(source, fm, (SectorEntityToken) source);
                    log.info("Running fleet creation call, starting from " + source.getName() + " and going to " + fm.getName());
                    //intelKey.add(INTEL_MEM_KEY);
                }
            }
	}
        /*if (!intelKey.isEmpty()) {
            intelDelay.advance(days);
            if (intelDelay.intervalElapsed()) {
                
                
                intelKey.clear();
            }
        }*/
        
	List<ReinforcementFleetData> remove = new LinkedList();
	for (ReinforcementFleetData data : this.activeFleets) {
            if ((data.fleet.getContainingLocation() == null) || (!data.fleet.getContainingLocation().getFleets().contains(data.fleet)) || (!data.fleet.isAlive())) {
		remove.add(data);
            }
	}
	this.activeFleets.removeAll(remove);
    }

    @Override
    public boolean isDone() {
        return false;
    }
    
    @Override
    public void reportFleetDespawned(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        super.reportFleetDespawned(fleet, reason, param);
        for (ReinforcementFleetData data : this.activeFleets) {
            if (data.fleet == fleet) {
                this.activeFleets.remove(data);
                break;
            }
        }
    }
    
    @Override
    public boolean runWhilePaused() {
        return false;
    }
    
    public static class ReinforcementFleetData
    {
        public CampaignFleetAPI fleet;
        public SectorEntityToken source;
        public SectorEntityToken target;
        public MarketAPI sourceMarket;
        public MarketAPI protectedMarket;
        public StarSystemAPI guardSystem;
        public float startingFleetPoints = 0.0F;
	
        public ReinforcementFleetData(CampaignFleetAPI fleet)
        {
            this.fleet = fleet;
        }
    }
}
