package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class MS_overdriveStats extends BaseShipSystemScript {

    public static final float ROF_BONUS = 1f;
    public static final float FLUX_MULT = 0.8f;
    public static final float ROF_MALUS = 0.5f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == State.ACTIVE) {
            float mult = 1f + ROF_BONUS * effectLevel;
            stats.getBallisticRoFMult().modifyMult(id, mult);
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
            stats.getMissileRoFMult().modifyMult(id, mult);
            stats.getEnergyRoFMult().modifyMult(id, mult);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
            stats.getBeamWeaponFluxCostMult().modifyFlat(id, FLUX_MULT);
        } else if (state == State.OUT) {
            float mult = 1f - ROF_MALUS * effectLevel;
            stats.getBallisticRoFMult().modifyMult(id, mult);
            stats.getMissileRoFMult().modifyMult(id, mult);
            stats.getEnergyRoFMult().modifyMult(id, mult);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getBeamWeaponFluxCostMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = 1f + ROF_BONUS * effectLevel;
        float curs = 1f - ROF_MALUS * effectLevel;
        float bonusPercent = (int) (mult - 1f) * 100f;
        float malusPercent = (int) (curs - 1f) * 100f;
        if (index == 0) {
            return new StatusData("all weapons rate of fire +" + (int) bonusPercent + "%", false);
        } else if (index == 1) {
            return new StatusData("all weapons rate of fire -" + (int) malusPercent + "%", false);
        }
        return null;
    }
}
