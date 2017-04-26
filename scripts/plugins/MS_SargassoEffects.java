package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MS_effectsHook;
import java.util.List;

public class MS_SargassoEffects extends BaseEveryFrameCombatPlugin {
    
    private CombatEngineAPI engine;
    
    private final IntervalUtil interval = new IntervalUtil(1.5f, 1.5f);
    
    private static final String SYS = "ms_swacs";
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        
        if (engine.isPaused()) {
            return;
        }
        
        for (ShipAPI ship : engine.getShips()) {
            if (!ship.getSystem().getId().contains(SYS) || !ship.isAlive()) {
                continue; //since this only goes for Sargasso's we can exclude most everything
            }
            
            if (ship.getSystem().isActive()) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    for (int i = 0; i < 1; i++) {
                            MS_effectsHook.createPulse(ship.getLocation());
                    }
                }
            }
        }
    }
    
    public void init(CombatEngineAPI engine, ShipAPI ship) {
        this.engine = engine;
    }
}
