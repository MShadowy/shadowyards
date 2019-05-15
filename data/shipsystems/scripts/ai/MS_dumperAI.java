package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

//also simple and straightforward (but mildly more complex); if flux is high and the ship is meaningful danger, dump the flux
public class MS_dumperAI implements ShipSystemAIScript {
    
    private static final float HARDFLUX_THRESH = 0.75f; 
    private static final float SOFTFLUX_THRESH = 0.85f;
    
    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f position, Vector2f collisionDanger, ShipAPI target) {
        if (engine.isPaused()) {
            return;
        }
        
        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            if (system.getCooldownRemaining() > 0) {
                return;
            }
            if (system.isOutOfAmmo()) {
                return;
            }
            if (system.isActive()) {
                return;
            }
            float curr_hard_flux = ship.getFluxTracker().getHardFlux() / ship.getFluxTracker().getMaxFlux();
            float curr_soft_flux = ship.getFluxTracker().getCurrFlux() / ship.getFluxTracker().getMaxFlux();
            if (curr_hard_flux >= HARDFLUX_THRESH || curr_soft_flux >= SOFTFLUX_THRESH) {
                ship.useSystem();
            }
        }
    }
    
}
