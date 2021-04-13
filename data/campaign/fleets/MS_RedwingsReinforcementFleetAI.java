package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.scripts.util.MS_responseUtils;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;

public class MS_RedwingsReinforcementFleetAI implements EveryFrameScript {
    public static final float RESERVE_RESTORE_EFFICIENCY = 0.75f;
    public static Logger log = Global.getLogger(MS_RedwingsReinforcementFleetAI.class);
	
    protected final MS_RedwingsReinforcementManager.ReinforcementFleetData data;
    protected float daysTotal = 0.0F;
    protected final CampaignFleetAPI fleet;
    protected boolean orderedReturn = false;
	
    public MS_RedwingsReinforcementFleetAI(CampaignFleetAPI fleet, MS_RedwingsReinforcementManager.ReinforcementFleetData data)
    {
	this.fleet = fleet;
	this.data = data;
	giveInitialAssignment();
    }
	
    float interval = 0;
	
    @Override
    public void advance(float amount)
    {
	float days = Global.getSector().getClock().convertToDays(amount);
	this.daysTotal += days;
	if (this.daysTotal > 30.0F)
	{
            giveStandDownOrders();
            return;
	}
		
	interval += days;
	if (interval >= 0.25f) interval -= 0.25f;
	else return;
		
	FleetAssignmentDataAPI assignment = this.fleet.getAI().getCurrentAssignment();
	float fp = this.fleet.getFleetPoints();
		
	if ((assignment == null || !MathUtils.isWithinRange(fleet, data.source, 900)) && fleet.getBattle() == null)
	{
            fleet.getAI().clearAssignments();
            MarketAPI market = data.sourceMarket;
            StarSystemAPI system = market.getStarSystem();
            if (system != null)
            {
                if (system != this.fleet.getContainingLocation()) {
			this.fleet.addAssignment(FleetAssignment.DELIVER_SUPPLIES, market.getPrimaryEntity(), 1000.0F, 
                                            "travellingToStarSystem" + system.getBaseName());
                }
                this.fleet.addAssignment(FleetAssignment.DELIVER_SUPPLIES, market.getPrimaryEntity(), 1000.0F, 
					"travellingTo" + market.getName());
                this.fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, market.getPrimaryEntity(), 1000.0F, 
					"defending" + market.getName());
            }
        }
    }
	
    @Override
    public boolean isDone()
    {
	return !this.fleet.isAlive();
    }
	
    @Override
    public boolean runWhilePaused()
    {
	return false;
    }
	
    protected void giveInitialAssignment()
    {
	this.fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, this.data.source, 0.1f, "scramblingFrom" + this.data.sourceMarket.getName());
	this.fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, this.data.target, 1000.0f, "inTransitTo" + this.data.protectedMarket.getName());
        this.fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, (SectorEntityToken) this.data.guardSystem, 35f, "patrolling" + this.data.guardSystem.getBaseName());
    }
	
    protected void giveStandDownOrders() {
	if (!this.orderedReturn) {
            //log.info("Response fleet " + this.fleet.getNameWithFaction() + " standing down");
            this.orderedReturn = true;
            this.fleet.clearAssignments();
			
            Script despawnScript = new Script() {
            @Override
                public void run() {
                    float points = MS_responseUtils.redwingsFleetGenPoints(fleet) * RESERVE_RESTORE_EFFICIENCY;
                    log.info("Response fleet despawning at base " + data.source.getName() + "; can restore " + points + " points");
                    MS_responseUtils.modifyRedwingsSize(data.sourceMarket, points);
                }
            };
			
            SectorEntityToken destination = data.source;		  
            this.fleet.addAssignment(FleetAssignment.DELIVER_CREW, destination, 1000.0F, "returningTo" + destination.getName());
            this.fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, destination, MS_responseUtils.getDaysToOrbit(fleet), "standingDown" + "missionPatrol", despawnScript);
            this.fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, destination, 1000.0F);
	}
    }
}
