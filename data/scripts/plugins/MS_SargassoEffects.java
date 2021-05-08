package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MS_effectsHook;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MS_SargassoEffects extends BaseEveryFrameCombatPlugin {
    
    private CombatEngineAPI engine;
    private final ShipAPI ship;
    
    private final IntervalUtil interval = new IntervalUtil(1.5f, 1.5f);
    
    public MS_SargassoEffects(@NotNull ShipAPI ship) {
        this.ship = ship;
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        
        if (engine.isPaused()) {
            return;
        }
        
        interval.advance(amount);
        if (interval.intervalElapsed()) {
            for (int i = 0; i < 1; i++) {
                MS_effectsHook.createPulse(ship.getLocation());
            }
        }
        
        if (ship.getSystem().getEffectLevel() <= 0) {
            engine.removePlugin(this);
        }
    }
    
    public void init(CombatEngineAPI engine, ShipAPI ship) {
        this.engine = engine;
    }
}
