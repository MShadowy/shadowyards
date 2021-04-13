package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.util.ShaderLib;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_swacs extends BaseShipSystemScript {

    private CombatEngineAPI engine;
    
    public static final Object KEY_JITTER = new Object();
    
    private static final Color COLOR1 = new Color(210, 125, 105, 155);
    
    private static final Color JITTER_UNDER_COLOR = new Color(255,50,0,125);
    private static final Color JITTER_COLOR = new Color(255,50,0,75);

    //Just some global variables.
    private static final float ACCURACY_BONUS = 20f;
    private static final float RANGE_BONUS = 10f;
    private static final float DAMAGE_BOOST = 33f;
    private static final float AGILITY_BONUS = 15f;
    private static final Vector2f ZERO = new Vector2f();

    //Creates a hashmap that keeps track of what ships are receiving the benefits.
    private static final Map<ShipAPI, ShipAPI> RECIEVING = new HashMap<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            RECIEVING.clear();
        }
        
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
		ship = (ShipAPI) stats.getEntity();
	} else {
		return;
	}

        if (effectLevel > 0) {
		float jitterLevel = effectLevel;
		float maxRangeBonus = 5f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		for (ShipAPI fighter : getFighters(ship)) {
			if (fighter.isHulk()) continue;
			MutableShipStatsAPI fStats = fighter.getMutableStats();
				
			fStats.getBallisticWeaponDamageMult().modifyPercent(id, 1f + 0.01f * DAMAGE_BOOST * effectLevel);
			fStats.getEnergyWeaponDamageMult().modifyPercent(id, 1f + 0.01f * DAMAGE_BOOST * effectLevel);
			fStats.getMissileWeaponDamageMult().modifyPercent(id, 1f + 0.01f * DAMAGE_BOOST * effectLevel);
                        
                        fStats.getAutofireAimAccuracy().modifyPercent(id, ACCURACY_BONUS);
                        fStats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                        fStats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                        fStats.getBeamWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
                        
                        fStats.getMaxSpeed().modifyPercent(id, AGILITY_BONUS);
                        fStats.getAcceleration().modifyPercent(id, AGILITY_BONUS);
                        fStats.getDeceleration().modifyPercent(id, AGILITY_BONUS);
                        fStats.getMaxTurnRate().modifyPercent(id, AGILITY_BONUS);
                        fStats.getTurnAcceleration().modifyPercent(id, AGILITY_BONUS);
				
			if (jitterLevel > 0 && ShaderLib.isOnScreen(ZERO, 100f)) {
				//fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
				fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponType.class));
					
				fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
				fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
				Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
                                
                                for (ShipEngineAPI engines : fighter.getEngineController().getShipEngines()) {
                                    if (engines.isDisabled() == false) {
                                    float size;
                                    for (int i = 0; i < 5; i++) {
                                        size = MathUtils.getRandomNumberInRange(4f, 1f);
                                        Vector2f spawn = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
                            
                                        if (Math.random() > 0.9 && !engine.isPaused()) {
                                            engine.addSmoothParticle(spawn, ZERO, size, (float) Math.random() * 1f, 1f, COLOR1);
                                        }
                                    }
                                }
                            }
			}
		}
	}
    }
    
    private List<ShipAPI> getFighters(ShipAPI carrier) {
	List<ShipAPI> result = new ArrayList<>();
		
	for (ShipAPI ship : Global.getCombatEngine().getShips()) {
		if (!ship.isFighter()) continue;
		if (ship.getWing() == null) continue;
		if (ship.getWing().getSourceShip() == carrier) {
			result.add(ship);
		}
	}
		
	return result;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        
        if (stats.getEntity() instanceof ShipAPI) {
		ship = (ShipAPI) stats.getEntity();
	} else {
		return;
	}
	for (ShipAPI fighter : getFighters(ship)) {
		if (fighter.isHulk()) continue;
		MutableShipStatsAPI fStats = fighter.getMutableStats();
		
                fStats.getBallisticWeaponDamageMult().unmodify(id);
		fStats.getEnergyWeaponDamageMult().unmodify(id);
		fStats.getMissileWeaponDamageMult().unmodify(id);
                
                fStats.getAutofireAimAccuracy().unmodify(id);
                fStats.getBallisticWeaponRangeBonus().unmodify(id);
                fStats.getEnergyWeaponRangeBonus().unmodify(id);
                fStats.getMaxSpeed().unmodify(id);

                fStats.getAcceleration().unmodify(id);
                fStats.getDeceleration().unmodify(id);
                fStats.getTurnAcceleration().unmodify(id);
                fStats.getMaxTurnRate().unmodify(id);
	}
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + DAMAGE_BOOST * effectLevel * 0.01f) + "x fighter damage", false);
        } else if (index == 1) {
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + AGILITY_BONUS * effectLevel * 0.01f) + "x fighter speed", false);
        }
        return null;
    }
    
    public void init(CombatEngineAPI engine, ShipAPI host) {
        this.engine = engine;
    }
}
