package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MS_GAT extends BaseShipSystemScript {
    //GAT system; increases damage, ROF, and increases inaccuracy of energy weapons
    //Makes missiles launch speed a bit faster
    protected Object STATUSKEY1 = new Object();
    
    public static final Map<HullSize, Float> ROF_BONUS = new HashMap<>();
    static {
        ROF_BONUS.put(HullSize.FRIGATE, 0.25f);
        ROF_BONUS.put(HullSize.DESTROYER, 0.275f);
        ROF_BONUS.put(HullSize.CRUISER, 0.33f);
        ROF_BONUS.put(HullSize.CAPITAL_SHIP, 0.4f);
    }
    
    public static final Map<HullSize, Float> PROJ_DAM_MULT = new HashMap<>();
    static {
        PROJ_DAM_MULT.put(HullSize.FRIGATE, 1.25f);
        PROJ_DAM_MULT.put(HullSize.DESTROYER, 1.33f);
        PROJ_DAM_MULT.put(HullSize.CRUISER, 1.5f);
        PROJ_DAM_MULT.put(HullSize.CAPITAL_SHIP, 1.66f);
    }
    
    public static final Map<HullSize, Float> BEAM_DAM_MULT = new HashMap<>();//1.55f;
    static {
        BEAM_DAM_MULT.put(HullSize.FRIGATE, 1.55f);
        BEAM_DAM_MULT.put(HullSize.DEFAULT, 1.65f);
        BEAM_DAM_MULT.put(HullSize.CRUISER, 1.75f);
        BEAM_DAM_MULT.put(HullSize.CAPITAL_SHIP, 1.85f);
    }
    
    public static final float RECOIL_MULT = 1.66f;
    public static final float FLUX_USE_MULT = 0.75f;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        ShipSystemAPI system = ship.getSystem();
        List<WeaponAPI> weaps = ship.getAllWeapons();
        
        if (state == State.ACTIVE) {
            float mult = 1f + ROF_BONUS.get(ship.getHullSize()) * effectLevel;
            if (ship.getPhaseCloak() != null && ship.isPhased()) {
                ship.getPhaseCloak().deactivate();
            }
            
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
                    stats.getEnergyWeaponDamageMult().modifyMult(id, BEAM_DAM_MULT.get(ship.getHullSize()) * effectLevel);
                } else {
                    stats.getEnergyWeaponDamageMult().modifyMult(id, PROJ_DAM_MULT.get(ship.getHullSize()) * effectLevel);
                }
            }
            
            if (ship == Global.getCombatEngine().getPlayerShip() && effectLevel > 0) {
                float multB = RECOIL_MULT * effectLevel;
                float multC = BEAM_DAM_MULT.get(ship.getHullSize()) * effectLevel;
                float multD = PROJ_DAM_MULT.get(ship.getHullSize()) * effectLevel;
                float multE = FLUX_USE_MULT * effectLevel;
                float bonus1 = (int) (mult - 1f) * 100f;
                float bonus2 = (int) (multB - 1f) * 100f;
                float bonus3 = (int) (multC - 1f) * 100f;
                float bonus4 = (int) (multD - 1f) * 100f;
                float bonus5 = (int) (1f - multE) * 100f;
                
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "all weapons rate of fire increased " + (int) Math.round(bonus1) + "%", false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "damage increased - projectiles by " + (int) Math.round(bonus4) + "%, beams by " + (int) Math.round(bonus3) + "%" , false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "weapon recoil increased by " + (int) Math.round(bonus2) + "%" , false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "weapon flux use decreased by " + (int) Math.round(bonus5) + "%" , false);
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
        return null;
    }
}
