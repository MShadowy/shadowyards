package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
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
    protected Object STATUSKEY2 = new Object();
    protected Object STATUSKEY3 = new Object();
    protected Object STATUSKEY4 = new Object();
    
    private static final Map<HullSize, Float> ROF_BONUS = new HashMap<>();
    static {
        ROF_BONUS.put(HullSize.FRIGATE, 0.25f);
        ROF_BONUS.put(HullSize.DESTROYER, 0.275f);
        ROF_BONUS.put(HullSize.CRUISER, 0.33f);
        ROF_BONUS.put(HullSize.CAPITAL_SHIP, 0.4f);
    }
    
    private static final Map<HullSize, Float> PROJ_DAM_MULT = new HashMap<>();
    static {
        PROJ_DAM_MULT.put(HullSize.FRIGATE, 1.25f);
        PROJ_DAM_MULT.put(HullSize.DESTROYER, 1.33f);
        PROJ_DAM_MULT.put(HullSize.CRUISER, 1.5f);
        PROJ_DAM_MULT.put(HullSize.CAPITAL_SHIP, 1.66f);
    }
    
    private static final Map<HullSize, Float> BEAM_DAM_MULT = new HashMap<>();//1.55f;
    static {
        BEAM_DAM_MULT.put(HullSize.FRIGATE, 1.55f);
        BEAM_DAM_MULT.put(HullSize.DEFAULT, 1.65f);
        BEAM_DAM_MULT.put(HullSize.CRUISER, 1.75f);
        BEAM_DAM_MULT.put(HullSize.CAPITAL_SHIP, 1.85f);
    }
    
    private static final float RECOIL_MULT = 1.66f;
    private static final float FLUX_USE_MULT = 0.75f;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
        
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        
        ShipSystemAPI system = ship.getSystem();
        List<WeaponAPI> weaps = ship.getAllWeapons();
        
        if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isActive()) {
            if (ship.getSystem().isActive()) ship.getPhaseCloak().deactivate();
        }
        
        if (state == State.ACTIVE) {
            float mult = 1f + ROF_BONUS.get(ship.getHullSize()) * effectLevel;
            
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
                
                if (!w.isBeam()) {
                    stats.getEnergyWeaponDamageMult().modifyMult(id, PROJ_DAM_MULT.get(ship.getHullSize()) * effectLevel);   
                }
                for (BeamAPI b : w.getBeams()) {
                    if (b != null) {
                        stats.getBeamWeaponDamageMult().modifyMult(id, BEAM_DAM_MULT.get(ship.getHullSize()) * effectLevel);
                    }
                }
            }
            if (player && effectLevel > 0) {
                float multB = RECOIL_MULT * effectLevel;
                float multC = BEAM_DAM_MULT.get(ship.getHullSize()) * effectLevel;
                float multD = PROJ_DAM_MULT.get(ship.getHullSize()) * effectLevel;
                float multE = FLUX_USE_MULT * effectLevel;
                float bonus1 = (int) (ROF_BONUS.get(ship.getHullSize()) * effectLevel * 100f);
                float bonus2 = (int) (100f - (multB * 100f)) * -1f;
                float bonus3 = (int) (100f - (multC * 100f)) * -1f;
                float bonus4 = (int) (100f - (multD * 100f)) * -1f;
                float bonus5 = (int) 100f - (multE * 100f);
                
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "all weapons rate of fire increased " + (int) Math.round(bonus1) + "%", false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "damage increased - projectiles by " + (int) Math.round(bonus4) + "%, beams by " + (int) Math.round(bonus3) + "%" , false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                    "weapon recoil increased by " + (int) Math.round(bonus2) + "%" , false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY4,
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
