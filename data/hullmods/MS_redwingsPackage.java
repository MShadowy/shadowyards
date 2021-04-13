package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class MS_redwingsPackage extends BaseHullMod {
    
    private static final float SHIELD_TANK = 0.85f;
    private static final float DAMAGE_BOOST = 5f;
    private static final float MAX_CR_BONUS = 15f;
    
    private static final String DATA_KEY = "ms_redwingsPackage";
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        
        stats.getShieldDamageTakenMult().modifyMult(id, SHIELD_TANK);
        stats.getPhaseCloakActivationCostBonus().modifyMult(id, SHIELD_TANK);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, SHIELD_TANK);
        
        stats.getBeamWeaponDamageMult().modifyPercent(id, DAMAGE_BOOST);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_BOOST);
        stats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_BOOST);
        
        //in order to represent the redwings elite status, the basic crews get a 15% CR bonus independent of officer skills
        if (ship != null && ship.getFleetMember() != null) { 
            if (ship.getFleetMember().getFleetData().getFleet().getId().startsWith("redwings")) {
                stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f);
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        float shieldDisp = 0.1f * 100f;
        if (index == 0) return "" + (int) shieldDisp;
        if (index == 1) return "" + (int) DAMAGE_BOOST;
	return null;
    }
    
    /*protected String getAltSpriteSuffix() {
        return "_redwing";
    }
    
    protected void updateWeaponLook(ShipAPI ship) {
        for (WeaponAPI w : ship.getAllWeapons()) {
            int frame;
            
            if (!w.getId().startsWith("ms_")) {
                continue;
            }
            
            String spriteId = w.getSprite().toString();
        }
    }*/
}
