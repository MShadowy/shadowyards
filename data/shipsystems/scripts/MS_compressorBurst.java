package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MS_compressorBurst extends BaseShipSystemScript {
    //alright, ship system for the Ninurta; speed boost system
    //applies a very brief ACTIVE period with a drawn out OUT
    //a high time compression during the brief in, that winds down during the out phase
    private static final float MAX_TIME_MULT = 10f;
    
    private static final Color JITTER_COLOR = new Color(15,15,15,55);
    private static final Color JITTER_UNDER_COLOR = new Color(185,234,253,75);
    
    //particle constants
    private static final int MAX_PARTICLES_PER_FRAME = 6; // Based on charge level
    private static final Color PARTICLE_COLOR = new Color(185,234,253);
    private static final float PARTICLE_OPACITY = 0.85f;
    private static final float PARTICLE_RADIUS = 130f;
    private static final float PARTICLE_SIZE = 4f;
    
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
                break;
            case ACTIVE:
                //we get a sort of subdued jitter effect for the brief active
                //and create a shockwave effect that sucks in                
                jitterLevel = 1f;
                
                jitterRangeBonus = jitterLevel * maxRangeBonus;
                
                stats.getMaxSpeed().modifyFlat(id, 80f * effectLevel);
                stats.getMaxSpeed().modifyPercent(id, 5f * effectLevel);
                stats.getAcceleration().modifyPercent(id, 220f * effectLevel);
                stats.getDeceleration().modifyPercent(id, 150f * effectLevel);
                stats.getTurnAcceleration().modifyFlat(id, 100f * effectLevel);
                stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
                stats.getMaxTurnRate().modifyFlat(id, 50f * effectLevel);
                stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
                break;
            case OUT:
                //jitter stops, we spawn wandering particles which follow behind the ship
                jitterLevel = 1f * effectLevel;
                Vector2f loc = new Vector2f(ship.getLocation());
                loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);
                
                Vector2f particlePos, particleVel; 
                int numParticlesThisFrame = Math.round(effectLevel * MAX_PARTICLES_PER_FRAME);
                for (int x = 0; x < numParticlesThisFrame; x++) {
                    particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), PARTICLE_RADIUS);
                    particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                    Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, PARTICLE_SIZE, PARTICLE_OPACITY, 1f,
                                                          PARTICLE_COLOR);
                }
                
                stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel); // to slow down ship to its regular top speed while powering drive down
                stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
                stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
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
        
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxSpeed().unmodifyPercent(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getMaxTurnRate().unmodifyPercent(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getTurnAcceleration().unmodifyPercent(id);
        stats.getAcceleration().unmodify(id);
        stats.getAcceleration().unmodifyPercent(id);
        stats.getDeceleration().unmodify(id);
        stats.getDeceleration().unmodifyPercent(id);
        
        Global.getCombatEngine().getTimeMult().unmodify(id);
	stats.getTimeMult().unmodify(id);
    }
}
