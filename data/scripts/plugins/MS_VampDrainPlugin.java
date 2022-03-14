package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MS_VampDrainPlugin extends BaseEveryFrameCombatPlugin {
    private final List<ShipAPI> Register = new ArrayList<>();
    
    private CombatEngineAPI engine;
    private final ShipAPI ship;
    
    private final Float FLUX_MALUS = .9f;
    
    private final IntervalUtil effectTime = new IntervalUtil(15f, 15f);
    
    public MS_VampDrainPlugin (@NotNull ShipAPI ship) {
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
        
        String id = ship.getFleetMemberId();
        
        if (!effectTime.intervalElapsed()) 
        {
            ship.getMutableStats().getFluxCapacity().modifyMult(id, FLUX_MALUS);
            
            if (!Register.contains(ship)) {
                Register.add(ship);
            }
        }
        
        if (Register.contains(ship)) {
            effectTime.advance(amount);
        }
        
        //Checks if our script should be removed from the combat engine
	if (effectTime.intervalElapsed() || !ship.isAlive() || ship.isHulk() || !Global.getCombatEngine().isEntityInPlay(ship)) 
        {
            ship.getMutableStats().getFluxCapacity().unmodify(id);
            
            Global.getCombatEngine().removePlugin(this);
            Register.remove(ship);
	}
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
