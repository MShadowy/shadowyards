package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class MS_phaseanchorStats extends BaseShipSystemScript {

    private static final Color JITTER_COLOR = new Color(255,175,255,255);
    private static final float JITTER_FADE_TIME = 0.5f;
    
    private static final Map<HullSize, Float> mag = new HashMap<>();
    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 60f);
        mag.put(ShipAPI.HullSize.FRIGATE, 60f);
        mag.put(ShipAPI.HullSize.DESTROYER, 50f);
        mag.put(ShipAPI.HullSize.CRUISER, 40f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 30f);
    }
    
    private static final float SHIP_ALPHA_MULT = 0.25f;
    private static final float VULNERABLE_FRACTION = 0f;
	
    private static final float MAX_TIME_MULT = 3f;
    
    public static boolean FLUX_LEVEL_AFFECTS_SPEED = true;
    public static float MIN_SPEED_MULT = 0.5f;
    public static float BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.33f;
	
    protected Object STATUSKEY1 = new Object();
    protected Object STATUSKEY2 = new Object();
    protected Object STATUSKEY3 = new Object();
    protected Object STATUSKEY4 = new Object();
    
    public static float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }
    
    protected boolean isDisruptable(ShipSystemAPI cloak) {
	return cloak.getSpecAPI().hasTag(Tags.DISRUPTABLE);
    }
    
    protected float getDisruptionLevel(ShipAPI ship) {
	//return disruptionLevel;
        //if (true) return 0f;
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            float threshold = ship.getMutableStats().getDynamic().getMod(
                            Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED);
            if (threshold <= 0) return 1f;
            float level = ship.getHardFluxLevel() / threshold;
            if (level > 1f) level = 1f;
            return level;
	}
	return 0f;
    }
    
    private void maintainStatus(ShipAPI playerShip, State state, float effectLevel) {
	float level = effectLevel;
	float f = VULNERABLE_FRACTION;
	
	ShipSystemAPI cloak = playerShip.getPhaseCloak();
	if (cloak == null) cloak = playerShip.getSystem();
	if (cloak == null) return;
	
	if (level > f) {
		Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
				cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "time flow altered", false);
	} else { }
        
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (level > f) {
                if (getDisruptionLevel(playerShip) <= 0f) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3, cloak.getSpecAPI().getIconSpriteName(), "phase anchors nominal", "top speed at 100%", false);
                }
            } else {
                String speedMalus = (int) Math.round(getSpeedMult(playerShip, effectLevel) * 100f) + "%" ;
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3, 
                        cloak.getSpecAPI().getIconSpriteName(),
                        "phase anchors under load",
                        "top speed at " + speedMalus,
                        true);
            }
        }
    }
    
    public float getSpeedMult(ShipAPI ship, float effectLevel) {
        if (getDisruptionLevel(ship) <= 0f) return 1f;
        return MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(ship) * effectLevel);
    }
    
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
        
        if (player) {
            maintainStatus(ship, state, effectLevel);
        }
        
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        ShipSystemAPI anchor = ship.getPhaseCloak();
        if (anchor == null) anchor = ship.getSystem();
        if (anchor == null) return;
        
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (state == State.ACTIVE || state == State.OUT || state == State.IN) {
                float mult = getSpeedMult(ship, effectLevel);
                if (mult < 1f) {
                    stats.getMaxSpeed().modifyMult(id + "_2", mult);
                } else {
                    stats.getMaxSpeed().unmodifyMult(id + "_2");
                }
                ((PhaseCloakSystemAPI)anchor).setMinCoilJitterLevel(getDisruptionLevel(ship));
            }
        }
        
        if (state == State.COOLDOWN || state == State.IDLE) {
            unapply(stats, id);
            return;
        }
        
        float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
        stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);
        
        float speedMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult();
        stats.getMaxSpeed().modifyMult(id, speedMultMod * effectLevel);
        
        float level = effectLevel;
	//float f = VULNERABLE_FRACTION;
        
	float levelForAlpha = level;
        
        if (state == State.IN || state == State.ACTIVE) {
            ship.setPhased(true);
            levelForAlpha = level;
            
            stats.getAcceleration().modifyFlat(id, mag.get(ship.getHullSize()) * effectLevel);
            stats.getDeceleration().modifyFlat(id, mag.get(ship.getHullSize()) * effectLevel);

            stats.getTurnAcceleration().modifyPercent(id, 180f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 140f);

            stats.getMaxSpeed().modifyFlat(id, mag.get(ship.getHullSize()) * effectLevel);
        } else if (state == State.OUT) {
            if (level > 0.5f) { 
                ship.setPhased(true);
            } else {
                ship.setPhased(false);
            }
            levelForAlpha = level;
            
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        }
        
        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha);
	ship.setApplyExtraAlphaToEngines(true);
	
        float extra = 0f;
	
	float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * levelForAlpha * (1f - extra);
	stats.getTimeMult().modifyMult(id, shipTimeMult);
	if (player) {
		Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
	} else {
		Global.getCombatEngine().getTimeMult().unmodify(id);
	}
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
	if (stats.getEntity() instanceof ShipAPI) {
		ship = (ShipAPI) stats.getEntity();
	} else {
		return;
	}
        
        Global.getCombatEngine().getTimeMult().unmodify(id);
	stats.getTimeMult().unmodify(id);
	
	ship.setPhased(false);
	ship.setExtraAlphaMult(1f);
        
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxSpeed().unmodifyPercent(id);
        stats.getMaxSpeed().unmodifyMult(id + "_2");
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
        ShipSystemAPI anchor = ship.getPhaseCloak();
        if (anchor == null) anchor = ship.getSystem();
        if (anchor != null) {
            ((PhaseCloakSystemAPI)anchor).setMinCoilJitterLevel(0f);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }
}