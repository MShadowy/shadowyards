package data.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;

public class MS_fleetFighterFinagler implements EveryFrameScript {
    // only bother with inflated fleets, and don't double check them
    // because fleets roam around we need to check them regularly enough so that the player doesn't run across un-finagled fleets
    /////////////////////////////// CONFIG ///////////////////////////////
    private static final String FIND_ID = "ms_shikome";
    private static final String REPLACE_ID = "ms_skinwalker";
    private static final int FP_THRESHOLD = 50; // allowed one shikome per this many FP (not allowed any if not over threshold)
    private final IntervalUtil timer = new IntervalUtil(2f, 5f); // min, max in seconds for check (you want this to run pretty often, but not every frame)
    /////////////////////////////// CONFIG ///////////////////////////////

    private static final String FIND_WING = FIND_ID + "_wing";
    private static final String REPLACE_WING = REPLACE_ID + "_wing";
    
    //garbage check timer; we don't need to run this too often, just enough to keep the list clear
    //private IntervalUtil clear = new IntervalUtil(7f, 14f);
    
    //Only run once per fleet
    //List<String> finagledFleets = new ArrayList<>();
    
    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        
        // advance the timer
        timer.advance(amount);
        // if the timer's not over, just give up and try again later
        if (!timer.intervalElapsed()) {
            return;
        }

        // loop through all the fleets around
        for (CampaignFleetAPI fleet : Global.getSector().getCurrentLocation().getFleets()) {

            // there are a whole host of reasons we might not care about checking a given fleet
            if (fleet == null
                    || !fleet.isInflated()
                    || !fleet.isPlayerFleet()
                    || !fleet.isAlive()
                    || fleet.isEmpty()
                    || fleet.isExpired()
                    || fleet.isDespawning()
                    || fleet.getFleetData() == null
                    || fleet.getFleetData().getMembersInPriorityOrder() == null
                    || fleet.getFleetData().getMembersInPriorityOrder().isEmpty()
                    || fleet.getFaction() == null
                    || fleet.getFaction().getKnownFighters() == null
                    || fleet.getFaction().getKnownFighters().isEmpty()
                    || !fleet.getFaction().getKnownFighters().contains(FIND_WING)) {
                continue;
            }

            // setup some variables
            int fp = fleet.getFleetPoints();
            int allowed = fp / FP_THRESHOLD;
            int count = 0; // per fleet

            // loop through all the members of this fleet
            List<FleetMemberAPI> members = fleet.getFleetData().getMembersInPriorityOrder();
            for (FleetMemberAPI member : members) {
                
                // grab the ship variant
                ShipVariantAPI variant = member.getVariant();
                if (variant == null) {
                    continue; // if the ship doesn't have a variant, idfk just skip it
                }
                
                // loop through all its flight decks
                for (int i = 0; i <= member.getNumFlightDecks(); i++) {
                    
                    // count shikome wings
                    if (FIND_WING.equals(variant.getWingId(i))) {
                        count++;
                        
                        // if we have too many shikome wings, replace this one with a skinwalker wing
                        if (count > allowed) {
                            member.getVariant().setWingId(i, REPLACE_WING);
                        }
                        
                    }
                    
                }
            }
        }
        
        //SectorAPI sector = Global.getSector();
        //if (sector == null) {
            //return;
        //}
        
        
        //if (timer.intervalElapsed()) {
            //finagleFighters();
        //}
    }
    
    /*public void finagleFighters() {
               
        for (CampaignFleetAPI f : Global.getSector().getCurrentLocation().getFleets()) {
            //if the fleet is still a stub or has already been checked we don't care
            if (!f.isInflated() || finagledFleets.contains(f.getId())) {
                continue;
            }
            //if the faction doesn't know how to make Shikomes there's no need to bother
            //since we don't want this to happen every time we set a bit so fleets don't get finagled multiple times
            if (!f.getFaction().getKnownFighters().contains("ms_shikome")) {
                finagledFleets.add(f.getId());
                continue;
            }
            finagledFleets.add(f.getId());
            
            int fSize = f.getFleetPoints();
            int max = fSize / FP_THRESHOLD;
            int curr = 0;
            
            //otherwise we rifle through the carriers and remove Shikomes
            for (FleetMemberAPI cv : f.getFleetData().getMembersInPriorityOrder()) {                
                if (cv.isFighterWing()) {
                    continue;
                }
                
                if (!cv.isCarrier()) {
                    continue;
                }
    
                // grab the ship variant
                ShipVariantAPI variant = cv.getVariant();
                if (variant == null) {
                    continue; // if the ship doesn't have a variant, idfk just skip it
                }
                
                for (int i=0; i <= cv.getNumFlightDecks(); i++) {
                    if (FIND_WING.equals(cv.getVariant().getWingId(i))) {
                        //cv.getVariant().setWingId(i, REPLACE_WING);
                            curr++;
                        if (curr > max) {
                            cv.getVariant().setWingId(i, REPLACE_WING);
                        }
                    }
                }
            }
            
            if (clear.intervalElapsed() && (f.isExpired() || !f.isAlive() 
                    || f.isEmpty() || f.isDespawning()) && finagledFleets.contains(f.getId())) {
                finagledFleets.remove(f.getId());
            }
        }
    }*/
}
