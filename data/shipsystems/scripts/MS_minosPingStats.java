package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.util.HashMap;
import java.util.Map;

public class MS_minosPingStats extends BaseShipSystemScript {
    
    private CombatEngineAPI engine;
    
    private final static int ECM_BUFF = 1;
    private static final float SENSOR_BOOST = 33f;
    
    //Creates a hashmap that keeps track of what ships are receiving the benefits.
    private static final Map<ShipAPI, ShipAPI> receiving = new HashMap<>();
        
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            receiving.clear();
        }
        
        ShipAPI host_ship = (ShipAPI) stats.getEntity();
        
        
        //the minos sensor buff goes here
        stats.getSightRadiusMod().modifyPercent(id, effectLevel * SENSOR_BOOST);
        
        //fleet wide ecm buff goes here
        if (effectLevel > 0) {
            for (ShipAPI ship : engine.getShips()) {
                if (ship.isHulk() || ship.isFighter() || host_ship.getOwner() != ship.getOwner()) {
                    continue;
                }
                
                if (state ==  ShipSystemStatsScript.State.OUT) {
                    ship.getMutableStats().getEccmChance().modifyFlat(id, effectLevel);
                }  else {
                    ship.getMutableStats().getEccmChance().modifyFlat(id, effectLevel * ECM_BUFF);
                } 
            }
        }
    }
        
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        //same objects as before.
        ShipAPI host_ship = (ShipAPI) stats.getEntity();
        
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            receiving.clear();
        }
        
        stats.getSightRadiusMod().unmodify();
        
        for (ShipAPI ship : engine.getShips()) {
            if (ship.isHulk() || ship.isFighter() || host_ship.getOwner() != ship.getOwner()) {
                    continue;
            }
            
            ship.getMutableStats().getEccmChance().unmodify();
        }
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("sensor strength increased", false);
        } else if (index == 1) {
            return new StatusData("fleet ecm capabilities improved", false);
        }
        return null;
    }
    
    public void init (CombatEngineAPI engine) {
        this.engine = engine;
    }
}
