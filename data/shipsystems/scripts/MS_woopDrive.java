package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;


public class MS_woopDrive extends BaseShipSystemScript {
    
    //ship system for the Clade; will very likely require a custom AI
    //Makes the ship go backwards basically like a reverse Burn Drive
    private static final String DATA_KEY = "ms_woopDrive";
    
    public static float effectLevel(ShipAPI ship) {
        final CladeData cladeData = (CladeData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (cladeData == null) {
            return 0f;
        }

        final Map<ShipAPI, Float> acting = cladeData.acting;

        if (acting.containsKey(ship)) {
            return acting.get(ship);
        } else {
            return 0f;
        }
    }
    
    private boolean started = false;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        
        if (engine.isPaused()) {
            return;
        }
        
        final CladeData localData = (CladeData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, Float> acting = localData.acting;
        
        ShipAPI ship = (ShipAPI) stats.getEntity();
        //So we need to get the ships facing, then apply the thrust in reverse
        if (ship.isAlive()) {
            if (effectLevel > 0f) {
                acting.put(ship, effectLevel);
            }
            
            //target a vector directly behind the ship
            Vector2f dir;
            Vector2f point = new Vector2f(-50f, 0f);
            VectorUtils.rotate(point, ship.getFacing(), point);
            Vector2f.add(point, ship.getLocation(), point);
            
            dir = (Vector2f) VectorUtils.getDirectionalVector(ship.getLocation(), point).scale(50f);
            Vector2f.add(ship.getVelocity(), dir, ship.getVelocity());
            
            if (state == State.OUT) {
                stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
                
                ship.getEngineController().isDecelerating();
                
                float speed = ship.getVelocity().length();
                if (speed < 300f) {
                    ship.getVelocity().normalise();
                    ship.getVelocity().scale(stats.getMaxSpeed().modified);
                }
            } else {
                if (!started) {
                    started = true;
                }
                
                stats.getMaxSpeed().modifyFlat(id, 145f * effectLevel);
		stats.getAcceleration().modifyFlat(id, 200f * effectLevel);
                
                ship.getEngineController().isAcceleratingBackwards();
                
                float speed = ship.getVelocity().length();
                if ( speed <= 0.1f ) {
                    //point the ships vector behind it
                    ship.getVelocity().set(VectorUtils.getDirectionalVector(ship.getLocation(), dir)).scale(stats.getMaxSpeed().modified);
                }
                if (speed < 300f) {
                    ship.getVelocity().normalise();
                    ship.getVelocity().scale(stats.getMaxSpeed().modified);
                }
            }
        }
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        started = false;
        
        if (ship != null) {
            if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
                Global.getCombatEngine().getCustomData().put(DATA_KEY, new CladeData());
            }
            final CladeData localData = (CladeData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
            if (localData != null) {
                final Map<ShipAPI, Float> acting = localData.acting;

                acting.remove(ship);
            }
        }
        
	stats.getAcceleration().unmodify(id);
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return new StatusData("retro-thrusters overcharged", false);
    }
    
    private static final class CladeData {
        final Map<ShipAPI, Float> acting = new HashMap<>(50);
    }
}
