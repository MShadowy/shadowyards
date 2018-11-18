package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class MS_boosterstats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 80f * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 5f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 220f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 150f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 100f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 50f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("improved maneuverability", false);
        } else if (index == 1) {
            return new StatusData("increased top speed", false);
        }
        return null;
    }
}
