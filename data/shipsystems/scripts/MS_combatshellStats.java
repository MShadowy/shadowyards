package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.awt.Color;

public class MS_combatshellStats extends BaseShipSystemScript {
    
    private static final float MAX_TIME_MULT = 3f;
	
    private static final Color JITTER_COLOR = new Color(15,15,15,55);
    private static final Color JITTER_UNDER_COLOR = new Color(234,253,185,155);
    private static final Color JITTER_CLEAR = new Color(0,0,0,0);
    
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
		
	float jitterLevel = effectLevel;
	float jitterRangeBonus = 0;
	float maxRangeBonus = 10f;
	if (null != state) switch (state) {
            case IN:
                jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
                if (jitterLevel > 1) {
                    jitterLevel = 1f;
                }
                jitterRangeBonus = jitterLevel * maxRangeBonus;
                stats.getBeamWeaponDamageMult().modifyPercent(id, 33f * effectLevel);
                stats.getEnergyRoFMult().modifyPercent(id, 33f * effectLevel);
                break;
            case ACTIVE:
                jitterLevel = 1f;
                jitterRangeBonus = maxRangeBonus;
                stats.getBeamWeaponDamageMult().modifyPercent(id, 33f * effectLevel);
                stats.getEnergyRoFMult().modifyPercent(id, 33f * effectLevel);
                break;
            case OUT:
                jitterRangeBonus = jitterLevel * maxRangeBonus;
                stats.getBeamWeaponDamageMult().modifyPercent(id, 33f * effectLevel);
                stats.getEnergyRoFMult().modifyPercent(id, 33f * effectLevel);
                break;
            default:
                break;
        }
	jitterLevel = (float) Math.sqrt(jitterLevel);
	effectLevel *= effectLevel;
		
	ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
	ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
		
	
	float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
	stats.getTimeMult().modifyMult(id, shipTimeMult);
	if (player) {
		Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
	} else {
		Global.getCombatEngine().getTimeMult().unmodify(id);
	}
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
	ShipAPI ship = null;
	boolean player = false;
	if (stats.getEntity() instanceof ShipAPI) {
		ship = (ShipAPI) stats.getEntity();
		player = ship == Global.getCombatEngine().getPlayerShip();
		id = id + "_" + ship.getId();
	} else {
		return;
	}

	Global.getCombatEngine().getTimeMult().unmodify(id);
	stats.getTimeMult().unmodify(id);
		
	stats.getHullDamageTakenMult().unmodify(id);
	stats.getArmorDamageTakenMult().unmodify(id);
        
        stats.getBeamWeaponDamageMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        
        ship.setJitter(this, JITTER_CLEAR, 0, 0, 0, 0f);
	ship.setJitterUnder(this, JITTER_CLEAR, 0, 0, 0f, 0f);
    }
	
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
	float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
	if (index == 0) {
		return new StatusData("time flow altered", false);
	}
	return null;
    }
}
