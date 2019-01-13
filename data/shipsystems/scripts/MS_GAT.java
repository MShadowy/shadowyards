package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.util.List;

public class MS_GAT extends BaseShipSystemScript {
    //GAT system; increases damage, ROF, and increases inaccuracy of energy weapons
    //Makes missiles launch speed a bit faster
    
    public static final float ROF_BONUS = 0.25f;
    public static final float PROJ_DAM_MULT = 1.25f;
    public static final float BEAM_DAM_MULT = 1.55f;
    public static final float RECOIL_MULT = 1.66f;
    public static final float FLUX_USE_MULT = 0.75f;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        List<WeaponAPI> weaps = ship.getAllWeapons();
        
        if (state == State.ACTIVE) {
            float mult = 1f + ROF_BONUS * effectLevel;
            stats.getEnergyRoFMult().modifyMult(id, mult);
            stats.getMaxRecoilMult().modifyMult(id, RECOIL_MULT * effectLevel);
            stats.getRecoilDecayMult().modifyMult(id, RECOIL_MULT * effectLevel);
            stats.getRecoilPerShotMult().modifyMult(id, RECOIL_MULT * effectLevel);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_USE_MULT * effectLevel);
            stats.getMissileMaxSpeedBonus().modifyMult(id, mult);
            for (WeaponAPI w : weaps) {
                if (!w.getType().equals(WeaponType.ENERGY)) {
                    continue;
                }
                
                if (w.isBeam()) {
                    stats.getEnergyWeaponDamageMult().modifyMult(id, BEAM_DAM_MULT * effectLevel);
                } else {
                    stats.getEnergyWeaponDamageMult().modifyMult(id, PROJ_DAM_MULT * effectLevel);
                }
            }
        }
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyRoFMult().unmodify(id);
        stats.getMaxRecoilMult().unmodify(id);
        stats.getRecoilDecayMult().unmodify(id);
        stats.getRecoilPerShotMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getMissileMaxSpeedBonus().unmodify(id);
        stats.getEnergyWeaponDamageMult().unmodify(id);
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = 1f + ROF_BONUS * effectLevel;
        float bonusPercent = (int) (mult - 1f) * 100f;
        switch (index) {
            case 0:
                return new StatusData("all weapons rate of fire +" + (int) bonusPercent + "%", false);
            case 1:
                return new StatusData("energy weapon damage increased", false);
            case 2:
                return new StatusData("weapon accuracy decreased", false);
            default:
                break;
        }
        return null;
    }
}
