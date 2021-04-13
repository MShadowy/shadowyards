package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.Random;

public class MS_eirDecoSelectorScript implements EveryFrameWeaponEffectPlugin {
    private int maxFrames;
    private boolean runOnce;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        
        ShipAPI ship = weapon.getShip();
        
        if (!runOnce) {
            maxFrames = weapon.getAnimation().getNumFrames() - 1;
            weapon.getAnimation().setFrame(new Random(ship.hashCode()).nextInt(maxFrames));
            
            runOnce = true;
        }
    }
}
