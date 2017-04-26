package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lazywizard.lazylib.MathUtils;
import java.util.Iterator;

import java.util.HashMap;
import java.util.Map;

public class MS_jammer implements ShipSystemStatsScript {

    private static CombatEngineAPI engine = null;

    public static final float RANGE = 1200f;
    public static final float ACCURACY_BONUS = -50f;
    public static final float RANGE_BONUS = -20f;

    private static final Map<ShipAPI, ShipAPI> jamming = new HashMap<>();

    private static final String staticID = "shadowyJammerDebuff";

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            jamming.clear();
        }

        //Declares two objects of type ShipAPI. 'ship' is just a generic holder for ships that are cycled through. 'host_ship' is the ship that is using the system.
        ShipAPI host_ship = (ShipAPI) stats.getEntity();

        for (ShipAPI ship : engine.getShips()) {
            if (!ship.isAlive()) {
                continue; //We don't want to bother modifying stats of the ship if it's disabled.
            }
            if (ship == host_ship) {
                continue;
            }

            if ((host_ship.getOwner() != ship.getOwner()) && (MathUtils.getDistance(ship, host_ship) <= (RANGE))) {
                //Modify this ship's stats.
                ship.getMutableStats().getAutofireAimAccuracy().modifyPercent(staticID, ACCURACY_BONUS);
                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(staticID, RANGE_BONUS);
                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(staticID, RANGE_BONUS);
                ship.getMutableStats().getSightRadiusMod().modifyPercent(staticID, RANGE_BONUS);

                //Adds the ship to the hashmap, and associates it with the host ship.
                jamming.put(ship, host_ship);
                //If the ship isn't in range but is contained in the hashmap, and the host ship of the ship is indeed this one...
            } else if ((jamming.containsKey(ship)) && (jamming.get(ship) == host_ship)) {
                //removes all benefits
                ship.getMutableStats().getAutofireAimAccuracy().unmodify(staticID);
                ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(staticID);
                ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(staticID);
                ship.getMutableStats().getSightRadiusMod().unmodify(staticID);

                //Removes the ship from the hashmap.
                jamming.remove(ship);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        //same objects as before.
        ShipAPI host_ship = (ShipAPI) stats.getEntity();
        //Loops through all the ships in the hashmap.
        Iterator<Map.Entry<ShipAPI, ShipAPI>> iter = jamming.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ShipAPI, ShipAPI> entry = iter.next();
            ShipAPI ship = entry.getKey();

            //(This makes it so that one host ship bringing down its system doesn't remove benefits that are being applied to other ships by host ships elsewhere.
            if (entry.getValue() == host_ship) {
                //removes all benefits
                ship.getMutableStats().getAutofireAimAccuracy().unmodify(staticID);
                ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(staticID);
                ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(staticID);

                iter.remove();
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("wide spectrum jamming active", false);
        }
        return null;
    }
}
