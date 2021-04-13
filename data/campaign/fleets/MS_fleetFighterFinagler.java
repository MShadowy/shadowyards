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
                    || !fleet.isAlive()
                    || fleet.isPlayerFleet()
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
                if (variant == null || !member.isCarrier()) {
                    continue; // if the ship doesn't have a variant, idfk just skip it
                }
                
                // loop through all its flight decks
                for (int i = 0; i <= member.getNumFlightDecks(); i++) {
                    
                    // count shikome wings
                    if (FIND_WING.equals(variant.getWingId(i))) {
                        count++;
                    }
                }
                
                // if we have too many shikome wings, list all the decks in fleet?
                // then replace Shiki's with skinwalkers until we're under the limit
                if (count > allowed) {
                    for (int d = 0; d <= member.getNumFlightDecks(); d++) {
                        member.getVariant().setWingId(d, REPLACE_WING);
                        count--;
                    }
                }
            }
        }
    }
}
