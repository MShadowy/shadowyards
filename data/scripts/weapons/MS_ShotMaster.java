package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.MS_PulsePopperPlugin;
import org.lazywizard.lazylib.combat.CombatUtils;

public class MS_ShotMaster implements EveryFrameWeaponEffectPlugin {
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) { 
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        
        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 400f)) {
            onFire(proj, weapon, engine);
        }
    }
    
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if (proj.getWeapon() == weapon && engine.isEntityInPlay(proj) && !proj.didDamage()) {
            String spec = proj.getProjectileSpecId();
            switch (spec) {
                case "ms_mcepc_blast":
                case "ms_gigacepc_blast":
                case "ms_cepc_blast":
                case "ms_cepc_tiny":
                    engine.addPlugin(new MS_PulsePopperPlugin(proj));
                    break;
            }
        }
    }
}
