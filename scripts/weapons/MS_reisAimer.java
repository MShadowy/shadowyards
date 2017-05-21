package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;

public class MS_reisAimer implements EveryFrameWeaponEffectPlugin {
    
    private final IntervalUtil interval = new IntervalUtil(0.03f, 0.03f);
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
        
        ShipAPI ship = weapon.getShip();
        
        //only bother if the ships alive
        if (ship.isAlive()) {
            //we don't need to get this more than once
            boolean runOnce = false;
            List<WeaponAPI> weapons = null;
            if (!runOnce) {
                weapons = ship.getAllWeapons();
                
                runOnce = true;
            }
            interval.advance(amount);
            
            //cycle through each frame
            if (interval.intervalElapsed()) {
                if (engine.isPaused()) {
                    return;
                }
                
                WeaponAPI pandoraWeapon = null;
                WeaponAPI reisWeapon = null;
                
                if (weapons != null) {
                    for (WeaponAPI w : weapons) {
                        //If it doesn't have the right weapon combo, don't bother
                        if (!w.getId().contentEquals("ms_pandora") && !w.getId().contentEquals("ms_stopper1")) {
                            continue;
                        }
                
                        //get the Pandora
                        if (w.getId().contentEquals("ms_pandora")) {
                            pandoraWeapon = w;
                        }
                
                        //and the REIS
                        if (w.getId().contentEquals("ms_stopper1")) {
                            reisWeapon = w;
                        }
                    }
                
                
                    if (pandoraWeapon != null && reisWeapon != null) {
                        float angle = pandoraWeapon.getCurrAngle();
                        reisWeapon.setCurrAngle(angle);
                    }
                }
            }
        }
    }
}
