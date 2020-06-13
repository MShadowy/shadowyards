package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.CombatUtils;

public class MS_ghostLighter implements EveryFrameWeaponEffectPlugin {
    private final List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {        
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
            
        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 400f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
                engine.addPlugin(new MS_ghostFireHandler(proj, null));
                alreadyRegisteredProjectiles.add(proj);
            }
        }
        
        //And clean up our registered projectile list
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }
    }
}
