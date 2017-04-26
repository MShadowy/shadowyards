package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static com.fs.starfarer.api.impl.combat.RecallDeviceStats.getFighters;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_swacs extends BaseShipSystemScript {

    private CombatEngineAPI engine;
    private ShipAPI host;
    
    public static final Object KEY_JITTER = new Object();
    
    private static final Color COLOR1 = new Color(210, 125, 105, 155);
    
    public static final Color JITTER_UNDER_COLOR = new Color(255,50,0,125);
    public static final Color JITTER_COLOR = new Color(255,50,0,75);

    //Just some global variables.
    public static final float RANGE = 5000f;
    public static final float ACCURACY_BONUS = 10f;
    public static final float RANGE_BONUS = 25f;
    public static final float DAMAGE_BOOST = 33f;
    public static final float SPEED_BONUS = 15f;
    public static final float AGILITY_BONUS = 20f;
    private static final Vector2f ZERO = new Vector2f();

    //Creates a hashmap that keeps track of what ships are receiving the benefits.
    private static final Map<ShipAPI, ShipAPI> receiving = new HashMap<>();

    private static final String staticID = "sargassoSWACSbuff";

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            receiving.clear();
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
				
			fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_BOOST * effectLevel);
			fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_BOOST * effectLevel);
			fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_BOOST * effectLevel);
                        
                        fStats.getAutofireAimAccuracy().modifyMult(id, ACCURACY_BONUS);
                        fStats.getBallisticWeaponRangeBonus().modifyMult(id, RANGE_BONUS);
                        fStats.getEnergyWeaponRangeBonus().modifyMult(id, RANGE_BONUS);
                        fStats.getBeamWeaponRangeBonus().modifyMult(id, RANGE_BONUS);
                        
                        fStats.getMaxSpeed().modifyMult(id, SPEED_BONUS);
                        fStats.getAcceleration().modifyMult(id, AGILITY_BONUS);
                        fStats.getDeceleration().modifyMult(id, AGILITY_BONUS);
                        fStats.getMaxTurnRate().modifyMult(id, AGILITY_BONUS);
                        fStats.getTurnAcceleration().modifyMult(id, AGILITY_BONUS);
				
			if (jitterLevel > 0) {
				//fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
				fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponType.class));
					
				fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
				fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
				Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
                                
                                for (ShipEngineAPI engines : fighter.getEngineController().getShipEngines()) {
                                    if (engines.isDisabled() == false) {
                                    float size;
                                    for (int i = 0; i < 5; i++) {
                                        size = MathUtils.getRandomNumberInRange(8f, 2f);
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
	List<ShipAPI> result = new ArrayList<ShipAPI>();
		
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
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + SPEED_BONUS * effectLevel * 0.01f) + "x fighter speed", false);
        }
        return null;
    }
    
    public void init(CombatEngineAPI engine, ShipAPI host) {
        this.engine = engine;
        this.host = host;
    }
}
